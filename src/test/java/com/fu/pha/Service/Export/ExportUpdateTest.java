package com.fu.pha.Service.Export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.exportSlip.ExportSlipItemRequestDto;
import com.fu.pha.dto.request.exportSlip.ExportSlipRequestDto;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.exception.UnauthorizedException;
import com.fu.pha.repository.*;
import com.fu.pha.service.ExportSlipService;
import com.fu.pha.entity.*;
import com.fu.pha.enums.ExportType;
import com.fu.pha.exception.Message;
import com.fu.pha.service.NotificationService;
import com.fu.pha.service.impl.ExportSlipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ExportUpdateTest {
    @Mock private ExportSlipRepository exportSlipRepository;
    @Mock private ImportItemRepository importItemRepository;
    @Mock private ExportSlipItemRepository exportSlipItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryHistoryRepository inventoryHistoryRepository;
    @Mock private NotificationService notificationService;
    @Mock private GenerateCode generateCode;

    @InjectMocks private ExportSlipServiceImpl exportSlipService;

    private User currentUser;
    private ExportSlipRequestDto exportDto;
    private ExportSlip exportSlip;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock data
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        exportDto = new ExportSlipRequestDto();
        exportDto.setTypeDelivery(ExportType.RETURN_TO_SUPPLIER);
        exportDto.setDiscount(5.0);
        exportDto.setNote("Test export");

        exportSlip = new ExportSlip();
        exportSlip.setId(1L);
        exportSlip.setStatus(OrderStatus.PENDING);
        exportSlip.setUser(currentUser);
        exportSlip.setExportDate(Instant.now());

        // Mocking user authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testUpdateExport_ExportSlipNotFound() {
        // Test trường hợp không tìm thấy phiếu xuất
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            exportSlipService.updateExport(1L, exportDto);
        });
    }

    @Test
    void testUpdateExport_StatusConfirmed_NotUpdatable() {
        // Test trường hợp phiếu xuất đã được xác nhận và không thể cập nhật
        exportSlip.setStatus(OrderStatus.CONFIRMED);
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        assertThrows(BadRequestException.class, () -> {
            exportSlipService.updateExport(1L, exportDto);
        });
    }

    @Test
    void testUpdateExport_UnauthorizedUser() {
        // Test trường hợp người dùng không có quyền cập nhật
        exportSlip.setUser(new User()); // Thay đổi người tạo phiếu xuất
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        assertThrows(UnauthorizedException.class, () -> {
            exportSlipService.updateExport(1L, exportDto);
        });
    }

    @Test
    void testUpdateExport_RejectStatus_UpdateToPending() {
        // Test khi phiếu xuất có trạng thái REJECT và người cập nhật không phải chủ cửa hàng
        exportSlip.setStatus(OrderStatus.REJECT);
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        exportSlipService.updateExport(1L, exportDto);

        assertEquals(OrderStatus.PENDING, exportSlip.getStatus());
    }

    @Test
    void testUpdateExport_InvalidExportType() {
        // Test khi loại phiếu xuất không hợp lệ
        exportDto.setTypeDelivery(null);
        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));

        assertThrows(BadRequestException.class, () -> {
            exportSlipService.updateExport(1L, exportDto);
        });
    }

    @Test
    void testUpdateExport_ExportItems_ValidItems() {
        // Test khi cập nhật ExportSlipItem hợp lệ
        ExportSlipItemRequestDto itemDto = new ExportSlipItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setImportItemId(1L);
        itemDto.setQuantity(10);
        exportDto.setExportSlipItems(Collections.singletonList(itemDto));

        ExportSlipItem exportSlipItem = new ExportSlipItem();
        exportSlipItem.setProduct(new Product());
        exportSlipItem.setImportItem(new ImportItem());

        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(exportSlipItemRepository.findByExportSlipId(1L)).thenReturn(new ArrayList<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(new ImportItem()));

        exportSlipService.updateExport(1L, exportDto);

        verify(exportSlipItemRepository, times(1)).save(any(ExportSlipItem.class));
    }

    @Test
    void testUpdateExport_AmountMismatch() {
        // Test khi tổng tiền không khớp với tổng tiền trong form
        exportDto.setTotalAmount(100.0);

        assertThrows(BadRequestException.class, () -> {
            exportSlipService.updateExport(1L, exportDto);
        });
    }

    @Test
    void testUpdateExport_StockUpdateForConfirmed() {
        // Test khi phiếu xuất đã xác nhận và cần xử lý tồn kho
        ExportSlipItemRequestDto itemDto = new ExportSlipItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setImportItemId(1L);
        itemDto.setQuantity(5);
        exportDto.setExportSlipItems(Collections.singletonList(itemDto));

        Product product = new Product();
        product.setTotalQuantity(100);

        ImportItem importItem = new ImportItem();
        importItem.setRemainingQuantity(100);

        ExportSlipItem exportSlipItem = new ExportSlipItem();
        exportSlipItem.setProduct(product);
        exportSlipItem.setImportItem(importItem);
        exportSlipItem.setQuantity(5);

        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(exportSlipItemRepository.findByExportSlipId(1L)).thenReturn(new ArrayList<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        exportSlipService.updateExport(1L, exportDto);

        verify(productRepository, times(1)).save(product);
        verify(importItemRepository, times(1)).save(importItem);
    }

    @Test
    void testUpdateExport_InvalidStock() {
        // Test khi không đủ tồn kho
        Product product = new Product();
        product.setTotalQuantity(0);

        ImportItem importItem = new ImportItem();
        importItem.setRemainingQuantity(0);

        ExportSlipItemRequestDto itemDto = new ExportSlipItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setImportItemId(1L);
        itemDto.setQuantity(10);
        exportDto.setExportSlipItems(Collections.singletonList(itemDto));

        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
        when(exportSlipItemRepository.findByExportSlipId(1L)).thenReturn(new ArrayList<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(importItemRepository.findById(1L)).thenReturn(Optional.of(importItem));

        assertThrows(BadRequestException.class, () -> {
            exportSlipService.updateExport(1L, exportDto);
        });
    }

}
