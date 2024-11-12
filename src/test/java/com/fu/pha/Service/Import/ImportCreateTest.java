package com.fu.pha.Service.Import;

import com.fu.pha.dto.request.importPack.ImportDto;
import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
import com.fu.pha.entity.*;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.repository.ImportItemRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.ProductUnitRepository;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.repository.UserRepository;
import com.fu.pha.service.impl.ImportServiceImpl;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ImportCreateTest {

    @Mock
    private ImportRepository importRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductUnitRepository productUnitRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ImportServiceImpl importService;

    private ImportDto importDto;
    private ImportItemRequestDto importItemRequestDto;
    private User user;
    private Supplier supplier;
    private Product product;
    private Validator validator;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);

        supplier = new Supplier();
        supplier.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setTotalQuantity(100);

        importItemRequestDto = new ImportItemRequestDto();
        importItemRequestDto.setProductId(1L);
        importItemRequestDto.setQuantity(10);
        importItemRequestDto.setUnitPrice(100.0);
        importItemRequestDto.setConversionFactor(1);
        importItemRequestDto.setTotalAmount(1000.0);

        importDto = new ImportDto();
        importDto.setUserId(1L);
        importDto.setSupplierId(1L);
        importDto.setImportItems(Collections.singletonList(importItemRequestDto));
        importDto.setTotalAmount(1000.0);

    }

    // Test trường hợp tạo mới phiếu nhập thành công
    @Test
    void UTCIC01() {
        // Setup các mock repository trả về dữ liệu hợp lệ
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));

        // Gọi hàm createImport
        importService.createImport(importDto);

        // Verify lưu thành công vào importRepository và importItemRepository
        verify(importRepository, times(2)).save(any(Import.class));
        verify(importItemRepository, times(importDto.getImportItems().size())).save(any(ImportItem.class));
    }

    // Test trường hợp importItems rỗng
    @Test
    void UTCIC02() {
        // Mock userRepository và supplierRepository trả về dữ liệu hợp lệ
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.of(supplier));

        // Thiết lập importItems rỗng để kiểm tra ngoại lệ
        importDto.setImportItems(Collections.emptyList());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importService.createImport(importDto);
        });

        assertEquals(Message.IMPORT_ITEMS_EMPTY, exception.getMessage());
    }

    // Test trường hợp user không tồn tại
    @Test
    void UTCIC03() {
        // Setup userRepository trả về Optional.empty()
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.createImport(importDto);
        });

        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp supplier không tồn tại
    @Test
    void UTCIC04() {
        // Setup userRepository trả về Optional.of(user), supplierRepository trả về Optional.empty()
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.createImport(importDto);
        });

        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp product không tồn tại
    @Test
    void UTCIC05() {
        // Setup mock cho user, supplier, và product
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.getProductById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            importService.createImport(importDto);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp totalAmount không khớp với tổng tiền của importItems
    @Test
    void UTCIC06() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));

        importDto.setTotalAmount(9999.0); // totalAmount không khớp

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importService.createImport(importDto);
        });

        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());
    }

    //test trường hợp conversionFactor = 0
    @Test
    void UTCIC07() {
        // Thiết lập conversionFactor là 0
        importItemRequestDto.setConversionFactor(0);
        importDto.setImportItems(Collections.singletonList(importItemRequestDto));

        // Đảm bảo rằng tất cả các mock cần thiết trả về dữ liệu hợp lệ
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));
        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(new ProductUnit()));

        // Kiểm tra ngoại lệ BadRequestException với message tương ứng
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            importService.createImport(importDto);
        });

        assertEquals(Message.INVALID_CONVERSION_FACTOR, exception.getMessage());
    }

}
