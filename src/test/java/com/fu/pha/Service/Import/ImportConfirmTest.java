package com.fu.pha.Service.Import;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.repository.*;
import com.fu.pha.service.NotificationService;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class ImportConfirmTest {

    @Mock
    private ImportRepository importRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductUnitRepository productUnitRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ImportServiceImpl importService;

    @Captor
    private ArgumentCaptor<Import> importCaptor;

    private Import importMock;
    private User currentUser;
    private User importCreator;

    @BeforeEach
    public void setUp() {
        // Initialize Import Mock
        importMock = new Import();
        importMock.setId(1L);
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setImportItems(Collections.emptyList());

        // Initialize Current User
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("user");
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        // Initialize Import Creator
        importCreator = new User();
        importCreator.setId(2L);
        importMock.setUser(importCreator);

        // Mock SecurityContext and Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user");

        // Mock userRepository.findByUsername("user") to return currentUser
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(currentUser));
    }

    // Test case không tìm thấy người dùng
    @Test
    public void UTCICF01() {
        // Arrange: Change user's roles to not include PRODUCT_OWNER
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_SALE.name())));
        // No need to mock userRepository.findById(...) as it's not used
        // Mock importRepository.findById(1L) to return importMock
        // Khi từ chối Import, ta không cần gọi findById(1L) nếu user không có quyền

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importService.rejectImport(1L, "Không đủ quyền hạn");
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());

        // Verify that no save operation was performed
        verify(importRepository, never()).save(any());
        // Verify that notificationService.sendNotificationToUser không được gọi
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }

    // Test case Import không tồn tại
    @Test
    public void UTCICF02() {
        // Arrange: Import not found
        when(importRepository.findById(200L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.rejectImport(200L, "Lý do từ chối");
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());

        // Verify that no save operation was performed
        verify(importRepository, never()).save(any());
        // Verify rằng notificationService.sendNotificationToUser không được gọi
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }

    // Test case thành công khi từ chối Import
    @Test
    public void UTCICF03() {
        // Arrange: Import found and status is PENDING
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // Mock ImportItem and associate with importMock
        ImportItem mockImportItem = new ImportItem();
        mockImportItem.setId(1L);
        mockImportItem.setProduct(new Product());
        mockImportItem.getProduct().setId(1L);
        mockImportItem.getProduct().setTotalQuantity(100); // Prevent NPE
        mockImportItem.setQuantity(2);
        mockImportItem.setRemainingQuantity(2);
        mockImportItem.setConversionFactor(1); // Đảm bảo không null
        importMock.setImportItems(Collections.singletonList(mockImportItem));

        // Act: Call rejectImport
        importService.rejectImport(1L, "Lý do từ chối");

        // Assert: Verify status update and save
        assertEquals(OrderStatus.REJECT, importMock.getStatus());
        assertEquals("Lý do từ chối", importMock.getNote());
        verify(importRepository, times(1)).save(importMock);

        // Verify notification
        verify(notificationService, times(1)).sendNotificationToUser(
                eq("Phiếu nhập bị từ chối"),
                eq("Phiếu nhập của bạn đã bị từ chối. Lý do: Lý do từ chối"),
                eq(importCreator),
                eq("/import/receipt/detail/" + importMock.getId())
        );

    }

    // Test case Import status is not PENDING
    @Test
    public void UTCICF04() {
        // Arrange: Import status is not PENDING
        importMock.setStatus(OrderStatus.CONFIRMED);
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importService.rejectImport(1L, "Lý do từ chối");
        });

        assertEquals(Message.NOT_PENDING_IMPORT, exception.getMessage());

        // Verify that no save operation was performed
        verify(importRepository, never()).save(any());
        // Verify rằng notificationService.sendNotificationToUser không được gọi
        verify(notificationService, never()).sendNotificationToUser(anyString(), anyString(), any(User.class), anyString());
    }
}
