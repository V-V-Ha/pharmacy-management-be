package com.fu.pha.Service.Import;

import com.fu.pha.dto.request.importPack.ImportViewListDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.dto.response.importPack.ImportResponseDto;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
public class ImportViewListTest {

    @Mock
    private ImportRepository importRepository;

    @InjectMocks
    private ImportService importService;

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
        importMock.setTotalAmount(5000.0);
        importMock.setStatus(OrderStatus.PENDING);

        // Tạo mock đối tượng User và Supplier
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("John Doe");
        importMock.setUser(mockUser);

        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        mockSupplier.setSupplierName("Supplier Name");
        importMock.setSupplier(mockSupplier);

        // Tạo các ImportItems
        ImportItem importItem1 = new ImportItem();
        importItem1.setProduct(new Product());
        importItem1.getProduct().setId(1L);
        importItem1.getProduct().setProductName("Product A");

        ImportItem importItem2 = new ImportItem();
        importItem2.setProduct(new Product());
        importItem2.getProduct().setId(2L);
        importItem2.getProduct().setProductName("Product B");

        importMock.setImportItems(Arrays.asList(importItem1, importItem2));
    }

    @Test
    public void testGetAllImportPaging_NoDateFilters() {
        // Arrange
        int page = 0;
        int size = 10;
        String supplierName = "Supplier Name";
        OrderStatus status = OrderStatus.PENDING;
        Instant fromDate = null;
        Instant toDate = null;

        List<Import> imports = Arrays.asList(importMock);


        // Act
        Page<ImportViewListDto> result = importService.getAllImportPaging(page, size, supplierName, status, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("INV123", result.getContent().get(0).getInvoiceNumber());
    }

    @Test
    public void testGetAllImportPaging_WithFromDateOnly() {
        // Arrange
        int page = 0;
        int size = 10;
        String supplierName = "Supplier Name";
        OrderStatus status = OrderStatus.PENDING;
        Instant fromDate = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant toDate = null;

        List<Import> imports = Arrays.asList(importMock);


        // Act
        Page<ImportViewListDto> result = importService.getAllImportPaging(page, size, supplierName, status, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("INV123", result.getContent().get(0).getInvoiceNumber());
    }

    @Test
    public void testGetAllImportPaging_WithToDateOnly() {
        // Arrange
        int page = 0;
        int size = 10;
        String supplierName = "Supplier Name";
        OrderStatus status = OrderStatus.PENDING;
        Instant fromDate = null;
        Instant toDate = Instant.now(); // Now

        List<Import> imports = Arrays.asList(importMock);


        // Act
        Page<ImportViewListDto> result = importService.getAllImportPaging(page, size, supplierName, status, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("INV123", result.getContent().get(0).getInvoiceNumber());
    }

    @Test
    public void testGetAllImportPaging_WithFromAndToDate() {
        // Arrange
        int page = 0;
        int size = 10;
        String supplierName = "Supplier Name";
        OrderStatus status = OrderStatus.PENDING;
        Instant fromDate = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant toDate = Instant.now();

        List<Import> imports = Arrays.asList(importMock);


        // Act
        Page<ImportViewListDto> result = importService.getAllImportPaging(page, size, supplierName, status, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("INV123", result.getContent().get(0).getInvoiceNumber());
    }

    @Test
    public void testGetAllImportPaging_NoImportsFound() {
        // Arrange
        int page = 0;
        int size = 10;
        String supplierName = "Supplier Name";
        OrderStatus status = OrderStatus.PENDING;
        Instant fromDate = null;
        Instant toDate = null;



        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            importService.getAllImportPaging(page, size, supplierName, status, fromDate, toDate);
        });

        assertEquals("Import not found", thrown.getMessage());
    }
}