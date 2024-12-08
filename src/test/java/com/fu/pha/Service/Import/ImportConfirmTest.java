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
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        // Initialize Import Creator
        importCreator = new User();
        importCreator.setId(2L);
        importMock.setUser(importCreator);
    }

    @Test
    public void testConfirmImport_UserUnauthorized() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        // Change user role to ROLE_STOCK
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_SALE.name())));
        doReturn(currentUser).when(importServiceSpy).getCurrentUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importServiceSpy.confirmImport(1L, 2L);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());

        // Verify that no other interactions occurred
        verify(importRepository, never()).save(any());
    }

    @Test
    public void testConfirmImport_ImportNotFound() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(currentUser).when(importServiceSpy).getCurrentUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(200L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importServiceSpy.confirmImport(200L, 1L);
        });

        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());

        // Verify that no other interactions occurred
        verify(importRepository, never()).save(any());
    }

    @Test
    public void testConfirmImport_Success() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(currentUser).when(importServiceSpy).getCurrentUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // Mock Product with totalQuantity
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setTotalQuantity(100); // Prevent NPE
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(mockProduct));

        // Mock ImportItem and associate with importMock
        ImportItem mockImportItem = new ImportItem();
        mockImportItem.setId(1L);
        mockImportItem.setProduct(mockProduct);
        mockImportItem.setQuantity(2);
        mockImportItem.setRemainingQuantity(2);
        importMock.setImportItems(Collections.singletonList(mockImportItem));

        // Act
        importServiceSpy.confirmImport(1L, 1L);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, importMock.getStatus());
        verify(importRepository, times(1)).save(importMock);
    }

    @Test
    public void testConfirmImport_ImportNotPending() {
        // Arrange
        // Set import status to CONFIRMED
        importMock.setStatus(OrderStatus.CONFIRMED);
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        doReturn(currentUser).when(importServiceSpy).getCurrentUser();
        when(importRepository.findById(1L)).thenReturn(Optional.of(importMock));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.confirmImport(1L, 1L);
        });

        assertEquals(Message.NOT_PENDING_IMPORT, exception.getMessage());

        // Verify that no save operation was performed
        verify(importRepository, never()).save(any());
    }

}
