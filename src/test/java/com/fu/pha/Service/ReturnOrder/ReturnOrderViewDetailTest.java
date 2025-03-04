package com.fu.pha.Service.ReturnOrder;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.dto.response.ReturnOrderItemResponseDto;
import com.fu.pha.dto.response.ReturnOrderResponseDto;
import com.fu.pha.dto.response.SaleOrderItemBatchResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ReturnOrderItemRepository;
import com.fu.pha.repository.ReturnOrderRepository;
import com.fu.pha.repository.SaleOrderItemBatchRepository;
import com.fu.pha.repository.SaleOrderItemRepository;
import com.fu.pha.service.impl.ReturnOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReturnOrderViewDetailTest {

    @Mock
    private ReturnOrderRepository returnOrderRepository;

    @Mock
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Mock
    private SaleOrderItemBatchRepository saleOrderItemBatchRepository;

    @Mock
    private SaleOrderItemRepository saleOrderItemRepository; // Thêm mock này

    @InjectMocks
    private ReturnOrderServiceImpl returnOrderService;

    private ReturnOrder returnOrder;
    private ReturnOrderItem returnOrderItem;
    private SaleOrderItemBatch saleOrderItemBatch;
    private Product product; // Thêm product để tránh NPE
    private Category category; // Thêm category để thiết lập cho product

    @BeforeEach
    public void setUp() {
        // Initialize Category Mock
        category = new Category();
        category.setId(1L);
        category.setCategoryName("Category A");

        // Initialize Product Mock
        product = new Product();
        product.setId(1L);
        product.setProductName("Product A");
        product.setCategoryId(category); // Thiết lập categoryId cho product
        // Thiết lập các trường khác của product nếu cần

        // Initialize ReturnOrderItem Mock
        returnOrderItem = new ReturnOrderItem();
        returnOrderItem.setId(1L);
        returnOrderItem.setConversionFactor(1);
        returnOrderItem.setProduct(product); // Thiết lập product cho returnOrderItem

        // Initialize ReturnOrder Mock
        returnOrder = new ReturnOrder();
        returnOrder.setId(1L);
        returnOrder.setReturnOrderItems(Collections.singletonList(returnOrderItem)); // Thiết lập returnOrderItems
        returnOrderItem.setReturnOrder(returnOrder); // Liên kết ngược lại

        // Initialize SaleOrderItemBatch Mock
        saleOrderItemBatch = new SaleOrderItemBatch();
        ImportItem importItem = new ImportItem();
        importItem.setBatchNumber("BATCH123");
        Import importReceipt = new Import();
        importReceipt.setInvoiceNumber("INV123");
        importItem.setImportReceipt(importReceipt);
        saleOrderItemBatch.setImportItem(importItem);
        saleOrderItemBatch.setQuantity(10);
        saleOrderItemBatch.setReturnedQuantity(5);
    }

    // Test case lấy thông tin chi tiết phiếu trả hàng không thành công
    @Test
    public void UTCSOVD02() {
        // Arrange
        when(returnOrderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            returnOrderService.getReturnOrderById(1L);
        });

        assertEquals("ReturnOrder not found", exception.getMessage());
    }
}
