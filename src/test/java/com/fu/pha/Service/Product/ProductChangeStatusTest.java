package com.fu.pha.Service.Product;

import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductChangeStatusTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    //test trường hợp change status sản phẩm thành công
    @Test
    void UTCPCS01() {
        Long productId = 3L;
        Product product = new Product();
        product.setId(productId);
        product.setStatus(Status.ACTIVE);

        when(productRepository.getProductById(productId)).thenReturn(Optional.of(product));

        productService.updateProductStatus(productId);

        assertTrue(product.getStatus() == Status.INACTIVE);
        verify(productRepository).save(product);
    }

    //test trường hợp change status sản phẩm không thành công do không tìm thấy sản phẩm
    @Test
    void UTCPCS02() {
        Long productId = 200L;

        when(productRepository.getProductById(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProductStatus(productId);
        });

        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
    }
}
