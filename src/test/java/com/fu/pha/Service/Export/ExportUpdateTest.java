package com.fu.pha.Service.Export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ExportType;
import com.fu.pha.exception.Message;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExportUpdateTest {

    @InjectMocks
    private ExportSlipServiceImpl exportService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @Mock
    private ExportSlipRepository exportSlipRepository;

    @Mock
    private ExportSlipItemRepository exportSlipItemRepository;

    private ExportSlipRequestDto exportDto;
    private ExportSlip exportSlip;
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

        exportSlip = new ExportSlip();
        exportSlip.setId(1L);
        exportSlip.setUser(user);
        exportSlip.setSupplier(supplier);
        exportSlip.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);

        // Tạo ExportSlipItemRequestDto cho exportDto
        ExportSlipItemRequestDto exportSlipItem = new ExportSlipItemRequestDto();
        exportSlipItem.setProductId(1L);
        exportSlipItem.setImportItemId(1L);
        exportSlipItem.setQuantity(10);
        exportSlipItem.setConversionFactor(1);
        exportSlipItem.setUnitPrice(100.0);
        exportSlipItem.setDiscount(0.0);
        exportSlipItem.setTotalAmount(1000.0);
        exportSlipItem.setBatchNumber("342");
        exportSlipItem.setExpiryDate(Instant.now());
        exportSlipItem.setUnit("Chai");

        // Khởi tạo danh sách exportSlipItems và gán cho exportDto
        exportDto = new ExportSlipRequestDto();
        exportDto.setUserId(1L);
        exportDto.setSupplierId(1L);
        exportDto.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);
        exportDto.setExportSlipItems(Collections.singletonList(exportSlipItem));
    }


    // 1. Success case: Update Export Slip with type Return to Supplier
    @Test
    void testUpdateExport_ReturnToSupplier_Success() {
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        assertDoesNotThrow(() -> exportService.updateExport(1L, exportDto));
        verify(exportSlipRepository, times(1)).save(exportSlip);
    }

    // 2. Success case: Update Export Slip with type Destroy
    @Test
    void testUpdateExport_Destroy_Success() {
        exportDto.setTypeDelivery(ExportType.DESTROY);
        exportDto.setSupplierId(null);

        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        assertDoesNotThrow(() -> exportService.updateExport(1L, exportDto));
        verify(exportSlipRepository, times(1)).save(exportSlip);
    }

    // 3. Not Found Export Slip
    @Test
    void testUpdateExport_NotFoundExportSlip() {
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());
    }

    // 4. Not Found User
    @Test
    void testUpdateExport_NotFoundUser() {
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // 5. Not Found Supplier for Return to Supplier
    @Test
    void testUpdateExport_NotFoundSupplier() {
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }

    // 6. Invalid Export Type
    @Test
    void testUpdateExport_InvalidExportType() {
        exportDto.setTypeDelivery(null);

        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.INVALID_EXPORT_TYPE, exception.getMessage());
    }

    // 7. Not Found Product
    @Test
    void testUpdateExport_NotFoundProduct() {
        // Mock dữ liệu cần thiết để tránh lỗi ResourceNotFoundException ở Supplier
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier)); // Mock supplier
        when(productRepository.findById(1L)).thenReturn(Optional.empty()); // Mock product không tồn tại

        // Thực hiện kiểm thử và xác minh kết quả
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    // 8. Not Enough Stock for Product
    @Test
    void testUpdateExport_NotEnoughStock() {
        // Đặt số lượng yêu cầu vượt quá tồn kho hiện tại
        exportDto.getExportSlipItems().get(0).setQuantity(200);

        // Mock dữ liệu cần thiết
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier)); // Mock supplier để tránh lỗi NotFoundException
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Thực hiện kiểm thử và xác minh kết quả
        BadRequestException exception = assertThrows(BadRequestException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.NOT_ENOUGH_STOCK, exception.getMessage());
    }


    // 9. Not Found Import Item
    @Test
    void testUpdateExport_NotFoundImportItem() {
        // Mock các dependencies
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // Mock sản phẩm để không bị lỗi không tìm thấy sản phẩm

        // Giả lập trường hợp không tìm thấy ImportItem
        when(importItemRepository.findById(1L)).thenReturn(Optional.empty());

        // Thực hiện kiểm tra
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
    }

    // 10. Supplier Mismatch for Return to Supplier
    @Test
    void testUpdateExport_SupplierMismatch() {
        // Tạo một Supplier khác để gây lỗi mismatch
        Supplier differentSupplier = new Supplier();
        differentSupplier.setId(2L);
        importItem.getImportReceipt().setSupplier(differentSupplier);

        // Thiết lập các giá trị trả về từ các repository cần thiết
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // Mock product để tránh lỗi ResourceNotFoundException
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem)); // Mock importItem để có dữ liệu ImportItem hợp lệ

        // Kiểm tra ngoại lệ BadRequestException khi nhà cung cấp không khớp
        BadRequestException exception = assertThrows(BadRequestException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.SUPPLIER_NOT_MATCH, exception.getMessage());
    }


    // 11. Not Enough Stock in Batch for Import Item
    @Test
    void testUpdateExport_NotEnoughStockInBatch() {
        // Đảm bảo số lượng remaining trong importItem nhỏ hơn yêu cầu để kích hoạt lỗi
        importItem.setRemainingQuantity(5);

        // Mock các repository cần thiết
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier)); // Mock supplier để tránh lỗi ResourceNotFoundException
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // Mock product nếu cần
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        // Kiểm tra ngoại lệ
        BadRequestException exception = assertThrows(BadRequestException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.NOT_ENOUGH_STOCK_IN_BATCH, exception.getMessage());
    }

    // 12. Total Amount Mismatch for Non-Destroy Type
    @Test
    void testUpdateExport_TotalAmountMismatch() {
        // Đặt totalAmount không khớp với giá trị dự kiến
        exportDto.setTotalAmount(2000.0);

        // Mock các repository cần thiết
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier)); // Mock supplier để tránh lỗi ResourceNotFoundException
        when(productRepository.findById(1L)).thenReturn(Optional.of(product)); // Mock product nếu cần
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        // Kiểm tra ngoại lệ
        BadRequestException exception = assertThrows(BadRequestException.class, () -> exportService.updateExport(1L, exportDto));
        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());
    }

}
