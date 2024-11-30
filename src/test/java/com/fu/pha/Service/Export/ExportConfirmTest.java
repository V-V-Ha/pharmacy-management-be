//package com.fu.pha.Service.Export;
//
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.mockito.Mockito.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//import com.fu.pha.exception.BadRequestException;
//import com.fu.pha.exception.ResourceNotFoundException;
//import com.fu.pha.exception.UnauthorizedException;
//import com.fu.pha.entity.*;
//import com.fu.pha.enums.OrderStatus;
//import com.fu.pha.repository.*;
//import com.fu.pha.service.impl.ExportSlipServiceImpl;
//import com.fu.pha.service.NotificationService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.Collections;
//import java.util.Optional;
//
//@ExtendWith(MockitoExtension.class)
//public class ExportConfirmTest {
//
//    @Mock private ExportSlipRepository exportSlipRepository;
//    @Mock private ExportSlipItemRepository exportSlipItemRepository;
//    @Mock private UserRepository userRepository;
//    @Mock private NotificationService notificationService;
//    @Mock private ProductRepository productRepository;
//    @Mock private InventoryHistoryRepository inventoryHistoryRepository;
//
//    @InjectMocks private ExportSlipServiceImpl exportSlipService;
//
//    private User currentUser;
//    private ExportSlip exportSlip;
//    private ExportSlipItem exportSlipItem;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // Setup mock data
//        currentUser = new User();
//        currentUser.setId(1L);
//        currentUser.setUsername("testuser");
//
//        exportSlip = new ExportSlip();
//        exportSlip.setId(1L);
//        exportSlip.setStatus(OrderStatus.PENDING);
//        exportSlip.setUser(currentUser);
//
//        exportSlipItem = new ExportSlipItem();
//        exportSlipItem.setQuantity(5);
//        exportSlipItem.setProduct(new Product());
//        exportSlipItem.setImportItem(new ImportItem());
//
//        exportSlip.setExportSlipItemList(Collections.singletonList(exportSlipItem));
//
//        // Mocking user authentication
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn("testuser");
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//    @Test
//    void testConfirmExport_UnauthorizedUser() {
//        // Test trường hợp người dùng không có quyền ROLE_PRODUCT_OWNER
//        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
//        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
//
//        assertThrows(UnauthorizedException.class, () -> {
//            exportSlipService.confirmExport(1L);
//        });
//    }
//
//    @Test
//    void testConfirmExport_ExportSlipNotFound() {
//        // Test trường hợp không tìm thấy phiếu xuất
//        when(exportSlipRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class, () -> {
//            exportSlipService.confirmExport(1L);
//        });
//    }
//
//    @Test
//    void testConfirmExport_StatusNotPending() {
//        // Test trường hợp trạng thái phiếu xuất không phải là PENDING
//        exportSlip.setStatus(OrderStatus.CONFIRMED);
//        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
//
//        assertThrows(BadRequestException.class, () -> {
//            exportSlipService.confirmExport(1L);
//        });
//    }
//
//    @Test
//    void testConfirmExport_Success() {
//        // Test trường hợp phiếu xuất được xác nhận thành công
//        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
//        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
//
//
//        // Gọi phương thức confirmExport
//        exportSlipService.confirmExport(1L);
//
//        // Kiểm tra xem trạng thái đã được cập nhật thành CONFIRMED
//        assertEquals(OrderStatus.CONFIRMED, exportSlip.getStatus());
//
//        // Kiểm tra xem phương thức gửi thông báo có được gọi hay không
//        verify(notificationService, times(1)).sendNotificationToUser(
//                anyString(), anyString(), eq(exportSlip.getUser()), eq(exportSlip.getId()));
//    }
//
//    @Test
//    void testConfirmExport_ProcessStockCalled() {
//        // Test kiểm tra xem phương thức processStockForConfirmedExport có được gọi không
//        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
//        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
//
//
//        // Gọi phương thức confirmExport
//        exportSlipService.confirmExport(1L);
//
//    }
//
//    @Test
//    void testConfirmExport_SendNotification() {
//        // Test kiểm tra xem thông báo có được gửi đi không
//        when(exportSlipRepository.findById(1L)).thenReturn(Optional.of(exportSlip));
//        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
//
//
//        // Gọi phương thức confirmExport
//        exportSlipService.confirmExport(1L);
//
//        String url = "/export/receipt/detail/" +  exportSlip.getId();
//
//        // Kiểm tra xem thông báo có được gửi đi không
//        verify(notificationService, times(1)).sendNotificationToUser(
//                eq("Phiếu xuất đã được xác nhận"),
//                eq("Phiếu xuất của bạn đã được chủ cửa hàng xác nhận."),
//                eq(exportSlip.getUser()),
//                eq(url));
//    }
//
//}
