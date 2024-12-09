package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ReturnOrderBatchRequestDto;
import com.fu.pha.dto.request.ReturnOrderItemRequestDto;
import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.dto.response.*;
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
import java.util.Map;
import java.util.Optional;
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
            Integer quantityToReturn = itemRequestDto.getQuantity() != null ? itemRequestDto.getQuantity() : 0;
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
                int quantityReturnB = batchRequestDto.getQuantity() != null ? batchRequestDto.getQuantity() : 0;
                int quantityReturn = quantityReturnB  * conversionFactor;
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
            saleOrderItemForReturnDto.setDiscount(saleOrderItem.getDiscount());

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
    public SaleOrderForReturnDto getSaleOrderForReturnById(Long id) {
        // Lấy đơn hàng từ cơ sở dữ liệu
        SaleOrder saleOrder = saleOrderRepository.findById(id)
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
            saleOrderItemForReturnDto.setDiscount(saleOrderItem.getDiscount());

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
        // 1. Lấy ReturnOrder hiện tại từ cơ sở dữ liệu
        ReturnOrder existingReturnOrder = returnOrderRepository.findById(returnOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(Message.RETURN_ORDER_NOT_FOUND));

        // 2. Cập nhật các thuộc tính cơ bản của ReturnOrder
        existingReturnOrder.setReturnReason(returnOrderRequestDto.getReturnReason());
        existingReturnOrder.setReturnDate(Instant.now()); // Cập nhật ngày trả hiện tại

        // 3. Xử lý các ReturnOrderItem hiện tại
        List<ReturnOrderItem> existingItems = existingReturnOrder.getReturnOrderItems();

        // Tạo map để dễ dàng tra cứu các item theo productId
        Map<Long, ReturnOrderItem> existingItemMap = existingItems.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), item -> item));

        // 4. Xử lý các ReturnOrderItem từ DTO
        for (ReturnOrderItemRequestDto itemRequestDto : returnOrderRequestDto.getReturnOrderItems()) {
            Long productId = itemRequestDto.getProductId();
            Integer newQuantityToReturn = itemRequestDto.getQuantity();
            Double unitPrice = itemRequestDto.getUnitPrice();
            String unit = itemRequestDto.getUnit();
            Integer conversionFactor = itemRequestDto.getConversionFactor();
            int smallestQuantityToReturn = newQuantityToReturn * conversionFactor;
            List<ReturnOrderBatchRequestDto> batchRequestDtos = itemRequestDto.getBatchRequestDtos();

            // Lấy sản phẩm từ repository
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

            if (existingItemMap.containsKey(productId)) {
                // 4.a. Cập nhật ReturnOrderItem hiện tại
                ReturnOrderItem existingReturnOrderItem = existingItemMap.get(productId);
                existingItemMap.remove(productId); // Loại bỏ khỏi map để xử lý các item bị xóa sau này

                // Cập nhật các thuộc tính của ReturnOrderItem
                existingReturnOrderItem.setQuantity(newQuantityToReturn);
                existingReturnOrderItem.setUnitPrice(unitPrice);
                existingReturnOrderItem.setTotalAmount(unitPrice * newQuantityToReturn);
                existingReturnOrderItem.setUnit(unit);
                existingReturnOrderItem.setConversionFactor(conversionFactor);
                returnOrderItemRepository.save(existingReturnOrderItem);

                // Xử lý các batch liên quan
                for (ReturnOrderBatchRequestDto batchRequestDto : batchRequestDtos) {
                    String batchNumber = batchRequestDto.getBatchNumber();
                    String invoiceNumber = batchRequestDto.getInvoiceNumber();
                    int newBatchQuantity = batchRequestDto.getQuantity();

                    // Tìm ImportItem dựa trên batchNumber, invoiceNumber, và productId
                    ImportItem importItem = importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId(
                            batchNumber,
                            productId,
                            invoiceNumber
                    ).orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND));

                    // Tìm SaleOrderItem từ SaleOrder và Product
                    SaleOrderItem saleOrderItem = saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(
                            existingReturnOrder.getSaleOrder().getId(),
                            productId
                    ).orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND));

                    // Tìm SaleOrderItemBatch dựa trên SaleOrderItem và ImportItem
                    SaleOrderItemBatch saleOrderItemBatch = saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(
                            saleOrderItem,
                            importItem
                    ).orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND));

                    int existingBatchQuantity = saleOrderItemBatch.getReturnedQuantity();
                    int quantityDifference = newBatchQuantity * conversionFactor - existingBatchQuantity;

                    if (quantityDifference > 0) {
                        // Tăng returnedQuantity
                        int availableQuantityForReturn = saleOrderItemBatch.getQuantity() - saleOrderItemBatch.getReturnedQuantity();
                        if (availableQuantityForReturn < quantityDifference) {
                            throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
                        }

                        saleOrderItemBatch.setReturnedQuantity(saleOrderItemBatch.getReturnedQuantity() + quantityDifference);
                        saleOrderItemBatchRepository.save(saleOrderItemBatch);

                        // Cập nhật lại tồn kho trong ImportItem
                        importItem.setRemainingQuantity(importItem.getRemainingQuantity() + quantityDifference);
                        importItemRepository.save(importItem);

                        // Cập nhật tổng tồn kho của sản phẩm
                        product.setTotalQuantity(product.getTotalQuantity() + quantityDifference);
                        productRepository.save(product);
                    } else if (quantityDifference < 0) {
                        // Giảm returnedQuantity
                        int quantityToReduce = -quantityDifference;
                        if (saleOrderItemBatch.getReturnedQuantity() < quantityToReduce) {
                            throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
                        }

                        saleOrderItemBatch.setReturnedQuantity(saleOrderItemBatch.getReturnedQuantity() - quantityToReduce);
                        saleOrderItemBatchRepository.save(saleOrderItemBatch);

                        // Cập nhật lại tồn kho trong ImportItem
                        importItem.setRemainingQuantity(importItem.getRemainingQuantity() - quantityToReduce);
                        importItemRepository.save(importItem);

                        // Cập nhật tổng tồn kho của sản phẩm
                        product.setTotalQuantity(product.getTotalQuantity() - quantityToReduce);
                        productRepository.save(product);
                    }
                    // Nếu quantityDifference == 0, không cần làm gì
                }
            } else {
                // 4.b. Thêm mới ReturnOrderItem
                SaleOrder saleOrder = existingReturnOrder.getSaleOrder();

                // Tìm SaleOrderItem liên quan
                SaleOrderItem saleOrderItem = saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(
                        saleOrder.getId(),
                        productId
                ).orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND));

                // Tạo mới ReturnOrderItem
                ReturnOrderItem returnOrderItem = new ReturnOrderItem();
                returnOrderItem.setReturnOrder(existingReturnOrder);
                returnOrderItem.setProduct(product);
                returnOrderItem.setQuantity(newQuantityToReturn);
                returnOrderItem.setUnitPrice(unitPrice);
                returnOrderItem.setTotalAmount(unitPrice * newQuantityToReturn);
                returnOrderItem.setUnit(unit);
                returnOrderItem.setConversionFactor(conversionFactor);
                returnOrderItemRepository.save(returnOrderItem);

                // Xử lý các batch liên quan
                int remainingQuantityToReturn = smallestQuantityToReturn;
                for (ReturnOrderBatchRequestDto batchRequestDto : batchRequestDtos) {
                    if (remainingQuantityToReturn <= 0) {
                        break;
                    }

                    String batchNumber = batchRequestDto.getBatchNumber();
                    String invoiceNumber = batchRequestDto.getInvoiceNumber();
                    int batchQuantity = batchRequestDto.getQuantity() * conversionFactor;

                    // Tìm ImportItem dựa trên batchNumber, invoiceNumber, và productId
                    ImportItem importItem = importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId(
                            batchNumber,
                            productId,
                            invoiceNumber
                    ).orElseThrow(() -> new ResourceNotFoundException(Message.IMPORT_ITEM_NOT_FOUND));

                    // Tìm SaleOrderItemBatch dựa trên SaleOrderItem và ImportItem
                    SaleOrderItemBatch saleOrderItemBatch = saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(
                            saleOrderItem,
                            importItem
                    ).orElseThrow(() -> new ResourceNotFoundException(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND));

                    int availableQuantityForReturn = saleOrderItemBatch.getQuantity() - saleOrderItemBatch.getReturnedQuantity();
                    if (availableQuantityForReturn < batchQuantity) {
                        throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
                    }

                    // Cập nhật returnedQuantity
                    saleOrderItemBatch.setReturnedQuantity(saleOrderItemBatch.getReturnedQuantity() + batchQuantity);
                    saleOrderItemBatch.setReturnOrderItem(returnOrderItem);
                    saleOrderItemBatchRepository.save(saleOrderItemBatch);

                    // Cập nhật lại tồn kho trong ImportItem
                    importItem.setRemainingQuantity(importItem.getRemainingQuantity() + batchQuantity);
                    importItemRepository.save(importItem);

                    // Cập nhật tổng tồn kho của sản phẩm
                    product.setTotalQuantity(product.getTotalQuantity() + batchQuantity);
                    productRepository.save(product);

                    remainingQuantityToReturn -= batchQuantity;
                }

                if (remainingQuantityToReturn > 0) {
                    throw new BadRequestException(Message.INVALID_RETURN_QUANTITY);
                }

                // Thêm ReturnOrderItem vào danh sách của ReturnOrder
                existingReturnOrder.getReturnOrderItems().add(returnOrderItem);
            }
        }

        // Sau khi xử lý các ReturnOrderItem từ DTO, tính lại tổng refundAmount
        existingReturnOrder.setRefundAmount(returnOrderRequestDto.getTotalAmount());

        // Lưu lại ReturnOrder với thông tin cập nhật
        returnOrderRepository.save(existingReturnOrder);

        // Cập nhật trạng thái của SaleOrder nếu cần
        SaleOrder saleOrder = existingReturnOrder.getSaleOrder();
        saleOrder.setCheckBackOrder(true);

        saleOrderRepository.save(saleOrder);
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
                            .map(saleOrderItemBatch -> {
                                // Lấy SaleOrderItem tương ứng với SaleOrderItemBatch hiện tại
                                Optional<SaleOrderItem> saleOrderItemOpt = saleOrderItemRepository.findBySaleOrderItemBatches_Id(saleOrderItemBatch.getId());

                                // Lấy discount từ SaleOrderItem nếu tồn tại
                                Double discount = saleOrderItemOpt.map(SaleOrderItem::getDiscount).orElse(0.0);

                                return new SaleOrderItemBatchResponseDto(
                                        saleOrderItemBatch.getImportItem().getBatchNumber(),
                                        saleOrderItemBatch.getQuantity(),
                                        saleOrderItemBatch.getReturnedQuantity() / returnOrderItem.getConversionFactor(),
                                        saleOrderItemBatch.getImportItem().getImportReceipt().getInvoiceNumber(),
                                        discount
                                );
                            })
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
