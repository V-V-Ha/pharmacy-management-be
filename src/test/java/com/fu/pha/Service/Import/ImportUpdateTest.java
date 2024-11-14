//package com.fu.pha.Service.Import;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import com.fu.pha.dto.request.importPack.ImportDto;
//import com.fu.pha.dto.request.importPack.ImportItemRequestDto;
//import com.fu.pha.entity.*;
//import com.fu.pha.exception.*;
//import com.fu.pha.repository.*;
//import com.fu.pha.service.impl.ImportServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//
//import java.util.*;
//
//
//@ExtendWith(MockitoExtension.class)
//public class ImportUpdateTest {
//
//    @InjectMocks
//    private ImportServiceImpl importService;
//
//    @Mock
//    private ImportRepository importRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private SupplierRepository supplierRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private ImportItemRepository importItemRepository;
//
//    @Mock
//    private ProductUnitRepository productUnitRepository;
//
//    private ImportDto importDto;
//    private User user;
//    private Supplier supplier;
//    private Product product;
//    private Import importReceipt;
//    private ImportItemRequestDto importItemRequestDto;
//
//    @BeforeEach
//    void setUp() {
//        user = new User();
//        user.setId(1L);
//
//        supplier = new Supplier();
//        supplier.setId(1L);
//
//        product = new Product();
//        product.setId(1L);
//        product.setTotalQuantity(100);
//
//        importReceipt = new Import();
//        importReceipt.setId(1L);
//
//        importItemRequestDto = new ImportItemRequestDto();
//        importItemRequestDto.setProductId(1L);
//        importItemRequestDto.setQuantity(10);
//        importItemRequestDto.setUnitPrice(100.0);
//        importItemRequestDto.setConversionFactor(1);
//        importItemRequestDto.setTotalAmount(1000.0);
//
//        importDto = new ImportDto();
//        importDto.setUserId(1L);
//        importDto.setSupplierId(1L);
//        importDto.setImportItems(Collections.singletonList(importItemRequestDto));
//        importDto.setTotalAmount(1000.0);
//    }
//
//    //Test trường hợp cập nhật phiếu nhập thành công
//    @Test
//    void UTCIU01() {
//        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
//        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));
//        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.emptyList());
//
//        importService.updateImport(1L, importDto);
//
//        verify(importRepository).save(importReceipt);
//        verify(importItemRepository).save(any(ImportItem.class));
//    }
//
//    //Test trường hợp cập nhật phiếu nhập không thành công do không tìm thấy người dùng
//    @Test
//    void UTCIU02() {
//        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));
//        when(userRepository.findById(1L)).thenReturn(Optional.empty());
//
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            importService.updateImport(1L, importDto);
//        });
//
//        assertEquals(Message.USER_NOT_FOUND, exception.getMessage());
//    }
//
//    //Test trường hợp cập nhật phiếu nhập không thành công do không tìm thấy nhà cung cấp
//    @Test
//    void UTCIU03() {
//        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());
//
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            importService.updateImport(1L, importDto);
//        });
//
//        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
//    }
//
//    //Test trường hợp cập nhật phiếu nhập không thành công do không tìm thấy sản phẩm
//    @Test
//    void UTCIU04() {
//        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
//        when(productRepository.getProductById(1L)).thenReturn(Optional.empty());
//
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            importService.updateImport(1L, importDto);
//        });
//
//        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
//    }
//
//    //Test trường hợp cập nhật phiếu nhập không thành công do tổng tiên không khớp
//    @Test
//    void UTCIU05() {
//        importDto.setTotalAmount(5000.0); // Mismatch totalAmount
//
//        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
//        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));
//
//        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
//            importService.updateImport(1L, importDto);
//        });
//
//        assertEquals(Message.TOTAL_AMOUNT_NOT_MATCH, exception.getMessage());
//    }
//
//    //Test trường hợp cập nhật phiếu nhập không thành công do số chuyên đổi không hợp lệ
//    @Test
//    void UTCIU06() {
//        // Thiết lập ImportItemRequestDto với conversion factor không hợp lệ là 0
//        importItemRequestDto.setConversionFactor(0);
//        importDto.setImportItems(Collections.singletonList(importItemRequestDto));
//
//        // Giả lập repository import để trả về một importReceipt khi tìm kiếm theo ID
//        when(importRepository.findById(1L)).thenReturn(Optional.of(importReceipt));
//
//        // Giả lập repository user để trả về một user khi tìm kiếm theo ID
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//
//        // Giả lập repository supplier để trả về một supplier khi tìm kiếm theo ID
//        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
//
//        // Giả lập repository product để trả về một product khi tìm kiếm theo ID
//        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product));
//
//        // Tạo và cấu hình một ImportItem với product không null và remainingQuantity hợp lệ
//        ImportItem importItem = new ImportItem();
//        importItem.setProduct(product); // Gán product để tránh NullPointerException trong service
//        importItem.setRemainingQuantity(10); // Gán remainingQuantity để tránh NullPointerException
//        when(importItemRepository.findByImportId(1L)).thenReturn(Collections.singletonList(importItem));
//
//        // Giả lập ProductUnit với conversion factor hợp lệ để logic trong updateProductUnits được thực thi
//        ProductUnit productUnit = new ProductUnit();
//        productUnit.setConversionFactor(1);
//        when(productUnitRepository.findByProductId(1L)).thenReturn(Collections.singletonList(productUnit));
//
//        // Thực hiện và Kiểm tra - Kỳ vọng BadRequestException do conversion factor không hợp lệ (0)
//        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
//            importService.updateImport(1L, importDto);
//        });
//
//        // Kiểm tra thông báo ngoại lệ trùng khớp với thông báo lỗi cho conversion factor không hợp lệ
//        assertEquals(Message.INVALID_CONVERSION_FACTOR, exception.getMessage());
//    }
//
//    //Test trường hợp cập nhật phiếu nhập không thành công do không tìm thấy phiếu nhập
//    @Test
//    void UTCIU07() {
//        when(importRepository.findById(200L)).thenReturn(Optional.empty());
//
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            importService.updateImport(200L, importDto);
//        });
//
//        assertEquals(Message.IMPORT_NOT_FOUND, exception.getMessage());
//    }
//}
