package com.fu.pha.Service.Product;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductViewDetailTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    //test trường hợp lấy sản phẩm theo id thành công
    @Test
    void UTCPVD01() {
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
    void UTCPVD02() {
        Long productId = 200L;

        // Giả lập repository để trả về Optional.empty() khi gọi getProductById
        when(productRepository.getProductById(productId)).thenReturn(Optional.empty());

        // Kiểm tra xem ngoại lệ ResourceNotFoundException có được ném ra hay không
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(productId);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }

}
