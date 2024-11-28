package com.fu.pha.Service.Import;

import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ImportViewDetailTest {

    @Mock
    private ImportRepository importRepository;

    @InjectMocks
    private ImportServiceImpl importService;

    private Import importMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Tạo mock đối tượng Import
        importMock = new Import();
        importMock.setId(1L);
        importMock.setInvoiceNumber("INV123");
        importMock.setImportDate(Instant.now());
        importMock.setPaymentMethod(PaymentMethod.CASH);
        importMock.setTax(100.0);
        importMock.setDiscount(50.0);
        importMock.setTotalAmount(5000.0);
        importMock.setNote("Import note");

        // Tạo mock đối tượng User và Supplier
        User mockUser = new User();
        mockUser.setId(1L);
        importMock.setUser(mockUser);

        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        mockSupplier.setSupplierName("Supplier Name");
        importMock.setSupplier(mockSupplier);

        importMock.setCreateDate(Instant.now());
        importMock.setLastModifiedDate(Instant.now());
        importMock.setCreateBy("admin");
        importMock.setLastModifiedBy("admin");

        importMock.setStatus(OrderStatus.PENDING);
        importMock.setImage("image-url");

        // Tạo mock ImportItems
        ImportItem importItem1 = new ImportItem();
        importItem1.setProduct(new Product());
        importItem1.getProduct().setId(1L);
        importItem1.getProduct().setProductName("Product A");

        ImportItem importItem2 = new ImportItem();
        importItem2.setProduct(new Product());
        importItem2.getProduct().setId(2L);
        importItem2.getProduct().setProductName("Product B");

        List<ImportItem> importItems = Arrays.asList(importItem1, importItem2);
        importMock.setImportItems(importItems);
    }

    @Test
    public void testGetImportById_ImportExists() {
        // Arrange
        Long importId = 1L;
        when(importRepository.findById(importId)).thenReturn(Optional.of(importMock));

        // Act
        ImportResponseDto result = importService.getImportById(importId);

        // Assert
        assertNotNull(result);
        assertEquals(importMock.getId(), result.getId());
        assertEquals(importMock.getInvoiceNumber(), result.getInvoiceNumber());
        assertEquals(importMock.getImportDate(), result.getImportDate());
        assertEquals(importMock.getPaymentMethod(), result.getPaymentMethod());
        assertEquals(importMock.getTax(), result.getTax());
        assertEquals(importMock.getDiscount(), result.getDiscount());
        assertEquals(importMock.getTotalAmount(), result.getTotalAmount());
        assertEquals(importMock.getNote(), result.getNote());
        assertEquals(importMock.getSupplier().getSupplierName(), result.getSupplierName());
        assertEquals(importMock.getStatus(), result.getStatus());
        assertEquals(importMock.getImage(), result.getImageUrl());
    }

    @Test
    void UTCIVD02() {
        // Giả lập trường hợp không tìm thấy Import với ID 200
        when(importRepository.findById(200L)).thenReturn(Optional.empty());

        // Kiểm tra nếu ném ra ResourceNotFoundException khi gọi getImportById với ID 200
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.getImportById(200L);
        });

        // Xác nhận thông báo lỗi khớp với thông báo IMPORT_NOT_FOUND
        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());

        // Xác nhận phương thức findById của importRepository được gọi 1 lần với ID 200
        verify(importRepository, times(1)).findById(200L);
    }

}
