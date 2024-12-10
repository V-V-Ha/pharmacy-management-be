package com.fu.pha.Service.ReturnOrder;

import com.fu.pha.dto.request.ReturnOrderBatchRequestDto;
import com.fu.pha.dto.request.ReturnOrderItemRequestDto;
import com.fu.pha.dto.request.ReturnOrderRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.impl.ReturnOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReturnOrderUpdateTest {

    @Mock
    private ReturnOrderRepository returnOrderRepository;

    @Mock
    private SaleOrderRepository saleOrderRepository;

    @Mock
    private SaleOrderItemRepository saleOrderItemRepository;

    @Mock
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;

    @Mock
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReturnOrderServiceImpl returnOrderService;

    private ReturnOrderRequestDto returnOrderRequestDto;
    private ReturnOrder existingReturnOrder;
    private SaleOrder saleOrder;
    private Product product;
    private ImportItem importItem;
    private SaleOrderItem saleOrderItem;
    private SaleOrderItemBatch saleOrderItemBatch;

    @BeforeEach
    public void setUp() {
        // Thiết lập ReturnOrderRequestDto
        returnOrderRequestDto = new ReturnOrderRequestDto();
        returnOrderRequestDto.setReturnReason("Defective product");
        returnOrderRequestDto.setTotalAmount(200.0);

        ReturnOrderItemRequestDto returnOrderItemRequestDto = new ReturnOrderItemRequestDto();
        returnOrderItemRequestDto.setProductId(1L);
        returnOrderItemRequestDto.setQuantity(2);
        returnOrderItemRequestDto.setUnitPrice(100.0);
        returnOrderItemRequestDto.setConversionFactor(1);
        returnOrderItemRequestDto.setUnit("pcs");

        ReturnOrderBatchRequestDto returnOrderBatchRequestDto = new ReturnOrderBatchRequestDto();
        returnOrderBatchRequestDto.setBatchNumber("BATCH123");
        returnOrderBatchRequestDto.setInvoiceNumber("INV123");
        returnOrderBatchRequestDto.setQuantity(2);

        returnOrderItemRequestDto.setBatchRequestDtos(Collections.singletonList(returnOrderBatchRequestDto));
        returnOrderRequestDto.setReturnOrderItems(Collections.singletonList(returnOrderItemRequestDto));

        // Thiết lập SaleOrder
        saleOrder = new SaleOrder();
        saleOrder.setId(1L);
        saleOrder.setCustomer(new Customer());

        // Thiết lập Product
        product = new Product();
        product.setId(1L);
        product.setTotalQuantity(10);

        // Thiết lập ImportItem
        importItem = new ImportItem();
        importItem.setBatchNumber("BATCH123");
        importItem.setRemainingQuantity(5);
        importItem.setProduct(product); // Thiết lập Product cho ImportItem

        // Thiết lập SaleOrderItem
        saleOrderItem = new SaleOrderItem();
        saleOrderItem.setId(1L);
        saleOrderItem.setProduct(product);

        // Thiết lập SaleOrderItemBatch
        saleOrderItemBatch = new SaleOrderItemBatch();
        saleOrderItemBatch.setSaleOrderItem(saleOrderItem);
        saleOrderItemBatch.setImportItem(importItem);
        saleOrderItemBatch.setQuantity(2);
        saleOrderItemBatch.setReturnedQuantity(0);

        // Thiết lập ReturnOrderItem với Product
        ReturnOrderItem existingReturnOrderItem = new ReturnOrderItem();
        existingReturnOrderItem.setId(1L);
        existingReturnOrderItem.setProduct(product); // Thiết lập Product cho ReturnOrderItem
        existingReturnOrderItem.setQuantity(1); // Thiết lập các thuộc tính khác nếu cần

        // Thiết lập ReturnOrder
        existingReturnOrder = new ReturnOrder();
        existingReturnOrder.setId(1L);
        existingReturnOrder.setSaleOrder(saleOrder);
        existingReturnOrder.setReturnOrderItems(Collections.singletonList(existingReturnOrderItem));
    }

    // Test case cập nhật phiếu trả hàng thành công
    @Test
    public void UTCSOC01() {
        // Thiết lập các mocks
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId("BATCH123",1L,"INV123"))
                .thenReturn(Optional.of(importItem)); // Đảm bảo đúng thứ tự tham số
        when(saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(1L, 1L))
                .thenReturn(Optional.of(saleOrderItem));
        when(saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(saleOrderItem, importItem))
                .thenReturn(Optional.of(saleOrderItemBatch));

        // Gọi phương thức cần kiểm thử
        returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);

        // Xác minh các tương tác
        verify(returnOrderRepository, times(1)).save(existingReturnOrder);
        verify(returnOrderItemRepository, times(1)).save(any(ReturnOrderItem.class));
        verify(saleOrderItemBatchRepository, times(1)).save(saleOrderItemBatch);
        verify(importItemRepository, times(1)).save(importItem);
        verify(productRepository, times(1)).save(product);
        verify(saleOrderRepository, times(1)).save(saleOrder);
    }

    // Test case cập nhật phiếu trả hàng không thành công vì không tìm thấy phiếu trả hàng
    @Test
    public void UTCSOC02() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.RETURN_ORDER_NOT_FOUND, exception.getMessage());
    }

    // Test case cập nhật phiếu trả hàng không thành công vì không tìm thấy sản phẩm
    @Test
    public void UTCSOC03() {
        // Thiết lập các mocks
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Gọi phương thức và kiểm tra ngoại lệ
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    // Test case cập nhật phiếu trả hàng không thành công vì không tìm thấy lô hàng
    @Test
    public void UTCSOC04() {
        // Thiết lập các mocks
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId("BATCH123",1L,"INV123"))
                .thenReturn(Optional.of(importItem));
        when(saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(1L, 1L))
                .thenReturn(Optional.of(saleOrderItem));
        when(saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(saleOrderItem, importItem))
                .thenReturn(Optional.empty()); // Không tìm thấy SaleOrderItemBatch

        // Gọi phương thức và kiểm tra ngoại lệ
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND, exception.getMessage());
    }
}
