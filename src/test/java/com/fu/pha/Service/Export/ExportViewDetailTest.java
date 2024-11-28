package com.fu.pha.Service.Export;

import com.fu.pha.dto.response.exportSlip.ExportSlipResponseDto;
import com.fu.pha.entity.*;
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

    @Mock
    private ExportSlipRepository exportSlipRepository;

    @InjectMocks
    private ExportSlipServiceImpl exportSlipService;

    private ExportSlip exportSlip;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Thiết lập đối tượng User và ExportSlip
        User user = new User();
        user.setId(1L);

        exportSlip = new ExportSlip();
        exportSlip.setId(1L);
        exportSlip.setUser(user);

        // Thiết lập đối t��ợng Category
        Category category = new Category();
        category.setCategoryName("Thuốc ho");

        // Thiết lập đối tượng Unit và ProductUnit
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setUnitName("Hộp");

        ProductUnit productUnit = new ProductUnit();
        productUnit.setUnit(unit);  // Gán Unit hợp lệ vào ProductUnit

        // Thiết lập đối tượng Product với ProductUnit và Category
        Product product = new Product();
        product.setId(1L);
        product.setCategoryId(category);
        product.setProductUnitList(Collections.singletonList(productUnit));

        // Gán Product cho ProductUnit để tránh NullPointerException
        productUnit.setProduct(product);

        // Thiết lập đối tượng ImportItem
        ImportItem importItem = new ImportItem();
        importItem.setId(1L);

        // Thiết lập đối tượng ExportSlipItem và gán Product, ImportItem vào
        ExportSlipItem exportSlipItem = new ExportSlipItem();
        exportSlipItem.setId(1L);
        exportSlipItem.setProduct(product);
        exportSlipItem.setImportItem(importItem);
        exportSlipItem.setExportSlip(exportSlip); // Set ExportSlip in ExportSlipItem
        exportSlip.setExportSlipItemList(Collections.singletonList(exportSlipItem));

        // Mock repository
        lenient().when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        lenient().when(exportSlipRepository.findById(200L)).thenReturn(Optional.empty());
    }

    @Test
    void testGetActiveExportSlipById_Success() {
        ExportSlipResponseDto response = exportSlipService.getActiveExportSlipById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Thuốc ho", response.getExportSlipItems().get(0).getProduct().getCategoryName());

        verify(exportSlipRepository, times(1)).findById(1L);
    }

    @Test
    void testGetActiveExportSlipById_NotFoundExportSlip() {
        // Giả lập repository trả về Optional.empty() khi tìm kiếm với id = 200
        when(exportSlipRepository.findById(200L)).thenReturn(Optional.empty());

        // Kiểm tra rằng khi gọi getActiveExportSlipById với id = 200, một ResourceNotFoundException sẽ được ném ra
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.getActiveExportSlipById(200L);
        });

        // Kiểm tra thông báo lỗi trong exception khớp với thông báo mong đợi
        assertEquals(Message.EXPORT_SLIP_NOT_FOUND, exception.getMessage());

        // Xác minh rằng phương thức findById đã được gọi chính xác một lần với id = 200
        verify(exportSlipRepository, times(1)).findById(200L);
    }

}
