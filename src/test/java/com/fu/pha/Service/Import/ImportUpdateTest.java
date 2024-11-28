package com.fu.pha.Service.Import;

import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.enums.OrderStatus;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.*;
import com.fu.pha.repository.*;
import com.fu.pha.service.impl.ImportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;


@ExtendWith(MockitoExtension.class)
public class ImportUpdateTest {

    @Mock
    private ImportRepository importRepository;
    @Mock
    private ProductUnitRepository productUnitRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ImportItemRepository importItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ImportServiceImpl importService;

    private ImportDto importDto;
    private MockMultipartFile file;
    private Import existingImport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Giả lập ImportDto và file upload
        importDto = new ImportDto();
        importDto.setUserId(1L);
        importDto.setSupplierId(1L);
        importDto.setPaymentMethod(PaymentMethod.CASH);
        importDto.setNote("Test note");
        importDto.setTax(10.0);
        importDto.setDiscount(5.0);

        file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[1]);

        // Giả lập Import đã tồn tại
        existingImport = new Import();
        existingImport.setId(1L);
        existingImport.setStatus(OrderStatus.PENDING);

        // Giả lập các đối tượng cần thiết
        Product product = new Product();
        product.setId(1L);

        User user = new User();
        user.setId(1L);

        Supplier supplier = new Supplier();
        supplier.setId(1L);

        when(importRepository.findById(1L)).thenReturn(Optional.of(existingImport));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
    }

    @Test
    void testUpdateImport_NotFound() {
        when(importRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> importService.updateImport(1L, importDto, file));
    }

    @Test
    void testUpdateImport_NotPending() {
        existingImport.setStatus(OrderStatus.CONFIRMED);

        assertThrows(BadRequestException.class, () -> importService.updateImport(1L, importDto, file));
    }

    @Test
    void testUpdateImport_Unauthorized() {
        existingImport.setUser(new User()); // Giả lập người dùng không phải chủ
        assertThrows(UnauthorizedException.class, () -> importService.updateImport(1L, importDto, file));
    }

    @Test
    void testUpdateImport_Success() {


        ImportItemRequestDto itemDto = new ImportItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(10);
        itemDto.setUnitPrice(100D);
        importDto.setImportItems(Collections.singletonList(itemDto));

        // Giả lập các đối tượng cần thiết cho sản phẩm, phiếu nhập
        Product product = new Product();
        product.setId(1L);
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));

        importService.updateImport(1L, importDto, file);

        // Kiểm tra các phương thức đã được gọi đúng
        verify(importRepository).save(any(Import.class));
        verify(importItemRepository, times(1)).save(any());
    }

    @Test
    void testUpdateImport_TotalAmountNotMatch() {
        importDto.setTotalAmount(500.0); // Số tiền gửi từ client không khớp với số tiền tính
        ImportItemRequestDto itemDto = new ImportItemRequestDto();
        itemDto.setProductId(1L);
        itemDto.setQuantity(10);
        itemDto.setUnitPrice(100D);
        importDto.setImportItems(Collections.singletonList(itemDto));

        assertThrows(BadRequestException.class, () -> importService.updateImport(1L, importDto, file));
    }

    @Test
    void testUpdateImport_WithFileUpload() {


        importService.updateImport(1L, importDto, file);

        verify(cloudinaryService).upLoadFile(file, "import_" + System.currentTimeMillis());
    }
}
