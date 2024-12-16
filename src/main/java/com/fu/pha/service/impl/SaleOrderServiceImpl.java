package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.request.SaleOrder.SaleOrderRequestDto;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.enums.PaymentStatus;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.SaleOrderService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @Autowired
    private SaleOrderItemRepository saleOrderItemRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private GenerateCode generateCode;

    @Autowired
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Override
    @Transactional
    public int createSaleOrder(SaleOrderRequestDto saleOrderRequestDto) {
        // Kiểm tra và lấy các đối tượng liên quan
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Doctor doctor = null;
        if (saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION && saleOrderRequestDto.getDoctorId() != null) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // Tạo SaleOrder
        SaleOrder saleOrder = new SaleOrder();
        String lastInvoiceNumber = saleOrderRepository.getLastInvoiceNumber();
        saleOrder.setInvoiceNumber(lastInvoiceNumber == null ? "XB000001" : generateCode.generateNewProductCode(lastInvoiceNumber));
        saleOrder.setSaleDate(Instant.now());
        saleOrder.setOrderType(saleOrderRequestDto.getOrderType());
        saleOrder.setPaymentMethod(saleOrderRequestDto.getPaymentMethod());
        saleOrder.setDiscount(saleOrderRequestDto.getDiscount() != null ? saleOrderRequestDto.getDiscount() : 0.0);
        saleOrder.setCustomer(customer);
        saleOrder.setUser(user);
        saleOrder.setDoctor(doctor);
        saleOrder.setDiagnosis(saleOrderRequestDto.getDiagnosis());
        saleOrder.setCheckBackOrder(false);

        // Thiết lập trạng thái thanh toán
        if (saleOrderRequestDto.getPaymentMethod() == PaymentMethod.CASH) {
            saleOrder.setPaymentStatus(PaymentStatus.PAID);
        } else {
            saleOrder.setPaymentStatus(PaymentStatus.UNPAID);
        }


        // Lưu SaleOrder
        saleOrderRepository.save(saleOrder);

        // Lưu các SaleOrderItem
        double totalOrderAmount = 0.0;
        List<SaleOrderItem> saleOrderItems = new ArrayList<>();
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Product product = productRepository.findById(itemRequestDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            SaleOrderItem saleOrderItem = new SaleOrderItem();
            saleOrderItem.setSaleOrder(saleOrder);
            saleOrderItem.setProduct(product);
            saleOrderItem.setTotalAmount(itemRequestDto.getTotalAmount());
            saleOrderItem.setQuantity(itemRequestDto.getQuantity());
            saleOrderItem.setUnitPrice(itemRequestDto.getUnitPrice());
            saleOrderItem.setDiscount(itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
            saleOrderItem.setConversionFactor(itemRequestDto.getConversionFactor());
            saleOrderItem.setDosage(itemRequestDto.getDosage());
            saleOrderItem.setUnit(itemRequestDto.getUnit());

            double itemTotalAmount = calculateSaleOrderItemTotalAmount(itemRequestDto);
            saleOrderItem.setTotalAmount(itemTotalAmount);

            saleOrderItemRepository.save(saleOrderItem);
            saleOrderItems.add(saleOrderItem);

            totalOrderAmount += itemTotalAmount;
        }

        checkInventory(saleOrderRequestDto.getSaleOrderItems());
        if (saleOrderRequestDto.getTotalAmount() != null) {
            double feTotalAmount = saleOrderRequestDto.getTotalAmount();
            if (Math.abs(totalOrderAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        // Cập nhật tổng tiền cho SaleOrder
        saleOrder.setSaleOrderItemList(saleOrderItems);
        saleOrder.setTotalAmount(totalOrderAmount);
        saleOrderRepository.save(saleOrder);

        // Nếu thanh toán là tiền mặt, thực hiện cập nhật kho
        if (saleOrder.getPaymentStatus() == PaymentStatus.PAID) {
            processOrderInventory(saleOrder);
        }

        return saleOrder.getId().intValue();
    }

    public void checkInventory(List<SaleOrderItemRequestDto> saleOrderItems) {
        for (SaleOrderItemRequestDto item : saleOrderItems) {
            int requiredQuantity = item.getQuantity() * item.getConversionFactor();
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(item.getProductId());

            int availableQuantity = 0;

            for (ImportItem batch : batches) {
                if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(Instant.now())) {
                    continue;
                }
                availableQuantity += batch.getRemainingQuantity();
                if (availableQuantity >= requiredQuantity) {
                    break;
                }
            }
            if (availableQuantity < requiredQuantity) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }
        }
    }

    // Xử lý logic cập nhật kho, batch, product khi thanh toán hoàn tất
    public void processOrderInventory(SaleOrder saleOrder) {
        for (SaleOrderItem saleOrderItem : saleOrder.getSaleOrderItemList()) {
            Product product = saleOrderItem.getProduct();

            int smallestQuantityToSell = saleOrderItem.getQuantity() * saleOrderItem.getConversionFactor();
            List<ImportItem> batches = importItemRepository.findByProductIdOrderByCreateDateAsc(product.getId());
            int remainingQuantity = smallestQuantityToSell;

            ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
            LocalDate today = LocalDate.now(zoneId);
            LocalDate tomorrow = today.plusDays(1);
            ZonedDateTime zonedDateTimeTomorrow = tomorrow.atStartOfDay(zoneId);
            Instant tomorrowInstant = zonedDateTimeTomorrow.toInstant();

            for (ImportItem batch : batches) {

                if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(tomorrowInstant)) {
                    // Bỏ qua lô hàng này nếu đã hết hạn
                    continue;
                }
                if (batch.getRemainingQuantity() > 0) {
                    int quantityFromBatch = Math.min(batch.getRemainingQuantity(), remainingQuantity);
                    batch.setRemainingQuantity(batch.getRemainingQuantity() - quantityFromBatch);
                    importItemRepository.save(batch);

                    SaleOrderItemBatch saleOrderItemBatch = new SaleOrderItemBatch();
                    saleOrderItemBatch.setImportItem(batch);
                    saleOrderItemBatch.setQuantity(quantityFromBatch);
                    saleOrderItemBatch.setReturnedQuantity(0);
                    saleOrderItemBatch.setSaleOrderItem(saleOrderItem);
                    saleOrderItemBatchRepository.save(saleOrderItemBatch);

                    remainingQuantity -= quantityFromBatch;
                    if (remainingQuantity <= 0) break;
                }
            }

            if (remainingQuantity > 0) {
                throw new BadRequestException(Message.OUT_OF_STOCK);
            }

            // Cập nhật tồn kho của sản phẩm
            product.setTotalQuantity(product.getTotalQuantity() - smallestQuantityToSell);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void completePayment(long orderId) {
        // Lấy SaleOrder từ DB
        SaleOrder saleOrder = saleOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        if (saleOrder.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(Message.ORDER_ALREADY_PAID);
        }

        // Cập nhật trạng thái thành PAID
        saleOrder.setPaymentStatus(PaymentStatus.PAID);
        saleOrderRepository.save(saleOrder);

        // Gọi hàm xử lý cập nhật kho
        processOrderInventory(saleOrder);
    }


    private double calculateSaleOrderItemTotalAmount(SaleOrderItemRequestDto itemRequestDto) {
        double unitPrice = itemRequestDto.getUnitPrice();
        int quantity = itemRequestDto.getQuantity();
        double discount = itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0;

        // Tính tổng tiền trước chiết khấu
        double total = unitPrice * quantity;

        // Áp dụng chiết khấu
        total = total - (total * discount / 100);

        return total;
    }

    @Override
    @Transactional
    public void updateSaleOrder(Long saleOrderId, SaleOrderRequestDto saleOrderRequestDto) {
        // 1. Lấy SaleOrder từ cơ sở dữ liệu và kiểm tra sự tồn tại
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        // 2. Không cho phép cập nhật nếu trạng thái là PAID
        if (saleOrder.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(Message.CANNOT_UPDATE_PAID_ORDER);
        }

        // 3. Cập nhật các thông tin cơ bản
        Customer customer = customerRepository.findById(saleOrderRequestDto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND));

        User user = userRepository.findById(saleOrderRequestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.USER_NOT_FOUND));

        Doctor doctor = null;
        if (saleOrderRequestDto.getDoctorId() != null && saleOrderRequestDto.getOrderType() == OrderType.PRESCRIPTION) {
            doctor = doctorRepository.findById(saleOrderRequestDto.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND));
        }

        // Cập nhật thông tin cơ bản của SaleOrder
        saleOrder.setCustomer(customer);
        saleOrder.setUser(user);
        saleOrder.setDoctor(doctor);
        saleOrder.setOrderType(saleOrderRequestDto.getOrderType());
        saleOrder.setSaleDate(saleOrderRequestDto.getSaleDate() != null ? saleOrderRequestDto.getSaleDate() : Instant.now());
        saleOrder.setPaymentMethod(saleOrderRequestDto.getPaymentMethod());
        saleOrder.setDiscount(saleOrderRequestDto.getDiscount() != null ? saleOrderRequestDto.getDiscount() : 0.0);
        saleOrder.setDiagnosis(saleOrderRequestDto.getDiagnosis());

        // 4. Cập nhật hoặc thêm mới các SaleOrderItem
        List<SaleOrderItem> existingItems = saleOrderItemRepository.findBySaleOrderId(saleOrderId);
        Map<Long, SaleOrderItem> existingItemsMap = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        double totalOrderAmount = 0.0;
        for (SaleOrderItemRequestDto itemRequestDto : saleOrderRequestDto.getSaleOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantity = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            SaleOrderItem saleOrderItem = existingItemsMap.get(productId);
            if (saleOrderItem != null) {
                // Cập nhật thông tin SaleOrderItem hiện có
                saleOrderItem.setQuantity(quantity);
                saleOrderItem.setUnitPrice(unitPrice);
                saleOrderItem.setDiscount(itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
                saleOrderItem.setConversionFactor(itemRequestDto.getConversionFactor());
                saleOrderItem.setDosage(itemRequestDto.getDosage());
                saleOrderItem.setUnit(itemRequestDto.getUnit());
                saleOrderItem.setTotalAmount(calculateSaleOrderItemTotalAmount(itemRequestDto));

                saleOrderItemRepository.save(saleOrderItem);
                existingItemsMap.remove(productId);
            } else {
                // Tạo mới SaleOrderItem
                SaleOrderItem saleOrderItemNew = new SaleOrderItem();
                saleOrderItemNew.setSaleOrder(saleOrder);
                saleOrderItemNew.setProduct(product);
                saleOrderItemNew.setQuantity(quantity);
                saleOrderItemNew.setUnitPrice(unitPrice);
                saleOrderItemNew.setDiscount(itemRequestDto.getDiscount() != null ? itemRequestDto.getDiscount() : 0.0);
                saleOrderItemNew.setConversionFactor(itemRequestDto.getConversionFactor());
                saleOrderItemNew.setDosage(itemRequestDto.getDosage());
                saleOrderItemNew.setUnit(itemRequestDto.getUnit());
                saleOrderItemNew.setTotalAmount(calculateSaleOrderItemTotalAmount(itemRequestDto));

                saleOrderItemRepository.save(saleOrderItemNew);
            }

            totalOrderAmount += calculateSaleOrderItemTotalAmount(itemRequestDto);
        }

        if (saleOrderRequestDto.getTotalAmount() != null) {
            double feTotalAmount = saleOrderRequestDto.getTotalAmount();
            if (Math.abs(totalOrderAmount - feTotalAmount) > 0.01) { // Cho phép sai số nhỏ
                throw new BadRequestException(Message.TOTAL_AMOUNT_NOT_MATCH);
            }
        } else {
            throw new BadRequestException(Message.TOTAL_AMOUNT_REQUIRED);
        }

        // Xóa các SaleOrderItem không còn trong yêu cầu
        if (!existingItemsMap.isEmpty()) {
            saleOrderItemRepository.deleteAll(existingItemsMap.values());
        }

        // Cập nhật tổng tiền
        saleOrder.setTotalAmount(totalOrderAmount);
        saleOrderRepository.save(saleOrder);
    }

    public SaleOrderResponseDto getSaleOrderById(Long saleOrderId) {
        // 1. Truy vấn SaleOrder từ cơ sở dữ liệu
        SaleOrder saleOrder = saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        return new SaleOrderResponseDto(saleOrder);
    }

    @Override
    public Page<SaleOrderResponseDto> getAllSaleOrderPaging(int page, int size, OrderType orderType, PaymentMethod paymentMethod, String invoiceNumber, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SaleOrderResponseDto> saleOrderResponseDto;

        //Nếu không có ngày bắt đầu và ngày kết thúc
        if (fromDate == null && toDate == null) {
            Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = LocalDate.now().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPagingWithoutDate(orderType, paymentMethod, invoiceNumber, startOfDay, endOfDay, pageable);
        }
        //Nếu chỉ có ngày bắt đầu
        else if (fromDate != null && toDate == null) {
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPagingFromDate(orderType, paymentMethod, invoiceNumber, fromDate, pageable);
        }
        //Nếu chỉ có ngày kết thúc
        else if (fromDate == null) {
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPagingToDate(orderType, paymentMethod, invoiceNumber, toDate, pageable);
        }
        //Nếu có cả ngày bắt đầu và ngày kết thúc
        else {
            saleOrderResponseDto = saleOrderRepository.getListSaleOrderPaging(orderType, paymentMethod, invoiceNumber, fromDate, toDate, pageable);
        }

        if (saleOrderResponseDto.isEmpty()) {
            throw new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND);
        }
        return saleOrderResponseDto;
    }

    @Override
    public void exportSaleOrdersToExcel(HttpServletResponse response, Instant fromInstant, Instant toInstant) throws IOException {
        // Fetch sale orders
        List<SaleOrderResponseDto> saleOrders = saleOrderRepository.getSaleOrdersByDateRange(fromInstant, toInstant);

        // Check if there is data to export
        if (saleOrders.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy dữ liệu hóa đơn bán trong khoảng thời gian đã chọn.");
        }

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách hóa đơn bán");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Date styling
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy     HH:mm"));

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Define column headers
        String[] headers = {"STT", "Mã hóa đơn", "Ngày tạo hóa đơn", "Người bán", "Loại hóa đơn", "Phương thức thanh toán", "Tổng tiền"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < saleOrders.size(); i++) {
            SaleOrderResponseDto saleOrder = saleOrders.get(i);
            Row row = sheet.createRow(rowNum++);

            // STT
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(i + 1);
            cell0.setCellStyle(dataCellStyle);

            // Mã hóa đơn
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(saleOrder.getInvoiceNumber());
            cell1.setCellStyle(dataCellStyle);

            // Ngày tạo hóa đơn
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(DateTimeFormatter.ofPattern("dd-MM-yyyy     HH:mm")
                    .withZone(ZoneOffset.ofHours(7))
                    .format(saleOrder.getSaleDate()));
            cell2.setCellStyle(dateStyle);

            // Người bán
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(saleOrder.getFullName());
            cell3.setCellStyle(dataCellStyle);




            // Loại hóa đơn
            Cell cell4 = row.createCell(4);
            String orderType = saleOrder.getOrderType().toString();
            if ("NORMAL".equals(orderType)) {
                orderType = "Bán thường";
            } else if ("PRESCRIPTION".equals(orderType)) {
                orderType = "Bán theo đơn bác sĩ";
            } else {
                orderType = "N/A";
            }
            cell4.setCellValue(orderType);
            cell4.setCellStyle(dataCellStyle);

            // Phương thức thanh toán
            Cell cell5 = row.createCell(5);
            String paymentMethod = saleOrder.getPaymentMethod().toString();
            if ("CASH".equals(paymentMethod)) {
                paymentMethod = "Tiền mặt";
            } else if ("TRANSFER".equals(paymentMethod)) {
                paymentMethod = "Chuyển khoản";
            } else {
                paymentMethod = "N/A";
            }
            cell5.setCellValue(paymentMethod);
            cell5.setCellStyle(dataCellStyle);

            // Tổng tiền
            Cell cell6 = row.createCell(6);
            cell6.setCellValue(saleOrder.getTotalAmount());
            cell6.setCellStyle(currencyStyle);
        }

        // Auto-size columns to fit the content
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write workbook to response output stream
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.flush();
        outputStream.close();
    }

}

