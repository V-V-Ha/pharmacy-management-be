package com.fu.pha.Service.Export;

import com.fu.pha.entity.User;
import com.fu.pha.enums.ExportType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.ExportSlip;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ExportSlipRepository;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.Collections;

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
        pageable = PageRequest.of(0, 10);
        exportSlip = new ExportSlip();
        exportSlip.setId(exportSlipId);
        exportSlip.setStatus(OrderStatus.PENDING);
    }

    // Test case lấy danh sách phiếu xuất kho thành công
    @Test
    void UTCEL01() {
        // Create a mock User object to avoid NullPointerException
        User mockUser = new User();
        mockUser.setId(1L);  // Set some valid ID

        // Create an ExportSlip with the mock User
        ExportSlip exportSlip = new ExportSlip();
        exportSlip.setUser(mockUser);  // Ensure that ExportSlip has a non-null User
        exportSlip.setStatus(OrderStatus.PENDING);  // Ensure that ExportSlip has a non-null status
        exportSlip.setExportSlipItemList(Collections.emptyList());  // Initialize the exportSlipItemList

        // Create ExportSlipResponseDto with the mock ExportSlip
        Page<ExportSlipResponseDto> page = new PageImpl<>(Collections.singletonList(new ExportSlipResponseDto(exportSlip)));

        // Mock the repository method
        when(exportSlipRepository.getListExportSlipPagingWithoutDate(any(), any(), any()))
                .thenReturn(page);

        // Act
        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test case trường hợp chỉ có fromDate
    @Test
    void UTCEL02() {
        // Create a mock User object to avoid NullPointerException
        User mockUser = new User();
        mockUser.setId(1L);  // Set some valid ID

        // Create an ExportSlip with the mock User
        ExportSlip exportSlip = new ExportSlip();
        exportSlip.setUser(mockUser);  // Ensure that ExportSlip has a non-null User
        exportSlip.setStatus(OrderStatus.PENDING);  // Ensure that ExportSlip has a non-null status
        exportSlip.setExportSlipItemList(Collections.emptyList());  // Initialize the exportSlipItemList

        // Create ExportSlipResponseDto with the mock ExportSlip
        Page<ExportSlipResponseDto> page = new PageImpl<>(Collections.singletonList(new ExportSlipResponseDto(exportSlip)));

        // Mock the repository method
        when(exportSlipRepository.getListExportSlipPagingFromDate(any(), any(), any(), any()))
                .thenReturn(page);

        // Act
        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, fromDate, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test case trường hợp chỉ có toDate
    @Test
    void UTCEL03() {
        // Create a mock User object to avoid NullPointerException
        User mockUser = new User();
        mockUser.setId(1L);  // Set some valid ID

        // Create an ExportSlip with the mock User
        ExportSlip exportSlip = new ExportSlip();
        exportSlip.setUser(mockUser);  // Ensure that ExportSlip has a non-null User
        exportSlip.setStatus(OrderStatus.PENDING);  // Ensure that ExportSlip has a non-null status
        exportSlip.setExportSlipItemList(Collections.emptyList());  // Initialize the exportSlipItemList

        // Create ExportSlipResponseDto with the mock ExportSlip
        Page<ExportSlipResponseDto> page = new PageImpl<>(Collections.singletonList(new ExportSlipResponseDto(exportSlip)));

        // Mock the repository method
        when(exportSlipRepository.getListExportSlipPagingToDate(any(), any(), any(), any()))
                .thenReturn(page);

        // Act
        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, null, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test case trường hợp có cả fromDate và toDate
    @Test
    void UTCEL04() {
        // Create a mock User object to avoid NullPointerException
        User mockUser = new User();
        mockUser.setId(1L);  // Set some valid ID

        // Create an ExportSlip with the mock User
        ExportSlip exportSlip = new ExportSlip();
        exportSlip.setUser(mockUser);  // Ensure that ExportSlip has a non-null User
        exportSlip.setStatus(OrderStatus.PENDING);  // Ensure that ExportSlip has a non-null status
        exportSlip.setExportSlipItemList(Collections.emptyList());  // Initialize the exportSlipItemList

        // Create ExportSlipResponseDto with the mock ExportSlip
        Page<ExportSlipResponseDto> page = new PageImpl<>(Collections.singletonList(new ExportSlipResponseDto(exportSlip)));

        // Mock the repository method
        when(exportSlipRepository.getListExportSlipPaging(any(), any(), any(), any(), any()))
                .thenReturn(page);

        // Act
        Page<ExportSlipResponseDto> result = exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, fromDate, toDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // Test case không tìm thấy phiếu xuất
    @Test
    void UTCEL05() {
        when(exportSlipRepository.getListExportSlipPagingWithoutDate(any(), any(), any()))
                .thenReturn(Page.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.getAllExportSlipPaging(0, 10, ExportType.DESTROY, OrderStatus.PENDING, null, null);
        });
    }

}
