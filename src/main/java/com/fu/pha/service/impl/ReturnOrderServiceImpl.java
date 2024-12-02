package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ReturnOrderBatchRequestDto;
import com.fu.pha.dto.request.ReturnOrderItemRequestDto;
import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.*;
import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ReturnOrderService;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReturnOrderServiceImpl implements ReturnOrderService {

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @Autowired
    private SaleOrderItemRepository saleOrderItemRepository;

    @Autowired
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private GenerateCode generateCode;

    @Override
    @Transactional
    public void createReturnOrder(ReturnOrderRequestDto returnOrderRequestDto) {
        // 1. Lấy SaleOrder từ cơ sở dữ liệu để đảm bảo tồn tại hóa đơn bán hàng
        SaleOrder saleOrder = saleOrderRepository.findById(returnOrderRequestDto.getSaleOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        // 2. Tạo và thiết lập các thuộc tính cho ReturnOrder từ ReturnOrderRequestDto
        ReturnOrder returnOrder = new ReturnOrder();
        String lastInvoiceNumber = returnOrderRepository.getLastInvoiceNumber();
        returnOrder.setInvoiceNumber(lastInvoiceNumber == null ? "TR000001" : generateCode.generateNewProductCode(lastInvoiceNumber));
        returnOrder.setReturnDate(Instant.now());
        returnOrder.setReturnReason(returnOrderRequestDto.getReturnReason());
        returnOrder.setSaleOrder(saleOrder);
        returnOrder.setCustomer(saleOrder.getCustomer());
        returnOrder.setReturnOrderItems(new ArrayList<>());
        returnOrder.setRefundAmount(0.0);
        returnOrderRepository.save(returnOrder);


        // 3. Xử lý từng ReturnOrderItemRequestDto trong danh sách returnOrderItems
        for (ReturnOrderItemRequestDto itemRequestDto : returnOrderRequestDto.getReturnOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantityToReturn = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            // Lấy sản phẩm và tính toán số lượng nhỏ nhất cần trả lại
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int conversionFactor = itemRequestDto.getConversionFactor();
            int smallestQuantityToReturn = quantityToReturn * conversionFactor;

            // Lấy danh sách các SaleOrderItem liên quan theo thông tin từ DTO
            List<ReturnOrderBatchRequestDto> batchRequestDtos = itemRequestDto.getBatchRequestDtos(); // Lấy thông tin các lô cần trả lại từ DTO
            int remainingQuantityToReturn = smallestQuantityToReturn;

            SaleOrderItem saleOrderItem = saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(saleOrder.getId(), productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND));


            ReturnOrderItem returnOrderItem = new ReturnOrderItem();
            returnOrderItem.setReturnOrder(returnOrder);
            returnOrderItem.setProduct(product);
            returnOrderItem.setQuantity(quantityToReturn);
            returnOrderItem.setUnitPrice(unitPrice);
            returnOrderItem.setTotalAmount(unitPrice * quantityToReturn);
            returnOrderItem.setUnit(itemRequestDto.getUnit());
            returnOrderItem.setConversionFactor(conversionFactor);

            returnOrderItemRepository.save(returnOrderItem);
            // Duyệt qua từng lô trong DTO gửi về và xử lý trả lại
            for (ReturnOrderBatchRequestDto batchRequestDto : batchRequestDtos) {
                if (remainingQuantityToReturn <= 0) {
                    break;
                }

                ImportItem importItem = importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId(
                        batchRequestDto.getBatchNumber(),
                        productId,
                        batchRequestDto.getInvoiceNumber()
                ).orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND));

                // Tìm SaleOrderItemBatch dựa trên SaleOrderItem và ImportItem
                SaleOrderItemBatch saleOrderItemBatch = saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(
                        saleOrderItem,
                        importItem
                ).orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND));


                // Kiểm tra số lượng có thể trả lại cho từng SaleOrderItemBatch
                int availableQuantityForReturn = saleOrderItemBatch.getQuantity();
                int quantityReturn = batchRequestDto.getQuantity();
                if (availableQuantityForReturn < saleOrderItemBatch.getReturnedQuantity() || availableQuantityForReturn < quantityReturn) {
                    continue;
                }

                // Tính toán số lượng trả lại cho SaleOrderItemBatch hiện tại
                int quantityToReturnFromBatch = Math.min(quantityReturn, remainingQuantityToReturn);

                // Cập nhật lại số lượng trả lại trong SaleOrderItemBatch
                saleOrderItemBatch.setReturnedQuantity(quantityToReturnFromBatch);
                saleOrderItemBatch.setReturnOrderItem(returnOrderItem);
                saleOrderItemBatchRepository.save(saleOrderItemBatch);

                // Cập nhật lại tồn kho trong ImportItem (sản phẩm trả lại vào kho)
                ImportItem batch = saleOrderItemBatch.getImportItem();
                batch.setRemainingQuantity(batch.getRemainingQuantity() + quantityToReturnFromBatch);
                importItemRepository.save(batch);

                remainingQuantityToReturn -= quantityToReturnFromBatch;
            }

            // Nếu còn số lượng cần trả lại mà không đủ trong các lô được yêu cầu, ném ngoại lệ
            if (remainingQuantityToReturn > 0) {
                throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
            }

            // Cập nhật lại tổng tồn kho của sản phẩm
            product.setTotalQuantity(product.getTotalQuantity() + smallestQuantityToReturn);
            productRepository.save(product);


            // Thêm ReturnOrderItem vào danh sách của ReturnOrder
            returnOrder.getReturnOrderItems().add(returnOrderItem);

        }

        // 4. Cập nhật tổng số tiền hoàn lại vào ReturnOrder và lưu lại
        returnOrder.setRefundAmount(returnOrderRequestDto.getTotalAmount());
        returnOrderRepository.save(returnOrder);
        saleOrder.setCheckBackOrder(true);
        saleOrderRepository.save(saleOrder);
    }

    @Override
    public SaleOrderForReturnDto getSaleOrderForReturn(String invoiceNumber) {
        // Lấy đơn hàng từ cơ sở dữ liệu
        SaleOrder saleOrder = saleOrderRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        // Chuyển dữ liệu đơn hàng thành DTO
        SaleOrderForReturnDto saleOrderForReturnDto = new SaleOrderForReturnDto();
        saleOrderForReturnDto.setId(saleOrder.getId());
        saleOrderForReturnDto.setInvoiceNumber(saleOrder.getInvoiceNumber());
        saleOrderForReturnDto.setSaleDate(saleOrder.getSaleDate());
        saleOrderForReturnDto.setOrderType(saleOrder.getOrderType());
        saleOrderForReturnDto.setPaymentMethod(saleOrder.getPaymentMethod());
        saleOrderForReturnDto.setPaymentStatus(saleOrder.getPaymentStatus());
        saleOrderForReturnDto.setDiscount(saleOrder.getDiscount());
        saleOrderForReturnDto.setTotalAmount(saleOrder.getTotalAmount());

        // Lấy thông tin khách hàng và bác sĩ (nếu có)
        if (saleOrder.getCustomer() != null) {
            saleOrderForReturnDto.setCustomer(new CustomerDTOResponse(saleOrder.getCustomer()));
        }
        if (saleOrder.getDoctor() != null) {
            saleOrderForReturnDto.setDoctor(new DoctorDTOResponse(saleOrder.getDoctor()));
        }

        saleOrderForReturnDto.setUserId(saleOrder.getUser().getId());

        // Lấy danh sách các mặt hàng trong đơn hàng
        List<SaleOrderItemForReturnDto> saleOrderItems = new ArrayList<>();
        for (SaleOrderItem saleOrderItem : saleOrder.getSaleOrderItemList()) {
            SaleOrderItemForReturnDto saleOrderItemForReturnDto = new SaleOrderItemForReturnDto();
            saleOrderItemForReturnDto.setQuantity(saleOrderItem.getQuantity());
            saleOrderItemForReturnDto.setUnitPrice(saleOrderItem.getUnitPrice());
            saleOrderItemForReturnDto.setUnit(saleOrderItem.getUnit());
            saleOrderItemForReturnDto.setTotalAmount(saleOrderItem.getTotalAmount());
            saleOrderItemForReturnDto.setConversionFactor(saleOrderItem.getConversionFactor());

            // Lấy thông tin sản phẩm
            ProductDTOResponse productDTO = new ProductDTOResponse(saleOrderItem.getProduct());
            saleOrderItemForReturnDto.setProduct(productDTO);

            // Lấy thông tin các lô của sản phẩm trong đơn bán
            List<SaleOrderItemBatch> saleOrderItemBatches = saleOrderItemBatchRepository.findBySaleOrderItemId(saleOrderItem.getId());
            List<ImportItemBatchDto> importItemBatchDtos = new ArrayList<>();
            for (SaleOrderItemBatch saleOrderItemBatch : saleOrderItemBatches) {
                // Tạo DTO cho ImportItemBatchDto
                ImportItemBatchDto importItemBatchDto = new ImportItemBatchDto();
                importItemBatchDto.setInvoiceNumber(saleOrderItemBatch.getImportItem().getImportReceipt().getInvoiceNumber());  // Số hóa đơn nhập hàng
                importItemBatchDto.setBatchNumber(saleOrderItemBatch.getImportItem().getBatchNumber());       // Số lô
                importItemBatchDto.setQuantityToSell(saleOrderItemBatch.getQuantity());                             // Số lượng trong lô

                importItemBatchDtos.add(importItemBatchDto);
            }

            // Set danh sách các lô cho mặt hàng
            saleOrderItemForReturnDto.setImportItemBatchDtos(importItemBatchDtos);

            // Thêm mặt hàng vào danh sách
            saleOrderItems.add(saleOrderItemForReturnDto);
        }

        // Thêm danh sách mặt hàng vào DTO của đơn hàng
        saleOrderForReturnDto.setSaleOrderItems(saleOrderItems);

        return saleOrderForReturnDto;
    }



    @Override
    @Transactional
    public void updateReturnOrder(Long returnOrderId, ReturnOrderRequestDto returnOrderRequestDto) {

    }



    @Override
    @Transactional
    public ReturnOrderResponseDto getReturnOrderById(Long returnOrderId) {
        // Lấy ReturnOrder từ cơ sở dữ liệu
        ReturnOrder returnOrder = returnOrderRepository.findById(returnOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnOrder not found"));

        // Lấy danh sách ReturnOrderItem liên quan đến ReturnOrder
        List<ReturnOrderItem> returnOrderItems = returnOrderItemRepository.findByReturnOrderId(returnOrderId);

        // Ánh xạ ReturnOrder sang ReturnOrderResponseDto
        ReturnOrderResponseDto returnOrderResponseDto = new ReturnOrderResponseDto(returnOrder);

        // Ánh xạ từng ReturnOrderItem vào DTO và thêm thông tin SaleOrderItemBatch
        returnOrderResponseDto.setReturnOrderItems(returnOrderItems.stream()
                .map(returnOrderItem -> {
                    // Lấy thông tin các SaleOrderItemBatch liên quan đến ReturnOrderItem
                    List<SaleOrderItemBatch> saleOrderItemBatches = saleOrderItemBatchRepository.findByReturnOrderItemId(returnOrderItem.getId());

                    // Tạo DTO cho ReturnOrderItem và ánh xạ các SaleOrderItemBatch vào DTO
                    ReturnOrderItemResponseDto returnOrderItemResponseDto = new ReturnOrderItemResponseDto(returnOrderItem);
                    returnOrderItemResponseDto.setBatchResponseDtos(saleOrderItemBatches.stream()
                            .map(saleOrderItemBatch -> new SaleOrderItemBatchResponseDto(
                                    saleOrderItemBatch.getImportItem().getBatchNumber(),
                                    saleOrderItemBatch.getQuantity(),
                                    saleOrderItemBatch.getReturnedQuantity(),
                                    saleOrderItemBatch.getImportItem().getImportReceipt().getInvoiceNumber()
                            ))
                            .collect(Collectors.toList()));

                    return returnOrderItemResponseDto;
                })
                .collect(Collectors.toList()));

        return returnOrderResponseDto;
    }

    @Override
    public Page<ReturnOrderResponseDto> getAllReturnOrderPaging(int page, int size, String invoiceNumber, Instant fromDate, Instant toDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReturnOrderResponseDto> returnOrderResponseDto;

        if (fromDate == null && toDate == null) {
            returnOrderResponseDto = returnOrderRepository.getListReturnOrderPagingWithoutDate(invoiceNumber, pageable);
        } else if (fromDate != null && toDate == null) {
            returnOrderResponseDto = returnOrderRepository.getListReturnOrderPagingFromDate(invoiceNumber, fromDate, pageable);
        } else if (fromDate == null) {
            returnOrderResponseDto = returnOrderRepository.getListReturnOrderPagingToDate(invoiceNumber, toDate, pageable);
        } else {
            returnOrderResponseDto = returnOrderRepository.getListReturnOrderPaging(invoiceNumber, fromDate, toDate, pageable);
        }

        if (returnOrderResponseDto.isEmpty()) {
            throw new ResourceNotFoundException(Message.RETURN_ORDER_NOT_FOUND);
        }
        return returnOrderResponseDto;
    }

    @Override
    public void exportReturnOrdersToExcel(HttpServletResponse response, Instant fromInstant, Instant toInstant) throws IOException {
        // Fetch return orders
        List<ReturnOrderResponseDto> returnOrders = returnOrderRepository.getReturnOrdersByDateRange(fromInstant, toInstant);

        // Check if there is data to export
        if (returnOrders.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy dữ liệu phiếu trả hàng trong khoảng thời gian đã chọn.");
        }

        // Create workbook and sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách phiếu trả hàng");

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
        String[] headers = {"STT", "Mã phiếu", "Ngày tạo phiếu", "Người tạo phiếu", "Khách hàng", "Tiền trả khách"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < returnOrders.size(); i++) {
            ReturnOrderResponseDto returnOrder = returnOrders.get(i);
            Row row = sheet.createRow(rowNum++);

            // STT
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(i + 1);
            cell0.setCellStyle(dataCellStyle);

            // Mã phiếu
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(returnOrder.getInvoiceNumber());
            cell1.setCellStyle(dataCellStyle);

            // Ngày tạo phiếu
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(DateTimeFormatter.ofPattern("dd-MM-yyyy     HH:mm")
                    .withZone(ZoneOffset.ofHours(7))
                    .format(returnOrder.getReturnDate()));
            cell2.setCellStyle(dateStyle);

            // Người tạo phiếu
            Cell cell3 = row.createCell(3);
            cell3.setCellValue(returnOrder.getCreatedBy());
            cell3.setCellStyle(dataCellStyle);

            // Khách hàng
            Cell cell4 = row.createCell(4);
            cell4.setCellValue(returnOrder.getCustomer() != null ? returnOrder.getCustomer().getCustomerName() : "Khách hàng lẻ");
            cell4.setCellStyle(dataCellStyle);

            // Tiền trả khách
            Cell cell5 = row.createCell(5);
            cell5.setCellValue(returnOrder.getRefundAmount());
            cell5.setCellStyle(currencyStyle);
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
