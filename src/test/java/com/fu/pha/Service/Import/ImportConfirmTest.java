package com.fu.pha.Service.Import;

import com.fu.pha.enums.ERole;
import com.fu.pha.service.NotificationService;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Collections;
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
    private ImportServiceImpl importService;

    private Import importMock;
    private User currentUser;
    private User importCreator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        importMock = new Import();
        importMock.setId(1L);
        importMock.setStatus(OrderStatus.PENDING);
        importMock.setImportItems(Collections.emptyList());

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        importCreator = new User();
        importCreator.setId(2L);
        importMock.setUser(importCreator);
    }

    @Test
    public void testConfirmImport_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.confirmImport(1L, 1L);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testConfirmImport_UserUnauthorized() {
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        lenient().when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importService.confirmImport(1L, 1L);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    @Test
    public void testConfirmImport_ImportNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.confirmImport(1L, 1L);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testConfirmImport_ImportNotPending() {
        importMock.setStatus(OrderStatus.CONFIRMED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importService.confirmImport(1L, 1L);
        });

        assertEquals(Message.NOT_PENDING_IMPORT, exception.getMessage());
    }

    @Test
    public void testConfirmImport_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        importService.confirmImport(1L, 1L);

        assertEquals(OrderStatus.CONFIRMED, importMock.getStatus());
        verify(importRepository, times(1)).save(importMock);
    }

    @Test
    public void testSaveInventoryHistory_NoChangeQuantity() {
        ImportItem importItem = new ImportItem();
        importItem.setQuantity(0);
        importItem.setConversionFactor(1);

        importService.saveInventoryHistory(importItem, 0, "No Change");

        verify(inventoryHistoryRepository, never()).save(any());
    }

    @Test
    public void testSaveInventoryHistory_WithChangeQuantity() {
        ImportItem importItem = new ImportItem();
        importItem.setQuantity(10);
        importItem.setConversionFactor(1);

        importService.saveInventoryHistory(importItem, 10, "Change");

        verify(inventoryHistoryRepository, times(1)).save(any());
    }
}
