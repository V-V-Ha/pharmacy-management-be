package com.fu.pha.Service.Export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ExportType;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExportCreateTest {

    @Mock
    private ExportSlipRepository exportSlipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @Mock
    private ExportSlipItemRepository exportSlipItemRepository; // Add this mock

    @InjectMocks
    private ExportSlipServiceImpl exportService;

    private ExportSlipRequestDto exportDto;
    private User user;
    private Supplier supplier;
    private Product product;
    private ImportItem importItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Traphaco");

        product = new Product();
        product.setId(1L);
        product.setTotalQuantity(100);

        importItem = new ImportItem();
        importItem.setId(1L);
        importItem.setRemainingQuantity(50);
        Import importReceipt = new Import();
        importReceipt.setSupplier(supplier);
        importItem.setImportReceipt(importReceipt);

        exportDto = new ExportSlipRequestDto();
        exportDto.setUserId(1L);
        exportDto.setSupplierId(1L);
        exportDto.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);

        ExportSlipItemRequestDto exportItem = new ExportSlipItemRequestDto();
        exportItem.setProductId(1L);
        exportItem.setImportItemId(1L);
        exportItem.setQuantity(10);
        exportItem.setConversionFactor(1);
        exportItem.setUnitPrice(100.0);
        exportItem.setDiscount(0.0);
        exportItem.setTotalAmount(1000.0);
        exportItem.setBatchNumber("342");
        exportItem.setExpiryDate(Instant.now());
        exportItem.setUnit("Chai");

        exportDto.setExportSlipItems(Collections.singletonList(exportItem));
    }

    @Test
    void testCreateExport_ReturnToSupplier_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));
        when(exportSlipRepository.getLastInvoiceNumber()).thenReturn(null);

        assertDoesNotThrow(() -> exportService.createExport(exportDto));

        // Kiểm tra rằng exportSlipRepository.save được gọi 2 lần
        verify(exportSlipRepository, times(2)).save(any(ExportSlip.class));
    }

    @Test
    void testCreateExport_Destroy_Success() {
        exportDto.setTypeDelivery(ExportType.DESTROY);
        exportDto.setSupplierId(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));
        when(exportSlipRepository.getLastInvoiceNumber()).thenReturn(null);

        assertDoesNotThrow(() -> exportService.createExport(exportDto));

        // Kiểm tra rằng exportSlipRepository.save được gọi đúng 2 lần
        verify(exportSlipRepository, times(2)).save(any(ExportSlip.class));
    }

    @Test
    void testCreateExport_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCreateExport_SupplierNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCreateExport_InvalidExportType() {
        exportDto.setTypeDelivery(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.INVALID_EXPORT_TYPE, exception.getMessage());
    }

    @Test
    void testCreateExport_EmptyExportItems() {
        exportDto.setExportSlipItems(Collections.emptyList());
        exportDto.setTypeDelivery(ExportType.DESTROY); // Đặt kiểu là DESTROY để tránh kiểm tra Supplier

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.EXPORT_ITEMS_EMPTY, exception.getMessage());
    }

    @Test
    void testCreateExport_ProductNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCreateExport_NotEnoughStock() {
        product.setTotalQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.NOT_ENOUGH_STOCK, exception.getMessage());
    }

    @Test
    void testCreateExport_ImportNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testCreateExport_SupplierNotMatch() {
        Supplier differentSupplier = new Supplier();
        differentSupplier.setId(2L);
        importItem.getImportReceipt().setSupplier(differentSupplier);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.SUPPLIER_NOT_MATCH, exception.getMessage());
    }

    @Test
    void testCreateExport_NotEnoughStockInBatch() {
        importItem.setRemainingQuantity(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.NOT_ENOUGH_STOCK_IN_BATCH, exception.getMessage());
    }

    @Test
    void testCreateExport_TotalAmountNotMatch() {
        exportDto.setTotalAmount(500.0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());
    }

}
