package com.fu.pha.Service.Import;

import com.fu.pha.enums.PaymentMethod;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
public class ImportRejectTest {
    @Mock
    private ImportRepository importRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ImportService importService;

    private Import importMock;
    private User currentUser;
    private User importCreator;

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

        // Tạo mock đối tượng User
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setFullName("John Doe");

        // Tạo mock đối tượng ImportCreator
        importCreator = new User();
        importCreator.setId(2L);
        importCreator.setFullName("Jane Smith");

        importMock.setUser(importCreator);
    }

    @Test
    public void testRejectImport_UserUnauthorized() {
        // Arrange
        Long importId = 1L;
        String reason = "Some reason";

        // Giả lập người dùng không có quyền ROLE_PRODUCT_OWNER
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        // Act & Assert
        UnauthorizedException thrown = assertThrows(UnauthorizedException.class, () -> {
            importService.rejectImport(importId, reason);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, thrown.getMessage());
    }

    @Test
    public void testRejectImport_ReasonRequired() {
        // Arrange
        Long importId = 1L;
        String reason = "";  // Lý do từ chối là chuỗi rỗng

        // Giả lập người dùng có quyền ROLE_PRODUCT_OWNER
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            importService.rejectImport(importId, reason);
        });

        assertEquals(Message.REASON_REQUIRED, thrown.getMessage());
    }

    @Test
    public void testRejectImport_ImportNotFound() {
        // Arrange
        Long importId = 1L;
        String reason = "Some reason";

        // Giả lập người dùng có quyền ROLE_PRODUCT_OWNER
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.empty()); // Không tìm thấy phiếu nhập

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            importService.rejectImport(importId, reason);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, thrown.getMessage());
    }

    @Test
    public void testRejectImport_ImportNotPending() {
        // Arrange
        Long importId = 1L;
        String reason = "Some reason";

        // Cập nhật trạng thái phiếu nhập thành CONFIRMED (không phải PENDING)
        importMock.setStatus(OrderStatus.CONFIRMED);

        // Giả lập người dùng có quyền ROLE_PRODUCT_OWNER
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.of(importMock)); // Phiếu nhập có trạng thái CONFIRMED

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            importService.rejectImport(importId, reason);
        });

        assertEquals(Message.NOT_PENDING_IMPORT, thrown.getMessage());
    }

    @Test
    public void testRejectImport_Success() {
        // Arrange
        Long importId = 1L;
        String reason = "The reason for rejection";

        // Giả lập người dùng có quyền ROLE_PRODUCT_OWNER
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.of(importMock)); // Phiếu nhập ở trạng thái PENDING

        // Act
        importService.rejectImport(importId, reason);

        // Assert
        verify(importRepository).save(importMock);  // Kiểm tra việc lưu phiếu nhập
        assertEquals(OrderStatus.REJECT, importMock.getStatus()); // Kiểm tra trạng thái là REJECT
        assertEquals(reason, importMock.getNote()); // Kiểm tra lý do từ chối được lưu trong trường note

        // Kiểm tra gửi thông báo cho người tạo phiếu nhập
        verify(notificationService).sendNotificationToUser(anyString(), anyString(), eq(importCreator), eq(importId));
    }

}
