package com.fu.pha.Service;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.ProductUnitRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductUnitRepository productUnitRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTORequest productDTORequest;
    private Product product;
    private Category category;

    @BeforeEach
    void setUpCreate() {
        productDTORequest = new ProductDTORequest();
        productDTORequest.setProductName("thuốc ho bổ phế Nam Hà");
        productDTORequest.setCategoryId(1L);
        productDTORequest.setRegistrationNumber("TCT-00092-22");
        productDTORequest.setActiveIngredient("Bạch phàn, Xạ can, Bạc hà");
        productDTORequest.setDosageConcentration("125ml");
        productDTORequest.setPackingMethod("Chai x 125ml");
        productDTORequest.setManufacturer("Nam Hà");
        productDTORequest.setCountryOfOrigin("Việt Nam");
        productDTORequest.setDosageForms("Siro");
        productDTORequest.setProductUnitListDTO(Collections.emptyList());

        product = new Product();
        product.setProductName("thuốc ho bổ phế Nam Hà");
        product.setCategoryId(category);
        product.setRegistrationNumber("TCT-00092-22");
        product.setActiveIngredient("Bạch phàn, Xạ can, Bạc hà");
        product.setDosageConcentration("125ml");
        product.setPackingMethod("Chai x 125ml");
        product.setManufacturer("Nam Hà");
        product.setCountryOfOrigin("Việt Nam");
        product.setDosageForms("Siro");

        category = new Category();
        category.setId(1L);
    }

    //test trường hợp tạo sản phẩm thành công
    @Test
    void testCreateProduct_Success() {
        when(productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber())).thenReturn(Optional.empty());
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.getLastProductCode()).thenReturn(null);

        productService.createProduct(productDTORequest, null);

        verify(productRepository).save(any(Product.class));
        verify(productUnitRepository).saveAll(anyIterable());
    }

    //test trường hợp tạo sản phẩm không thành công do trường productName null
    @Test
    void testCreateProduct_NullProductName() {
        productDTORequest.setProductName("");

         ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do không tìm thấy category
   @Test
    void testCreateProduct_NotFoundCategory() {
        productDTORequest.setCategoryId(123L);
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường categoryId null
    @Test
    void testCreateProduct_NullCategory() {
        productDTORequest.setCategoryId(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường registrationNumber trùng
    @Test
    void testCreateProduct_DuplicateRegistrationNumber() {
        when(productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber())).thenReturn(Optional.of(product));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.EXIST_REGISTRATION_NUMBER, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường registrationNumber null
    @Test
    void testCreateProduct_NullRegistrationNumber() {
        productDTORequest.setRegistrationNumber("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường activeIngredient null
    @Test
    void testCreateProduct_NullActiveIngredient() {
        productDTORequest.setActiveIngredient("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường dosageConcentration null
    @Test
    void testCreateProduct_NullDosageConcentration() {
        productDTORequest.setDosageConcentration("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường packingMethod null
    @Test
    void testCreateProduct_NullPackingMethod() {
        productDTORequest.setPackingMethod("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường manufacturer null
    @Test
    void testCreateProduct_NullManufacturer() {
        productDTORequest.setManufacturer("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường countryOfOrigin null
    @Test
    void testCreateProduct_NullCountryOfOrigin() {
        productDTORequest.setCountryOfOrigin("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường dosageForms null
    @Test
    void testCreateProduct_NullDosageForms() {
        productDTORequest.setDosageForms("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    @BeforeEach
    void setUpUpdate() {
        productDTORequest = new ProductDTORequest();
        productDTORequest.setId(1L);
        productDTORequest.setProductName("thuốc ho bổ phế Nam Hà");
        productDTORequest.setCategoryId(1L);
        productDTORequest.setRegistrationNumber("TCT-00092-22");
        productDTORequest.setActiveIngredient("Bạch phàn, Xạ can, Bạc hà");
        productDTORequest.setDosageConcentration("125ml");
        productDTORequest.setPackingMethod("Chai x 125ml");
        productDTORequest.setManufacturer("Nam Hà");
        productDTORequest.setCountryOfOrigin("Việt Nam");
        productDTORequest.setDosageForms("Siro");
        productDTORequest.setProductUnitListDTO(Collections.emptyList());

        product = new Product();
        product.setId(1L);
        product.setProductName("thuốc ho bổ phế Nam Hà");
        product.setCategoryId(category);
        product.setRegistrationNumber("TCT-00092-22");
        product.setActiveIngredient("Bạch phàn, Xạ can, Bạc hà");
        product.setDosageConcentration("125ml");
        product.setPackingMethod("Chai x 125ml");
        product.setManufacturer("Nam Hà");
        product.setCountryOfOrigin("Việt Nam");
        product.setDosageForms("Siro");

        category = new Category();
        category.setId(1L);
    }

    //test trường hợp cập nhật sản phẩm thành công
    @Test
    void testUpdateProduct_Success() {
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.of(product));
        when(productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.of(category));

        productService.updateProduct(productDTORequest, null);

        verify(productRepository, times(2)).save(any(Product.class));
    }

    //test trường hợp cập nhật sản phẩm không thành công do không tìm thấy sản phẩm
    @Test
    void testUpdateProduct_NotFoundProduct() {
        productDTORequest.setId(123L);
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường productName null
    @Test
    void testUpdateProduct_NullProductName() {
        productDTORequest.setProductName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do không tìm thấy category
    @Test
    void testUpdateProduct_NotFoundCategory() {
        // Thiết lập ID của danh mục không tồn tại
        productDTORequest.setCategoryId(123L);

        // Giả lập hành vi để xác định rằng sản phẩm tồn tại, nhưng danh mục thì không
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường categoryId null
    @Test
    void testUpdateProduct_NullCategory() {
        productDTORequest.setCategoryId(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường registrationNumber trùng
    @Test
    void testUpdateProduct_DuplicateRegistrationNumber() {
        // Giả lập rằng sản phẩm cần cập nhật đã tồn tại
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.of(product));

        // Giả lập rằng một sản phẩm khác có cùng số đăng ký đã tồn tại
        Product duplicateProduct = new Product();
        duplicateProduct.setId(2L); // Đảm bảo đây là một sản phẩm khác với productDTORequest
        duplicateProduct.setRegistrationNumber(productDTORequest.getRegistrationNumber());
        when(productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber())).thenReturn(Optional.of(duplicateProduct));

        // Kiểm tra ngoại lệ BadRequestException được ném ra do trùng số đăng ký
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        // Kiểm tra thông báo lỗi
        assertEquals(Message.EXIST_REGISTRATION_NUMBER, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường registrationNumber null
    @Test
    void testUpdateProduct_NullRegistrationNumber() {
        productDTORequest.setRegistrationNumber("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường activeIngredient null
    @Test
    void testUpdateProduct_NullActiveIngredient() {
        productDTORequest.setActiveIngredient("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường dosageConcentration null
    @Test
    void testUpdateProduct_NullDosageConcentration() {
        productDTORequest.setDosageConcentration("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường packingMethod null
    @Test
    void testUpdateProduct_NullPackingMethod() {
        productDTORequest.setPackingMethod("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường manufacturer null
    @Test
    void testUpdateProduct_NullManufacturer() {
        productDTORequest.setManufacturer("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường countryOfOrigin null
    @Test
    void testUpdateProduct_NullCountryOfOrigin() {
        productDTORequest.setCountryOfOrigin("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường dosageForms null
    @Test
    void testUpdateProduct_NullDosageForms() {
        productDTORequest.setDosageForms("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp lấy sản phẩm theo id thành công
    @Test
    void testGetProductById_Success() {
        // Tạo đối tượng Product và Category để giả lập dữ liệu
        Long productId = 3L;
        Product product = new Product();
        product.setId(productId);

        Category category = new Category();
        category.setId(1L);
        category.setCategoryName("Thuốc Ho");

        product.setCategoryId(category); // Thiết lập Category cho Product
        product.setProductUnitList(Collections.emptyList()); // Đảm bảo không bị NullPointerException

        // Giả lập repository để trả về product khi gọi `getProductById`
        when(productRepository.getProductById(productId)).thenReturn(Optional.of(product));

        // Gọi phương thức và kiểm tra kết quả
        ProductDTOResponse productDTOResponse = productService.getProductById(productId);

        assertNotNull(productDTOResponse);
        assertEquals(productId, productDTOResponse.getId());
        assertEquals("Thuốc Ho", productDTOResponse.getCategoryName());
    }

    //test trường hợp lấy sản phẩm theo id không thành công do không tìm thấy sản phẩm
    @Test
    void testGetProductById_NotFoundProduct() {
        Long productId = 200L;

        // Giả lập repository để trả về Optional.empty() khi gọi getProductById
        when(productRepository.getProductById(productId)).thenReturn(Optional.empty());

        // Kiểm tra xem ngoại lệ ResourceNotFoundException có được ném ra hay không
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(productId);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp xóa sản phẩm thành công
    @Test
    void testDeleteProduct_Success() {
        Long productId = 3L;
        Product product = new Product();
        product.setId(productId);

        when(productRepository.getProductById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        assertTrue(product.getDeleted());
        verify(productRepository).save(product);
    }

    //test trường hợp xóa sản phẩm không thành công do không tìm thấy sản phẩm
    @Test
    void testDeleteProduct_NotFoundProduct() {
        Long productId = 200L;

        when(productRepository.getProductById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(productId);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }
}