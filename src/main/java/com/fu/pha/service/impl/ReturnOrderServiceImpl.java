package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ReturnOrderItemRequestDto;
import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
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
import java.util.List;

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

    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;

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

        double totalRefundAmount = 0.0;

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

            // Lấy danh sách các SaleOrderItem liên quan theo FIFO
            List<SaleOrderItem> saleOrderItems = saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderByIdDesc(saleOrder.getId(), productId);
            int remainingQuantityToReturn = smallestQuantityToReturn;

            for (SaleOrderItem saleOrderItem : saleOrderItems) {
                if (remainingQuantityToReturn <= 0) {
                    break;
                }

                // Kiểm tra số lượng có thể trả lại cho từng SaleOrderItem
                int availableQuantityForReturn = (saleOrderItem.getQuantity() - saleOrderItem.getReturnedQuantity()) * saleOrderItem.getConversionFactor();
                if (availableQuantityForReturn <= 0) {
                    continue;
                }

                // Tính toán số lượng trả lại cho SaleOrderItem hiện tại
                int quantityToReturnFromItem = Math.min(availableQuantityForReturn, remainingQuantityToReturn);

                // Cập nhật returnedQuantity trong SaleOrderItem
                saleOrderItem.setReturnedQuantity(saleOrderItem.getReturnedQuantity() + quantityToReturnFromItem);
                saleOrderItemRepository.save(saleOrderItem);

                // Cập nhật tồn kho trong các lô hàng (ImportItem)
                List<SaleOrderItemBatch> saleOrderItemBatches = saleOrderItemBatchRepository.findBySaleOrderItemId(saleOrderItem.getId());

                int remainingQuantityToRestore = quantityToReturnFromItem * conversionFactor;
                for (SaleOrderItemBatch saleOrderItemBatch : saleOrderItemBatches) {
                    if (remainingQuantityToRestore <= 0) {
                        break;
                    }

                    ImportItem batch = saleOrderItemBatch.getImportItem();
                    int batchQuantityUsed = saleOrderItemBatch.getQuantity();

                    int restoreQuantity = Math.min(batchQuantityUsed, remainingQuantityToRestore);

                    // Cập nhật tồn kho của ImportItem
                    batch.setRemainingQuantity(batch.getRemainingQuantity() + restoreQuantity);
                    importItemRepository.save(batch);

                    // Lưu vào lịch sử tồn kho
                    InventoryHistory inventoryHistory = new InventoryHistory();
                    inventoryHistory.setImportItem(batch);
                    inventoryHistory.setRecordDate(Instant.now());
                    inventoryHistory.setRemainingQuantity(batch.getRemainingQuantity()); // Số lượng tồn hiện tại
                    inventoryHistory.setChangeQuantity(restoreQuantity); // Số lượng thay đổi
                    inventoryHistoryRepository.save(inventoryHistory);

                    // Cập nhật SaleOrderItemBatch
                    saleOrderItemBatch.setQuantity(batchQuantityUsed - restoreQuantity);
                    saleOrderItemBatchRepository.save(saleOrderItemBatch);

                    remainingQuantityToRestore -= restoreQuantity;
                }

                remainingQuantityToReturn -= quantityToReturnFromItem;
            }

            // Nếu còn số lượng cần trả lại mà không đủ trong các SaleOrderItem, ném ngoại lệ
            if (remainingQuantityToReturn > 0) {
                throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
            }

            // Cập nhật lại tổng tồn kho của sản phẩm
            product.setTotalQuantity(product.getTotalQuantity() + smallestQuantityToReturn);
            productRepository.save(product);

            // Tạo ReturnOrderItem
            ReturnOrderItem returnOrderItem = new ReturnOrderItem();
            returnOrderItem.setReturnOrder(returnOrder);
            returnOrderItem.setProduct(product);
            returnOrderItem.setQuantity(quantityToReturn);
            returnOrderItem.setUnitPrice(unitPrice);
            returnOrderItem.setTotalAmount(unitPrice * quantityToReturn);
            returnOrderItem.setUnit(itemRequestDto.getUnit());
            returnOrderItem.setConversionFactor(conversionFactor);

            // Thêm ReturnOrderItem vào danh sách của ReturnOrder
            returnOrder.getReturnOrderItems().add(returnOrderItem);

            // Cập nhật tổng tiền hoàn lại
            totalRefundAmount += returnOrderItem.getTotalAmount();
        }

        // 4. Cập nhật tổng số tiền hoàn lại vào ReturnOrder và lưu lại
        returnOrder.setRefundAmount(totalRefundAmount);
        returnOrderRepository.save(returnOrder);
        saleOrder.setCheckBackOrder(true);
        saleOrderRepository.save(saleOrder);
    }


    @Override
    @Transactional
    public void updateReturnOrder(Long returnOrderId, ReturnOrderRequestDto returnOrderRequestDto) {
        // 1. Lấy ReturnOrder từ cơ sở dữ liệu để đảm bảo tồn tại
        ReturnOrder returnOrder = returnOrderRepository.findById(returnOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.RETURN_ORDER_NOT_FOUND));

        // 2. Rollback các cập nhật trước đó trong các bảng liên quan (Rollback)
        for (ReturnOrderItem returnOrderItem : returnOrder.getReturnOrderItems()) {
            Long productId = returnOrderItem.getProduct().getId();
            int quantityToRollback = returnOrderItem.getQuantity() * returnOrderItem.getConversionFactor();

            // Rollback theo thứ tự FIFO trên các SaleOrderItem và SaleOrderItemBatch
            List<SaleOrderItem> saleOrderItems = saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderByIdDesc(returnOrder.getSaleOrder().getId(), productId);

            int remainingQuantityToRollback = quantityToRollback;
            for (SaleOrderItem saleOrderItem : saleOrderItems) {
                if (remainingQuantityToRollback <= 0) break;

                // Rollback returnedQuantity trong SaleOrderItem
                int rollbackQuantity = Math.min(saleOrderItem.getReturnedQuantity(), remainingQuantityToRollback);
                saleOrderItem.setReturnedQuantity(saleOrderItem.getReturnedQuantity() - rollbackQuantity);
                saleOrderItemRepository.save(saleOrderItem);

                // Rollback tồn kho của ImportItem thông qua SaleOrderItemBatch theo thứ tự FIFO
                List<SaleOrderItemBatch> saleOrderItemBatches = saleOrderItemBatchRepository.findBySaleOrderItemIdOrderByImportItemIdAsc(saleOrderItem.getId());
                for (SaleOrderItemBatch saleOrderItemBatch : saleOrderItemBatches) {
                    if (remainingQuantityToRollback <= 0) break;

                    ImportItem batch = saleOrderItemBatch.getImportItem();
                    int rollbackBatchQuantity = Math.min(saleOrderItemBatch.getQuantity(), remainingQuantityToRollback);

                    batch.setRemainingQuantity(batch.getRemainingQuantity() - rollbackBatchQuantity);
                    importItemRepository.save(batch);

                    saleOrderItemBatch.setQuantity(saleOrderItemBatch.getQuantity() + rollbackBatchQuantity);
                    saleOrderItemBatchRepository.save(saleOrderItemBatch);

                    remainingQuantityToRollback -= rollbackBatchQuantity;
                }
            }

            // Rollback tổng tồn kho của Product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));
            product.setTotalQuantity(product.getTotalQuantity() - quantityToRollback);
            productRepository.save(product);
        }

        // 3. Cập nhật các thuộc tính ReturnOrder từ ReturnOrderRequestDto
        returnOrder.setReturnReason(returnOrderRequestDto.getReturnReason());
        returnOrder.setReturnDate(Instant.now());

        double totalRefundAmount = 0.0;

        // 4. Thực hiện lại việc cập nhật với số lượng trả hàng mới
        for (ReturnOrderItemRequestDto itemRequestDto : returnOrderRequestDto.getReturnOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer quantityToReturn = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            int conversionFactor = itemRequestDto.getConversionFactor();
            int smallestQuantityToReturn = quantityToReturn * conversionFactor;

            // Xử lý các SaleOrderItem theo thứ tự FIFO
            List<SaleOrderItem> saleOrderItems = saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderByIdDesc(returnOrder.getSaleOrder().getId(), productId);
            int remainingQuantityToReturn = smallestQuantityToReturn;

            for (SaleOrderItem saleOrderItem : saleOrderItems) {
                if (remainingQuantityToReturn <= 0) break;

                int availableQuantityForReturn = (saleOrderItem.getQuantity() - saleOrderItem.getReturnedQuantity()) * saleOrderItem.getConversionFactor();
                if (availableQuantityForReturn <= 0) continue;

                int quantityToReturnFromItem = Math.min(availableQuantityForReturn, remainingQuantityToReturn);

                saleOrderItem.setReturnedQuantity(saleOrderItem.getReturnedQuantity() + quantityToReturnFromItem);
                saleOrderItemRepository.save(saleOrderItem);

                List<SaleOrderItemBatch> saleOrderItemBatches = saleOrderItemBatchRepository.findBySaleOrderItemIdOrderByImportItemIdAsc(saleOrderItem.getId());
                int remainingQuantityToRestore = quantityToReturnFromItem * conversionFactor;

                for (SaleOrderItemBatch saleOrderItemBatch : saleOrderItemBatches) {
                    if (remainingQuantityToRestore <= 0) break;

                    ImportItem batch = saleOrderItemBatch.getImportItem();
                    int batchQuantityUsed = saleOrderItemBatch.getQuantity();
                    int restoreQuantity = Math.min(batchQuantityUsed, remainingQuantityToRestore);

                    batch.setRemainingQuantity(batch.getRemainingQuantity() + restoreQuantity);
                    importItemRepository.save(batch);

                    // Lưu vào lịch sử tồn kho
                    InventoryHistory inventoryHistory = new InventoryHistory();
                    inventoryHistory.setImportItem(batch);
                    inventoryHistory.setRecordDate(Instant.now());
                    inventoryHistory.setRemainingQuantity(batch.getRemainingQuantity()); // Số lượng tồn hiện tại
                    inventoryHistory.setChangeQuantity(restoreQuantity); // Số lượng thay đổi
                    inventoryHistory.setReason("Return Order");
                    inventoryHistoryRepository.save(inventoryHistory);

                    saleOrderItemBatch.setQuantity(batchQuantityUsed - restoreQuantity);
                    saleOrderItemBatchRepository.save(saleOrderItemBatch);

                    remainingQuantityToRestore -= restoreQuantity;
                }

                remainingQuantityToReturn -= quantityToReturnFromItem;
            }

            if (remainingQuantityToReturn > 0) {
                throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
            }

            product.setTotalQuantity(product.getTotalQuantity() + smallestQuantityToReturn);
            productRepository.save(product);

            ReturnOrderItem returnOrderItem = returnOrder.getReturnOrderItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst()
                    .orElse(new ReturnOrderItem());

            returnOrderItem.setReturnOrder(returnOrder);
            returnOrderItem.setProduct(product);
            returnOrderItem.setQuantity(quantityToReturn);
            returnOrderItem.setUnitPrice(unitPrice);
            returnOrderItem.setTotalAmount(unitPrice * quantityToReturn);
            returnOrderItem.setUnit(itemRequestDto.getUnit());
            returnOrderItem.setConversionFactor(conversionFactor);

            returnOrder.getReturnOrderItems().add(returnOrderItem);
            totalRefundAmount += returnOrderItem.getTotalAmount();
        }

        // 5. Cập nhật tổng số tiền hoàn lại và lưu ReturnOrder
        returnOrder.setRefundAmount(totalRefundAmount);
        returnOrderRepository.save(returnOrder);
    }

    @Override
    public SaleOrderResponseDto getSaleOrderByInvoiceNumber(String invoiceNumber) {
        // 1. Truy vấn SaleOrder từ cơ sở dữ liệu
        SaleOrder saleOrder = saleOrderRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_NOT_FOUND));

        return new SaleOrderResponseDto(saleOrder);
    }

    @Override
    public ReturnOrderResponseDto getReturnOrderById(Long returnOrderId) {
        // 1. Truy vấn ReturnOrder từ cơ sở dữ liệu
        ReturnOrder returnOrder = returnOrderRepository.findById(returnOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.RETURN_ORDER_NOT_FOUND));

        return new ReturnOrderResponseDto(returnOrder);
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
