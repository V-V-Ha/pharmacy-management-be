package com.fu.pha.Service.Import;

import com.fu.pha.enums.ERole;
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
import java.util.List;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
public class ImportConfirmTest {
    @Mock
    private ImportRepository importRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

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

        // Thiết lập các ImportItems cho Import
        ImportItem importItem1 = new ImportItem();
        importItem1.setQuantity(10);
        importItem1.setConversionFactor(1);
        importMock.setImportItems(List.of(importItem1));
    }

    @Test
    public void testConfirmImport_UserNotFound() {
        // Arrange
        Long userId = 1L;
        Long importId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            importService.confirmImport(importId, userId);
        });

        assertEquals(Message.USER_NOT_FOUND, thrown.getMessage());
    }

    @Test
    public void testConfirmImport_UserUnauthorized() {
        // Arrange
        Long userId = 1L;
        Long importId = 1L;

        // Giả lập người dùng không có quyền ROLE_PRODUCT_OWNER
        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.of(importMock));

        // Act & Assert
        UnauthorizedException thrown = assertThrows(UnauthorizedException.class, () -> {
            importService.confirmImport(importId, userId);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, thrown.getMessage());
    }

    @Test
    public void testConfirmImport_ImportNotFound() {
        // Arrange
        Long userId = 1L;
        Long importId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            importService.confirmImport(importId, userId);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, thrown.getMessage());
    }

    @Test
    public void testConfirmImport_ImportNotPending() {
        // Arrange
        Long userId = 1L;
        Long importId = 1L;

        // Cập nhật trạng thái phiếu nhập thành CONFIRMED
        importMock.setStatus(OrderStatus.CONFIRMED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.of(importMock));

        // Act & Assert
        BadRequestException thrown = assertThrows(BadRequestException.class, () -> {
            importService.confirmImport(importId, userId);
        });

        assertEquals(Message.NOT_PENDING_IMPORT, thrown.getMessage());
    }

    @Test
    public void testConfirmImport_Success() {
        // Arrange
        Long userId = 1L;
        Long importId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(importId)).thenReturn(Optional.of(importMock));

        // Act
        importService.confirmImport(importId, userId);

        // Assert
        verify(importRepository).save(importMock);  // Kiểm tra save Import
        assertEquals(OrderStatus.CONFIRMED, importMock.getStatus()); // Kiểm tra trạng thái là CONFIRMED

        // Kiểm tra gửi thông báo cho người tạo phiếu nhập
        verify(notificationService).sendNotificationToUser(anyString(), anyString(), eq(importCreator), eq(importId));

        // Kiểm tra gọi lưu lịch sử kho hàng
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    @Test
    public void testSaveInventoryHistory_NoChangeQuantity() {
        // Arrange
        ImportItem importItem = importMock.getImportItems().get(0);
        int totalChangeQuantity = 0; // Không thay đổi số lượng sản phẩm

        // Act

        // Assert
        verify(inventoryHistoryRepository, never()).save(any(InventoryHistory.class)); // Không gọi lưu lịch sử kho hàng
    }

    @Test
    public void testSaveInventoryHistory_WithChangeQuantity() {
        // Arrange
        ImportItem importItem = importMock.getImportItems().get(0);
        int totalChangeQuantity = 10; // Thay đổi số lượng sản phẩm

        // Act

        // Assert
        verify(inventoryHistoryRepository).save(any(InventoryHistory.class)); // Kiểm tra lưu lịch sử kho hàng
    }
}
