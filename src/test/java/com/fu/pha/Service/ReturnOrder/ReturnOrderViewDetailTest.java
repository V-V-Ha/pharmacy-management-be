package com.fu.pha.Service.ReturnOrder;

import com.fu.pha.dto.response.ReturnOrderItemResponseDto;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.dto.response.SaleOrderItemBatchResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ReturnOrderItemRepository;
import com.fu.pha.repository.ReturnOrderRepository;
import com.fu.pha.repository.SaleOrderItemBatchRepository;
import com.fu.pha.service.impl.ReturnOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReturnOrderViewDetailTest {

    @Mock
    private ReturnOrderRepository returnOrderRepository;

    @Mock
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Mock
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;

    @InjectMocks
    private ReturnOrderServiceImpl returnOrderService;

    private ReturnOrder returnOrder;
    private ReturnOrderItem returnOrderItem;
    private SaleOrderItemBatch saleOrderItemBatch;

    @BeforeEach
    public void setUp() {
        returnOrder = new ReturnOrder();
        returnOrder.setId(1L);

        returnOrderItem = new ReturnOrderItem();
        returnOrderItem.setId(1L);
        returnOrderItem.setReturnOrder(returnOrder);
        returnOrderItem.setConversionFactor(1);

        saleOrderItemBatch = new SaleOrderItemBatch();
        saleOrderItemBatch.setImportItem(new ImportItem());
        saleOrderItemBatch.getImportItem().setBatchNumber("BATCH123");
        saleOrderItemBatch.getImportItem().setImportReceipt(new Import());
        saleOrderItemBatch.getImportItem().getImportReceipt().setInvoiceNumber("INV123");
        saleOrderItemBatch.setQuantity(10);
        saleOrderItemBatch.setReturnedQuantity(5);
    }

    @Test
    public void testGetReturnOrderById_Success() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.of(returnOrder));
        when(returnOrderItemRepository.findByReturnOrderId(1L)).thenReturn(Collections.singletonList(returnOrderItem));
        when(saleOrderItemBatchRepository.findByReturnOrderItemId(1L)).thenReturn(Collections.singletonList(saleOrderItemBatch));

        ReturnOrderResponseDto result = returnOrderService.getReturnOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getReturnOrderItems().size());

        ReturnOrderItemResponseDto itemResponseDto = result.getReturnOrderItems().get(0);
        assertEquals(1, itemResponseDto.getBatchResponseDtos().size());

        SaleOrderItemBatchResponseDto batchResponseDto = itemResponseDto.getBatchResponseDtos().get(0);
        assertEquals("BATCH123", batchResponseDto.getBatchNumber());
        assertEquals(10, batchResponseDto.getQuantity());
        assertEquals(5, batchResponseDto.getReturnedQuantity());
        assertEquals("INV123", batchResponseDto.getInvoiceNumber());
    }

    @Test
    public void testGetReturnOrderById_ReturnOrderNotFound() {
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.getReturnOrderById(1L);
        });

        assertEquals("ReturnOrder not found", exception.getMessage());
    }
}
