package com.fu.pha.Service.Export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.enums.ERole;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.repository.*;
import com.fu.pha.entity.*;
import com.fu.pha.exception.Message;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ExportUpdateTest {
    @Mock private ExportSlipRepository exportSlipRepository;

    @InjectMocks private ExportSlipServiceImpl exportSlipService;

    private User currentUser;
    private ExportSlipRequestDto exportDto;
    private ExportSlip exportSlip;
    private User mockUser;

    //Tese case không tìm thấy phiếu xuất kho
    @Test
    void UTCEU01() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        doReturn(mockUser).when(exportSlipServiceSpy).getCurrentUser();
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipServiceSpy.updateExport(200L, exportDto);
        });

        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());
    }

    //Test case cập nhật phiếu xuất kho đã xác nhận
    @Test
    void UTCEU02() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        doReturn(mockUser).when(exportSlipServiceSpy).getCurrentUser();

        ExportSlip exportSlipMock = new ExportSlip();
        exportSlipMock.setStatus(OrderStatus.CONFIRMED);
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.of(exportSlipMock));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipServiceSpy.updateExport(1L, exportDto);
        });

        assertEquals(Message.NOT_UPDATE_CONFIRMED, exception.getMessage());
    }

    //Test case người dùng không có quyền cập nhật phiếu xuất kho
    @Test
    void UTCEU03() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User unauthorizedUser = new User();
        unauthorizedUser.setRoles(Collections.singleton(new Role(ERole.ROLE_SALE.name())));
        doReturn(unauthorizedUser).when(exportSlipServiceSpy).getCurrentUser();

        User exportUser = new User();
        exportUser.setId(200L);
        ExportSlip exportSlipMock = new ExportSlip();
        exportSlipMock.setUser(exportUser);
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.of(exportSlipMock));

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            exportSlipServiceSpy.updateExport(1L, exportDto);
        });

        assertEquals(Message.REJECT_AUTHORIZATION, exception.getMessage());
    }

    //Test case loại phiếu xuất kho không hợp lệ
    @Test
    void UTCEU04() {
        // Arrange
        ExportSlipServiceImpl exportSlipServiceSpy = Mockito.spy(exportSlipService);
        User mockUser = new User();
        mockUser.setId(1L); // Set the ID of the user
        doReturn(mockUser).when(exportSlipServiceSpy).getCurrentUser();

        ExportSlip exportSlipMock = new ExportSlip();
        exportSlipMock.setStatus(OrderStatus.PENDING);
        exportSlipMock.setUser(mockUser); // Set the user in the export slip
        when(exportSlipRepository.findById(anyLong())).thenReturn(Optional.of(exportSlipMock));

        exportDto = new ExportSlipRequestDto(); // Initialize exportDto
        exportDto.setTypeDelivery(null); // Set invalid export type

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            exportSlipServiceSpy.updateExport(1L, exportDto);
        });

        assertEquals(Message.INVALID_EXPORT_TYPE, exception.getMessage());
    }

}
