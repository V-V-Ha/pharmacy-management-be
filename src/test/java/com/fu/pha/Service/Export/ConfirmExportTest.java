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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ConfirmExportTest {

    @Mock private ExportSlipRepository exportSlipRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private ExportSlipServiceImpl exportSlipService;

    private User currentUser;
    private ExportSlip exportSlip;

    @BeforeEach
    void setUp() {
        // Set up mock user and role
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_PRODUCT_OWNER.name())));

        exportSlip = new ExportSlip();
        exportSlip.setId(1L);
        exportSlip.setStatus(OrderStatus.PENDING);
        exportSlip.setExportSlipItemList(Collections.emptyList());

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Mock userRepository to return the current user
        lenient().when(userRepository.findByUsername("user")).thenReturn(Optional.of(currentUser));
    }

    @Test
    void testConfirmExport_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.confirmExport(1L);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testConfirmExport_UnauthorizedUser() {
        currentUser.setRoles(Collections.singleton(new Role(ERole.ROLE_STOCK.name())));  // Change to stock role
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(currentUser));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            exportSlipService.confirmExport(1L);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    @Test
    void testConfirmExport_ExportSlipNotFound() {
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.confirmExport(1L);
        });

        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());
    }

    @Test
    void testConfirmExport_ExportSlipNotPending() {
        exportSlip.setStatus(OrderStatus.CONFIRMED);
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipService.confirmExport(1L);
        });

        assertEquals(Message.NOT_PENDING_EXPORT, exception.getMessage());
    }

    @Test
    void testConfirmExport_Success() {
        // Mock to ensure exportSlip is found
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        // Call the confirmExport method
        exportSlipService.confirmExport(1L);

        // Verify status change and save call
        assertEquals(OrderStatus.CONFIRMED, exportSlip.getStatus());
        verify(exportSlipRepository, times(1)).save(exportSlip);

        // Verify notification sent to user
        verify(notificationService, times(1)).sendNotificationToUser(
                eq("Phiếu xuất đã được xác nhận"),
                eq("Phiếu xuất của bạn đã được chủ cửa hàng xác nhận."),
                eq(exportSlip.getUser()),
                eq("/export/receipt/detail/" + exportSlip.getId())
        );
    }
}
