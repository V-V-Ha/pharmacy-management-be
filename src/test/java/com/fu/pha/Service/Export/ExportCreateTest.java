package com.fu.pha.Service.Export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.ExportType;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.NotificationService;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ExportCreateTest {
    @InjectMocks
    private ExportSlipServiceImpl exportService; // Sử dụng lớp thực thi cụ thể

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExportSlipRepository exportSlipRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @Mock
    private ExportSlipItemRepository exportSlipItemRepository; // Mock thêm ExportSlipItemRepository

    @Mock
    private GenerateCode generateCode; // Giả sử đây là một dependency

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository; // Giả sử đây là một dependency

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ImportRepository importRepository;

    // Các đối tượng dùng chung
    private User mockUser;
    private Supplier mockSupplier;
    private Product mockProduct;
    private ImportItem mockImportItem;
    private ExportSlipRequestDto exportDto;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ExportSlipServiceImpl exportSlipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setRoles(new HashSet<>(Arrays.asList(new Role(ERole.ROLE_PRODUCT_OWNER.name()))));

        // Configure SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        // Configure UserRepository
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // Configure ExportSlipRepository
        lenient().when(exportSlipRepository.getLastInvoiceNumber()).thenReturn("EX000001");
        lenient().when(generateCode.generateNewProductCode("EX000001")).thenReturn("EX000002");

        // Initialize mock supplier
        mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        lenient().when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));

        // Initialize mock product
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setTotalQuantity(100);
        lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // Initialize mock import receipt
        Import mockImportReceipt = new Import();
        mockImportReceipt.setSupplier(mockSupplier);

        // Initialize mock import item
        mockImportItem = new ImportItem();
        mockImportItem.setId(1L);
        mockImportItem.setRemainingQuantity(50);
        mockImportItem.setImportReceipt(mockImportReceipt);
        lenient().when(importItemRepository.findById(1L)).thenReturn(Optional.of(mockImportItem));

        // Initialize valid ExportSlipItemRequestDto
        ExportSlipItemRequestDto itemDto = new ExportSlipItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(10);
        itemDto.setUnitPrice(100.0);
        itemDto.setUnit("pcs");
        itemDto.setDiscount(5.0);
        itemDto.setBatchNumber("BATCH001");
        itemDto.setImportItemId(1L);
        itemDto.setConversionFactor(1);
        itemDto.setTotalAmount(950.0); // 100 * 10 - 5%

        exportDto = new ExportSlipRequestDto();
        exportDto.setInvoiceNumber("INV0001");
        exportDto.setExportDate(Instant.now());
        exportDto.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);
        exportDto.setDiscount(5.0);
        exportDto.setTotalAmount(950.0);
        exportDto.setNote("Test note");
        exportDto.setUserId(1L);
        exportDto.setSupplierId(1L);
        exportDto.setExportSlipItems(Arrays.asList(itemDto));
        exportDto.setProductCount(1L);
        exportDto.setStatus("CONFIRMED");
}

    @Test
    void createExport_UnauthenticatedUser_ThrowsUnauthorizedException() {
        // Arrange: Người dùng không được xác thực
        lenient().when(securityContext.getAuthentication()).thenReturn(null); // Lenient để bỏ qua kiểm tra không cần thiết

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.NOT_LOGIN, exception.getMessage()); // Kiểm tra thông báo lỗi
        verify(exportSlipRepository, never()).save(any(ExportSlip.class)); // Đảm bảo không có export được lưu
    }

    @Test
    void createExport_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange: Không tìm thấy người dùng
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_InvalidExportType_ThrowsBadRequestException() {
        exportDto.setTypeDelivery(null);
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.INVALID_EXPORT_TYPE, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_EmptyExportSlipItems_ThrowsBadRequestException() {
        exportDto.setExportSlipItems(Collections.emptyList());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.EXPORT_ITEMS_EMPTY, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange: Sản phẩm không tồn tại
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_ImportItemNotFound_ThrowsResourceNotFoundException() {
        // Arrange: ImportItem không tồn tại
        when(importItemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }


}
