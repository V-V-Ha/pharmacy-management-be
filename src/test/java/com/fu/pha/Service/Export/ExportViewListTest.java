package com.fu.pha.Service.Export;

import com.fu.pha.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.ExportSlip;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ExportSlipRepository;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ExportViewListTest {

    @Mock private ExportSlipRepository exportSlipRepository;

    @InjectMocks private ExportSlipServiceImpl exportSlipService;

    private Pageable pageable;
    private ExportSlip exportSlip;
    private Long exportSlipId = 1L;
    private Instant fromDate = Instant.now().minusSeconds(10000);
    private Instant toDate = Instant.now();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pageable = PageRequest.of(0, 10);

        // Setup mock ExportSlip
        exportSlip = new ExportSlip();
        exportSlip.setId(exportSlipId);
        exportSlip.setStatus(OrderStatus.PENDING);
        exportSlip.setUser(new User()); // mock user

        // Mock trả về khi gọi repository findById
        when(exportSlipRepository.findById(exportSlipId)).thenReturn(Optional.of(exportSlip));
    }

    @Test
    void testGetAllExportSlipPaging_EmptyResult() {
        // Kiểm tra trường hợp không có phiếu xuất
        Page<ExportSlip> emptyPage = new PageImpl<>(List.of());


        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, null, null);

        assertTrue(result.isEmpty(), "Kết quả phải rỗng");
    }

    @Test
    void testGetAllExportSlipPaging_WithResults() {
        // Kiểm tra trường hợp có phiếu xuất
        Page<ExportSlip> exportSlipPage = new PageImpl<>(List.of(exportSlip));


        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, null, null);

        assertFalse(result.isEmpty(), "Kết quả không thể rỗng");
        assertEquals(1, result.getTotalElements(), "Số lượng phần tử trong kết quả phải đúng");
    }

    @Test
    void testGetAllExportSlipPaging_WithFromDate() {
        // Kiểm tra trường hợp chỉ có fromDate
        Page<ExportSlip> exportSlipPage = new PageImpl<>(List.of(exportSlip));


        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.RETURN_TO_SUPPLIER, OrderStatus.PENDING, fromDate, null);

        assertFalse(result.isEmpty(), "Kết quả không thể rỗng");
        assertEquals(1, result.getTotalElements(), "Số lượng phần tử trong kết quả phải đúng");
    }

    @Test
    void testGetAllExportSlipPaging_WithToDate() {
        // Kiểm tra trường hợp chỉ có toDate
        Page<ExportSlip> exportSlipPage = new PageImpl<>(List.of(exportSlip));


        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.RETURN_TO_SUPPLIER, OrderStatus.PENDING, null, toDate);

        assertFalse(result.isEmpty(), "Kết quả không thể rỗng");
        assertEquals(1, result.getTotalElements(), "Số lượng phần tử trong kết quả phải đúng");
    }

    @Test
    void testGetAllExportSlipPaging_WithFromDateAndToDate() {
        // Kiểm tra trường hợp có cả từ ngày và đến ngày
        Page<ExportSlip> exportSlipPage = new PageImpl<>(List.of(exportSlip));


        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.RETURN_TO_SUPPLIER, OrderStatus.PENDING, fromDate, toDate);

        assertFalse(result.isEmpty(), "Kết quả không thể rỗng");
        assertEquals(1, result.getTotalElements(), "Số lượng phần tử trong kết quả phải đúng");
    }

    @Test
    void testGetAllExportSlipPaging_NoExportSlipFound() {
        // Kiểm tra khi không có phiếu xuất nào tìm thấy
        when(exportSlipRepository.getListExportSlipPagingWithoutDate(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.getAllExportSlipPaging(0, 10, ExportType.RETURN_TO_SUPPLIER, OrderStatus.PENDING, null, null);
        });
    }
}
