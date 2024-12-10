package com.fu.pha.Service.Product;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ImportItemRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductViewListTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Pageable pageable;
    @BeforeEach
    public void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    //Test case: Found products with valid search criteria
    @Test
    public void UTCPL01() {
        // Arrange
        ProductDTOResponse productDto = new ProductDTOResponse();
        productDto.setProductName("Thuốc Ho Bảo Thanh");
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(List.of(productDto));
        when(productRepository.getListProductPaging("Thuốc Ho Bảo Thanh", "Thuốc Ho", Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, "Thuốc Ho Bảo Thanh", "Thuốc Ho", "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(productRepository).getListProductPaging("Thuốc Ho Bảo Thanh", "Thuốc Ho", Status.ACTIVE, pageable);
    }

    //Test case: No products found
    @Test
    public void UTCPL02() {
        // Arrange
        when(productRepository.getListProductPaging("bột hapacol", "Thuốc Giảm Đau", Status.ACTIVE, pageable)).thenReturn(Page.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                productService.getAllProductPaging(0, 10, "bột hapacol", "Thuốc Giảm Đau", "ACTIVE"));
        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
        verify(productRepository).getListProductPaging("bột hapacol", "Thuốc Giảm Đau", Status.ACTIVE, pageable);
    }

    //Test case: Get all products by paging with invalid status
    @Test
    public void UTCPL03() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                productService.getAllProductPaging(0, 10, "Thuốc Ho Bảo Thanh", "Thuốc Ho", "INVALID_STATUS"));
        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    //Test case: Get all products by paging with status is null
    @Test
    public void UTCPL04() {
        // Arrange
        ProductDTOResponse productDto = new ProductDTOResponse();
        productDto.setProductName("Product A");
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(List.of(productDto));
        when(productRepository.getListProductPaging("Thuốc Ho Bảo Thanh", "Thuốc Ho", null, pageable)).thenReturn(expectedPage);

        // Act
        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, "Thuốc Ho Bảo Thanh", "Thuốc Ho", null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(productRepository).getListProductPaging("Thuốc Ho Bảo Thanh", "Thuốc Ho", null, pageable);
    }

    //Test case: Get all products by paging with category is null
    @Test
    public void UTCPL05() {
        // Arrange
        ProductDTOResponse productDto = new ProductDTOResponse();
        productDto.setProductName("Thuốc Ho Bảo Thanh");
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(List.of(productDto));
        when(productRepository.getListProductPaging("Thuốc Ho Bảo Thanh", null, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, "Thuốc Ho Bảo Thanh", null, "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(productRepository).getListProductPaging("Thuốc Ho Bảo Thanh", null, Status.ACTIVE, pageable);
    }

    @Test
    public void UTCPL06() {
        // Arrange
        when(productRepository.getListProductPaging("Thuốc Ho Bảo Thanh", "Invalid_Category", Status.ACTIVE, pageable)).thenReturn(Page.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                productService.getAllProductPaging(0, 10, "Thuốc Ho Bảo Thanh", "Invalid_Category", "ACTIVE"));
        assertEquals(Message.PRODUCT_NOT_FOUND, exception.getMessage());
        verify(productRepository).getListProductPaging("Thuốc Ho Bảo Thanh", "Invalid_Category", Status.ACTIVE, pageable);
    }

    @Test
    public void UTCPL07() {
        // Arrange
        ProductDTOResponse productDto = new ProductDTOResponse();
        productDto.setProductName(null);
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(List.of(productDto));
        when(productRepository.getListProductPaging(null, "Thuốc Ho", Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, null, "Thuốc Ho", "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(productRepository).getListProductPaging(null, "Thuốc Ho", Status.ACTIVE, pageable);
    }


}
