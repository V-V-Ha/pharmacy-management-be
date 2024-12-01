package com.fu.pha.Service.Export;

import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.*;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ExportSlipRepository;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ExportViewDetailTest {

    @Mock private ExportSlipRepository exportSlipRepository;

    @InjectMocks private ExportSlipServiceImpl exportSlipService;

    private ExportSlip exportSlip;
    private final Long exportSlipId = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock ExportSlip
        exportSlip = new ExportSlip();
        exportSlip.setId(exportSlipId);
        exportSlip.setStatus(OrderStatus.PENDING);
        exportSlip.setUser(new User()); // mock user

        // Mock trả về khi gọi repository findById
        when(exportSlipRepository.findById(exportSlipId)).thenReturn(Optional.of(exportSlip));
    }

    @Test
    void testGetActiveExportSlipById_ExportSlipNotFound() {
        // Kiểm tra khi không tìm thấy phiếu xuất
        when(exportSlipRepository.findById(exportSlipId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.getActiveExportSlipById(exportSlipId);
        });
    }

    @Test
    void testGetActiveExportSlipById_Success() {
        // Initialize the export slip item list
        exportSlip.setExportSlipItemList(Collections.emptyList());

        // Kiểm tra khi tìm thấy phiếu xuất và chuyển đổi thành DTO
        ExportSlipResponseDto result = exportSlipService.getActiveExportSlipById(exportSlipId);

        assertNotNull(result); // Kết quả không null
        assertEquals(exportSlipId, result.getId()); // ID của ExportSlip phải khớp với ID của DTO
        assertEquals(OrderStatus.PENDING.name(), result.getStatus()); // Trạng thái phải khớp với phiếu xuất gốc
        // Thêm các kiểm tra khác nếu DTO có thêm các trường cần kiểm tra
    }

}
