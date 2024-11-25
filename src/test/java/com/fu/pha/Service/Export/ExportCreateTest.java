package com.fu.pha.Service.Export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

    // Các đối tượng dùng chung
    private User mockUser;
    private Supplier mockSupplier;
    private Product mockProduct;
    private ImportItem mockImportItem;
    private ExportSlipRequestDto exportDto;

    @BeforeEach
    void setUp() {
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
        itemDto.setExpiryDate(Instant.now().plusSeconds(86400));
        itemDto.setImportItemId(1L);
        itemDto.setConversionFactor(1);
        itemDto.setTotalAmount(950.0); // 100 * 10 - 5%

        exportDto = new ExportSlipRequestDto(
                null,
                "INV0001",
                Instant.now(),
                ExportType.RETURN_TO_SUPPLIER,
                5.0,
                950.0,
                "Test note",
                1L,
                1L,
                Arrays.asList(itemDto),
                1L,
                "CONFIRMED"
        );

        // Mock additional repositories if needed
        // Example:
        // lenient().when(exportSlipItemRepository.save(any(ExportSlipItem.class))).thenReturn(new ExportSlipItem());
    }

    // Phương thức helper để tạo ExportSlipRequestDto với tùy chỉnh
    private ExportSlipRequestDto createExportSlipRequestDto(ExportType typeDelivery, Double discount, Double totalAmount, Long supplierId, boolean addItems) {
        ExportSlipRequestDto dto = new ExportSlipRequestDto(
                null,
                "INV0001",
                Instant.now(),
                typeDelivery,
                discount,
                totalAmount,
                "Test note",
                1L,
                supplierId,
                addItems ? exportDto.getExportSlipItems() : null,
                1L,
                "CONFIRMED"
        );
        return dto;
    }

    // Các phương thức kiểm thử

    @Test
    void createExport_SuccessWithConfirmedStatus() {
        // Act
        exportService.createExport(exportDto);

        // Assert
        ArgumentCaptor<ExportSlip> exportSlipCaptor = ArgumentCaptor.forClass(ExportSlip.class);
        verify(exportSlipRepository, times(2)).save(exportSlipCaptor.capture()); // Lần đầu lưu phiếu xuất và lần sau cập nhật totalAmount

        ExportSlip savedExportSlip = exportSlipCaptor.getAllValues().get(0);
        assertEquals("EX000002", savedExportSlip.getInvoiceNumber());
        assertEquals(ExportType.RETURN_TO_SUPPLIER, savedExportSlip.getTypeDelivery());
        assertEquals(5.0, savedExportSlip.getDiscount());
        assertEquals("Test note", savedExportSlip.getNote());
        assertEquals(mockUser, savedExportSlip.getUser());
        assertEquals(OrderStatus.CONFIRMED, savedExportSlip.getStatus());

        // Kiểm tra ExportSlipItem
        ArgumentCaptor<ExportSlipItem> exportSlipItemCaptor = ArgumentCaptor.forClass(ExportSlipItem.class);
        verify(productRepository).save(mockProduct);
        verify(importItemRepository).save(mockImportItem);
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));

        // Kiểm tra tổng tiền
        ExportSlip updatedExportSlip = exportSlipCaptor.getAllValues().get(1);
        assertEquals(950.0, updatedExportSlip.getTotalAmount());
    }

    @Test
    void createExport_SuccessWithPendingStatus() {
        // Arrange: Người dùng không có ROLE_PRODUCT_OWNER
        mockUser.setRoles(new HashSet<>(Arrays.asList(new Role(ERole.ROLE_STOCK.name()))));

        // Act
        exportService.createExport(exportDto);

        // Assert
        ArgumentCaptor<ExportSlip> exportSlipCaptor = ArgumentCaptor.forClass(ExportSlip.class);
        verify(exportSlipRepository, times(2)).save(exportSlipCaptor.capture());

        ExportSlip savedExportSlip = exportSlipCaptor.getAllValues().get(0);
        assertEquals(OrderStatus.PENDING, savedExportSlip.getStatus());

        // Kiểm tra rằng stock không được xử lý
        verify(productRepository, never()).save(any(Product.class));
        verify(importItemRepository, never()).save(any(ImportItem.class));
        verify(inventoryHistoryRepository, never()).save(any(InventoryHistory.class));

        // Kiểm tra tổng tiền
        ExportSlip updatedExportSlip = exportSlipCaptor.getAllValues().get(1);
        assertEquals(950.0, updatedExportSlip.getTotalAmount());
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
        // Arrange: Loại phiếu xuất không hợp lệ
        ExportSlipRequestDto invalidDto = createExportSlipRequestDto(null, 5.0, 950.0, 1L, true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(invalidDto);
        });

        assertEquals(Message.INVALID_EXPORT_TYPE, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_EmptyExportSlipItems_ThrowsBadRequestException() {
        // Arrange: Danh sách sản phẩm xuất rỗng
        ExportSlipRequestDto invalidDto = createExportSlipRequestDto(ExportType.RETURN_TO_SUPPLIER, 5.0, 950.0, 1L, false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(invalidDto);
        });

        assertEquals(Message.EXPORT_ITEMS_EMPTY, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_TotalAmountMissing_ThrowsBadRequestException() {
        // Arrange: Tổng tiền bị thiếu
        ExportSlipRequestDto invalidDto = createExportSlipRequestDto(ExportType.RETURN_TO_SUPPLIER, 5.0, null, 1L, true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(invalidDto);
        });

        assertEquals(Message.TOTAL_AMOUNT_REQUIRED, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_TotalAmountMismatch_ThrowsBadRequestException() {
        // Arrange: Tổng tiền không khớp
        ExportSlipRequestDto invalidDto = createExportSlipRequestDto(ExportType.RETURN_TO_SUPPLIER, 5.0, 1000.0, 1L, true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(invalidDto);
        });

        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
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

    @Test
    void createExport_SupplierMismatchForReturn_ThrowsBadRequestException() {
        // Arrange: Nhà cung cấp không khớp khi trả lại nhà cung cấp
        Supplier differentSupplier = new Supplier();
        differentSupplier.setId(2L);
        Import differentImportReceipt = new Import();
        differentImportReceipt.setSupplier(differentSupplier);
        mockImportItem.setImportReceipt(differentImportReceipt);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.SUPPLIER_NOT_MATCH, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_NotEnoughStock_ThrowsBadRequestException() {
        // Arrange: Số lượng tồn kho không đủ
        mockProduct.setTotalQuantity(50); // Số lượng hiện tại = 50, yêu cầu = 60 * 1 = 60 > 50
        exportDto.getExportSlipItems().get(0).setQuantity(60); // 60 * 1 = 60 > 50

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.NOT_ENOUGH_STOCK, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }

    @Test
    void createExport_NotEnoughStockInBatch_ThrowsBadRequestException() {
        // Arrange: Số lượng trong lô không đủ
        mockImportItem.setRemainingQuantity(50); // Số lượng còn lại = 50
        exportDto.getExportSlipItems().get(0).setQuantity(60); // 60 * 1 = 60 > 50

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportService.createExport(exportDto);
        });

        assertEquals(Message.NOT_ENOUGH_STOCK_IN_BATCH, exception.getMessage()); // Cập nhật thông điệp tiếng Việt
        //verify(exportSlipRepository, never()).save(any(ExportSlip.class));
    }
}
