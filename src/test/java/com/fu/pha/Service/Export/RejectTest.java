package com.fu.pha.Service.Export;

import com.fu.pha.enums.ERole;
import com.fu.pha.exception.Message;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.repository.*;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import com.fu.pha.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RejectTest {
    @Mock private ExportSlipRepository exportSlipRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private ExportSlipServiceImpl exportSlipService;

    @Test
    void testRejectExport_UnauthorizedUser() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User unauthorizedUser = new User();
        unauthorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));
        doReturn(unauthorizedUser).when(exportSlipServiceSpy).getCurrentUser();

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            exportSlipServiceSpy.rejectExport(1L, "Reason");
        });
        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    @Test
    void testRejectExport_EmptyReason() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(exportSlipServiceSpy).getCurrentUser();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipServiceSpy.rejectExport(1L, "");
        });
        assertEquals(Message.REASON_REQUIRED, exception.getMessage());
    }

    @Test
    void testRejectExport_NullReason() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(exportSlipServiceSpy).getCurrentUser();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipServiceSpy.rejectExport(1L, null);
        });
        assertEquals(Message.REASON_REQUIRED, exception.getMessage());
    }

    @Test
    void testRejectExport_ExportSlipNotFound() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(exportSlipServiceSpy).getCurrentUser();
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipServiceSpy.rejectExport(1L, "Reason");
        });
        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testRejectExport_StatusNotPending() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(exportSlipServiceSpy).getCurrentUser();

        ExportSlip exportSlipMock = new ExportSlip();
        exportSlipMock.setStatus(OrderStatus.REJECT);
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.of(exportSlipMock));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipServiceSpy.rejectExport(1L, "Reason");
        });
        assertEquals(Message.NOT_REJECT, exception.getMessage());
    }

    @Test
    void testRejectExport_Success() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User authorizedUser = new User();
        authorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));
        doReturn(authorizedUser).when(exportSlipServiceSpy).getCurrentUser();

        ExportSlip exportSlipMock = new ExportSlip();
        exportSlipMock.setStatus(OrderStatus.PENDING);
        User exportCreator = new User();
        exportSlipMock.setUser(exportCreator);
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.of(exportSlipMock));

        // Act
        exportSlipServiceSpy.rejectExport(1L, "Reason");

        // Assert
        assertEquals(OrderStatus.REJECT, exportSlipMock.getStatus());
        assertEquals("Reason", exportSlipMock.getNote());
        verify(exportSlipRepository, times(1)).save(exportSlipMock);
        verify(notificationService, times(1)).sendNotificationToUser(anyString(), anyString(), eq(exportCreator), anyString());
    }

}
