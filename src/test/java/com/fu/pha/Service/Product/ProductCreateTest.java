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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductCreateTest {

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
        productDTORequest.setNumberWarning(10);
        productDTORequest.setPrescriptionDrug(false);
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
        product.setNumberWarning(10);
        product.setPrescriptionDrug(false);

        category = new Category();
        category.setId(1L);
        category.setStatus(Status.ACTIVE);
    }

    //test trường hợp tạo sản phẩm thành công
    @Test
    void UTCPC01() {
        when(productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber())).thenReturn(Optional.empty());
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.of(category));
        when(productRepository.getLastProductCode()).thenReturn(null);

        productService.createProduct(productDTORequest, null);

        verify(productRepository, times(2)).save(any(Product.class));
        verify(productUnitRepository).saveAll(anyIterable());
    }

    //test trường hợp tạo sản phẩm không thành công do trường productName null
    @Test
    void UTCPC02() {
        productDTORequest.setProductName("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do không tìm thấy category
    @Test
    void UTCPC03() {
        productDTORequest.setCategoryId(123L);
        when(categoryRepository.findById(productDTORequest.getCategoryId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường categoryId null
    @Test
    void UTCPC04() {
        productDTORequest.setCategoryId(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường registrationNumber null
    @Test
    void UTCPC05() {
        productDTORequest.setRegistrationNumber("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường activeIngredient null
    @Test
    void UTCPC06() {
        productDTORequest.setActiveIngredient("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường dosageConcentration null
    @Test
    void UTCPC07() {
        productDTORequest.setDosageConcentration("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường packingMethod null
    @Test
    void UTCPC08() {
        productDTORequest.setPackingMethod("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường manufacturer null
    @Test
    void UTCPC09() {
        productDTORequest.setManufacturer("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường countryOfOrigin null
    @Test
    void UTCPC10() {
        productDTORequest.setCountryOfOrigin("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường dosageForms null
    @Test
    void UTCPC11() {
        productDTORequest.setDosageForms("");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp tạo sản phẩm không thành công do trường registrationNumber trùng
    @Test
    void UTCPC12() {
        // Thiết lập số đăng ký cho productDTORequest
        productDTORequest.setRegistrationNumber("TCT-00092-22");

        // Giả lập kết quả trả về của productRepository khi tìm kiếm số đăng ký đã tồn tại
        when(productRepository.findByRegistrationNumber("TCT-00092-22")).thenReturn(Optional.of(product));

        // Kiểm tra xem phương thức createProduct có ném ra BadRequestException khi số đăng ký đã tồn tại
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.createProduct(productDTORequest, null);
        });

        // Kiểm tra thông điệp lỗi có khớp với thông điệp mong muốn
        assertEquals(Message.EXIST_REGISTRATION_NUMBER, exception.getMessage());
    }

}
