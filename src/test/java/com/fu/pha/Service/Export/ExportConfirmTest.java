package com.fu.pha.Service.Export;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.repository.*;
import com.fu.pha.service.NotificationService;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ExportConfirmTest {

    @Mock
    private ExportSlipRepository exportSlipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ExportSlipServiceImpl exportSlipService;

    private User currentUser;
    private ExportSlip exportSlip;
    private ExportSlipItemRequestDto exportSlipItemRequestDto;
    private ExportSlipRequestDto exportDto;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Mock SecurityContext and Authentication
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user");

        // Initialize mock user with ROLE_PRODUCT_OWNER
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("user");
        currentUser.setRoles(new HashSet<>(Arrays.asList(new Role(ERole.ROLE_PRODUCT_OWNER.name()))));

        // Mock userRepository.findByUsername("user") to return currentUser
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(currentUser));

        // Initialize ExportSlip
        exportSlip = new ExportSlip();
        exportSlip.setId(1L);
        exportSlip.setStatus(OrderStatus.PENDING);
        exportSlip.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);
        Supplier mockSupplier = new Supplier();
        mockSupplier.setId(1L);
        exportSlip.setSupplier(mockSupplier);
        exportSlip.setExportSlipItemList(new ArrayList<>());

        // **Thêm dòng này để đảm bảo exportSlip.getUser() không null**
        exportSlip.setUser(currentUser);

        // Initialize ExportSlipItemRequestDto (không sử dụng trong confirmExport, có thể loại bỏ nếu không cần)
        exportSlipItemRequestDto = new ExportSlipItemRequestDto();
        exportSlipItemRequestDto.setProductId(1L);
        exportSlipItemRequestDto.setQuantity(10);
        exportSlipItemRequestDto.setUnitPrice(100.0);
        exportSlipItemRequestDto.setUnit("Hộp");
        exportSlipItemRequestDto.setDiscount(5.0);
        exportSlipItemRequestDto.setBatchNumber("BATCH001");
        exportSlipItemRequestDto.setImportItemId(1L);
        exportSlipItemRequestDto.setConversionFactor(1);
        exportSlipItemRequestDto.setTotalAmount(950.0); // 100 * 10 - 5%

        // Initialize ExportSlipRequestDto
        exportDto = new ExportSlipRequestDto();
        exportDto.setInvoiceNumber("INV0001");
        exportDto.setExportDate(Instant.now());
        exportDto.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);
        exportDto.setDiscount(5.0);
        exportDto.setTotalAmount(950.0);
        exportDto.setNote("abc");
        exportDto.setUserId(1L);
        exportDto.setSupplierId(1L);
        exportDto.setExportSlipItems(Arrays.asList(exportSlipItemRequestDto));
        exportDto.setProductCount(1L);
        exportDto.setStatus("CONFIRMED");
    }

    // Test case không tìm thấy người dùng
    @Test
    void UTCECF01() {
        // Arrange: User not found
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.confirmExport(200L);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());

        // **Xác minh rằng exportSlipRepository.findById không được gọi**
        verify(exportSlipRepository, never()).findById(anyLong());

        // **Xác minh rằng notificationService.sendNotificationToUser không được gọi**
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }

    // Test case user không có quyền xác nhận phiếu xuất
    @Test
    void UTCECF02() {
        // Arrange: Change user's roles to not include PRODUCT_OWNER
        currentUser.setRoles(new HashSet<>(Arrays.asList(new Role(ERole.ROLE_STOCK.name()))));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(currentUser));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            exportSlipService.confirmExport(1L);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());

        // **Xác minh rằng exportSlipRepository.findById không được gọi**
        verify(exportSlipRepository, never()).findById(anyLong());

        // **Xác minh rằng notificationService.sendNotificationToUser không được gọi**
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }

    // Test case ExportSlip not found
    @Test
    void UTCECF03() {
        // Arrange: ExportSlip not found
        when(exportSlipRepository.findById(200L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.confirmExport(200L);
        });

        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());

        // **Xác minh rằng notificationService.sendNotificationToUser không được gọi**
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }

    // Test case ExportSlip status is not PENDING
    @Test
    void UTCECF04() {
        // Arrange: ExportSlip status is not PENDING
        exportSlip.setStatus(OrderStatus.CONFIRMED);
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipService.confirmExport(1L);
        });

        assertEquals(Message.NOT_PENDING_EXPORT, exception.getMessage());

        // **Xác minh rằng notificationService.sendNotificationToUser không được gọi**
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }

    // Test case successful confirmExport
    @Test
    void UTCECF05() {
        // Arrange: ExportSlip found and status is PENDING
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        // Act: Call the method
        exportSlipService.confirmExport(1L);

        // Assert: Verify status update and save
        assertEquals(OrderStatus.CONFIRMED, exportSlip.getStatus());
        verify(exportSlipRepository, times(1)).save(exportSlip);

        // **Xác minh rằng notificationService.sendNotificationToUser được gọi đúng cách**
        verify(notificationService, times(1)).sendNotificationToUser(
                eq("Phiếu xuất đã được xác nhận"),
                eq("Phiếu xuất của bạn đã được chủ cửa hàng xác nhận."),
                eq(exportSlip.getUser()),
                eq("/export/receipt/detail/" + exportSlip.getId())
        );
    }
}
