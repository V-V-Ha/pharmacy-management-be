package com.fu.pha.Service.Export;

import com.fu.pha.entity.ExportSlip;
import com.fu.pha.entity.ExportSlipItem;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ExportSlipItemRepository;
import com.fu.pha.repository.ExportSlipRepository;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ExportDeleteTest {

    @Mock
    private ExportSlipRepository exportSlipRepository;

    @Mock
    private ExportSlipItemRepository exportSlipItemRepository;

    @InjectMocks
    private ExportSlipServiceImpl exportSlipService;

    @Test
    void testSoftDeleteExportSlip_Success() {
        // Tạo một đối tượng ExportSlip và thiết lập thuộc tính isDeleted ban đầu là false
        ExportSlip exportSlip = new ExportSlip();
        exportSlip.setId(1L);
        exportSlip.setIsDeleted(false);

        // Tạo một đối tượng ExportSlipItem liên kết với ExportSlip trên và thiết lập isDeleted ban đầu là false
        ExportSlipItem exportSlipItem = new ExportSlipItem();
        exportSlipItem.setId(1L);
        exportSlipItem.setIsDeleted(false);
        exportSlipItem.setExportSlip(exportSlip); // Liên kết ExportSlipItem với ExportSlip

        // Khi `exportSlipRepository.findById(1L)` được gọi, trả về Optional chứa exportSlip
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        // Khi `exportSlipItemRepository.findByExportSlipId(1L)` được gọi, trả về danh sách chứa exportSlipItem
        when(exportSlipItemRepository.findByExportSlipId(1L)).thenReturn(Collections.singletonList(exportSlipItem));

        // Gọi phương thức softDeleteExportSlip để kiểm tra hoạt động xóa mềm
        exportSlipService.softDeleteExportSlip(1L);

        // Kiểm tra xem thuộc tính isDeleted của exportSlip đã được đặt thành true hay chưa
        assertTrue(exportSlip.getIsDeleted());

        // Kiểm tra xem thuộc tính isDeleted của exportSlipItem đã được đặt thành true hay chưa
        assertTrue(exportSlipItem.getIsDeleted());

        // Xác minh rằng phương thức save trên exportSlipRepository đã được gọi đúng một lần với exportSlip
        verify(exportSlipRepository, times(1)).save(exportSlip);

        // Xác minh rằng phương thức save trên exportSlipItemRepository đã được gọi đúng một lần với exportSlipItem
        verify(exportSlipItemRepository, times(1)).save(exportSlipItem);
    }

    @Test
    void testSoftDeleteExportSlip_NotFoundExport() {
        when(exportSlipRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.softDeleteExportSlip(200L);
        });

        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());
        verify(exportSlipRepository, times(1)).findById(200L);
    }
}