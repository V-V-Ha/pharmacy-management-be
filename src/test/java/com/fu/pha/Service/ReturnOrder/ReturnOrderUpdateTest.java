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

        saleOrder = new SaleOrder();
        saleOrder.setId(1L);
        saleOrder.setCustomer(new Customer());

        product = new Product();
        product.setId(1L);
        product.setTotalQuantity(10);

        importItem = new ImportItem();
        importItem.setBatchNumber("BATCH123");
        importItem.setRemainingQuantity(5);

        saleOrderItem = new SaleOrderItem();
        saleOrderItem.setId(1L);
        saleOrderItem.setProduct(product);

        saleOrderItemBatch = new SaleOrderItemBatch();
        saleOrderItemBatch.setSaleOrderItem(saleOrderItem);
        saleOrderItemBatch.setImportItem(importItem);
        saleOrderItemBatch.setQuantity(2);
        saleOrderItemBatch.setReturnedQuantity(0);

        existingReturnOrder = new ReturnOrder();
        existingReturnOrder.setId(1L);
        existingReturnOrder.setSaleOrder(saleOrder);
        existingReturnOrder.setReturnOrderItems(Collections.singletonList(new ReturnOrderItem()));
    }

    @Test
    public void testUpdateReturnOrder_Success() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId("BATCH123", 1L, "INV123"))
                .thenReturn(Optional.of(importItem));
        when(saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(1L, 1L))
                .thenReturn(Optional.of(saleOrderItem));
        when(saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(saleOrderItem, importItem))
                .thenReturn(Optional.of(saleOrderItemBatch));

        returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);

        verify(returnOrderRepository, times(1)).save(existingReturnOrder);
        verify(returnOrderItemRepository, times(1)).save(any(ReturnOrderItem.class));
        verify(saleOrderItemBatchRepository, times(1)).save(saleOrderItemBatch);
        verify(importItemRepository, times(1)).save(importItem);
        verify(productRepository, times(1)).save(product);
        verify(saleOrderRepository, times(1)).save(saleOrder);
    }

    @Test
    public void testUpdateReturnOrder_ReturnOrderNotFound() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.RETURN_ORDER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testUpdateReturnOrder_ProductNotFound() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testUpdateReturnOrder_SaleOrderItemBatchNotFound() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId("BATCH123", 1L, "INV123"))
                .thenReturn(Optional.of(importItem));
        when(saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(1L, 1L))
                .thenReturn(Optional.of(saleOrderItem));
        when(saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(saleOrderItem, importItem))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.SALE_ORDER_ITEM_BATCH_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testUpdateReturnOrder_InvalidReturnQuantity() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(existingReturnOrder));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findByBatchNumberAndImportReceipt_InvoiceNumberAndProductId("BATCH123", 1L, "INV123"))
                .thenReturn(Optional.of(importItem));
        when(saleOrderItemRepository.findBySaleOrderIdAndProductIdOrderById(1L, 1L))
                .thenReturn(Optional.of(saleOrderItem));
        when(saleOrderItemBatchRepository.findBySaleOrderItemAndImportItem(saleOrderItem, importItem))
                .thenReturn(Optional.of(saleOrderItemBatch));

        returnOrderRequestDto.getReturnOrderItems().get(0).setQuantity(10);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            returnOrderService.updateReturnOrder(1L, returnOrderRequestDto);
        });

        assertEquals(Message.INVALID_RETURN_QUANTITY, exception.getMessage());
    }
}
