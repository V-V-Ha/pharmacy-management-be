package com.fu.pha.Service.Product;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductUpdateTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTORequest productDTORequest;
    private Product product;
    private Category category;

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
        productDTORequest.setNumberWarning(10);
        productDTORequest.setPrescriptionDrug(false);
        productDTORequest.setProductUnitListDTO(Collections.emptyList());
        productDTORequest.setLastModifiedBy("minhhieu");
        productDTORequest.setLastModifiedDate(Instant.now());

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
        product.setNumberWarning(10);
        product.setPrescriptionDrug(false);
        product.setLastModifiedBy("minhhieu");
        product.setLastModifiedDate(Instant.now());

        category = new Category();
        category.setId(1L);
        category.setStatus(Status.ACTIVE);
    }

    //test trường hợp cập nhật sản phẩm thành công
    @Test
    void UTCPU01() {
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.of(product));
        when(productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.of(category));

        productService.updateProduct(productDTORequest, null);

        verify(productRepository, times(2)).save(any(Product.class));
    }

    //test trường hợp cập nhật sản phẩm không thành công do không tìm thấy sản phẩm
    @Test
    void UTCPU02() {
        productDTORequest.setId(123L);
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường productName null
    @Test
    void UTCPU03() {
        productDTORequest.setProductName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do không tìm thấy category
    @Test
    void UTCPU04() {
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
    void UTCPU05() {
        productDTORequest.setCategoryId(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường registrationNumber null
    @Test
    void UTCPU06() {
        productDTORequest.setRegistrationNumber("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường activeIngredient null
    @Test
    void UTCPU07() {
        productDTORequest.setActiveIngredient("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường dosageConcentration null
    @Test
    void UTCPU08() {
        productDTORequest.setDosageConcentration("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường packingMethod null
    @Test
    void UTCPU09() {
        productDTORequest.setPackingMethod("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường manufacturer null
    @Test
    void UTCPU10() {
        productDTORequest.setManufacturer("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường countryOfOrigin null
    @Test
    void UTCPU11() {
        productDTORequest.setCountryOfOrigin("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường dosageForms null
    @Test
    void UTCPU12() {
        productDTORequest.setDosageForms("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp cập nhật sản phẩm không thành công do trường registrationNumber trùng
    @Test
    void UTCPU13() {
        // Thiết lập số đăng ký cho productDTORequest
        productDTORequest.setRegistrationNumber("TCT-00092-22");

        // Giả lập rằng sản phẩm cần cập nhật đã tồn tại
        when(productRepository.getProductById(productDTORequest.getId())).thenReturn(Optional.of(product));

        // Giả lập rằng một sản phẩm khác có cùng số đăng ký đã tồn tại
        Product duplicateProduct = new Product();
        duplicateProduct.setId(2L); // Đảm bảo đây là một sản phẩm khác với productDTORequest
        duplicateProduct.setRegistrationNumber("TCT-00092-22");
        when(productRepository.findByRegistrationNumber("TCT-00092-22")).thenReturn(Optional.of(duplicateProduct));

        // Kiểm tra ngoại lệ BadRequestException được ném ra do trùng số đăng ký
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.updateProduct(productDTORequest, null);
        });

        // Kiểm tra thông báo lỗi
        assertEquals(Message.EXIST_REGISTRATION_NUMBER, exception.getMessage());
    }


}
