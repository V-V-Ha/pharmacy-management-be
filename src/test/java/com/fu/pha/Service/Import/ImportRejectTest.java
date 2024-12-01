package com.fu.pha.Service.Import;

import com.fu.pha.enums.ERole;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
public class ImportRejectTest {
    @Mock
    private ImportRepository importRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ImportServiceImpl importService;

    @Test
    public void testRejectImport_UserUnauthorized() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User unauthorizedUser = new User();
        unauthorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(unauthorizedUser).when(importServiceSpy).getCurrentUser();

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            importServiceSpy.rejectImport(1L, "Reason");
        });
        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    @Test
    public void testRejectImport_ReasonRequired() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.rejectImport(1L, "");
        });
        assertEquals(Message.REASON_REQUIRED, exception.getMessage());
    }

    @Test
    public void testRejectImport_ImportNotFound() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();
        when(importRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importServiceSpy.rejectImport(1L, "Reason");
        });
        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testRejectImport_ImportNotPending() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        Import importMock = new Import();
        importMock.setStatus(OrderStatus.REJECT);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importServiceSpy.rejectImport(1L, "Reason");
        });
        assertEquals(Message.NOT_PENDING_IMPORT, exception.getMessage());
    }

    @Test
    public void testRejectImport_Success() {
        // Arrange
        ImportServiceImpl importServiceSpy = Mockito.spy(importService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(importServiceSpy).getCurrentUser();

        Import importMock = new Import();
        importMock.setStatus(OrderStatus.PENDING);
        User importCreator = new User();
        importMock.setUser(importCreator);
        when(importRepository.findById(anyLong())).thenReturn(Optional.of(importMock));

        // Act
        importServiceSpy.rejectImport(1L, "Reason");

        // Assert
        assertEquals(OrderStatus.REJECT, importMock.getStatus());
        assertEquals("Reason", importMock.getNote());
        verify(importRepository, times(1)).save(importMock);
        verify(notificationService, times(1)).sendNotificationToUser(anyString(), anyString(), eq(importCreator), anyString());
    }

}
