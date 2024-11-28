package com.fu.pha.Service.Product;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.ImportItemRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductViewListTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ImportItemRepository importItemRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    // Test case: Found products with valid search criteria
    @Test
    public void testGetListProductForSaleOrderPaging_FoundProducts() {
        // Arrange
        ProductDTOResponse productDTOResponse = new ProductDTOResponse();
        productDTOResponse.setId(1L);
        productDTOResponse.setProductName("Product1");
        productDTOResponse.setTotalQuantity(10);

        List<ProductDTOResponse> productList = List.of(productDTOResponse);
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(productList);
        Pageable pageable = PageRequest.of(0, 10);
        String productName = "Product1";

        when(productRepository.getListProductForSaleOrderPaging(productName, pageable)).thenReturn(expectedPage);
       // Act
        Page<ProductDTOResponse> result = productService.getListProductForSaleOrderPaging(0, 10, productName);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(10, result.getContent().get(0).getTotalQuantity());
    }

    // Test case: No products found
    @Test
    public void testGetListProductForSaleOrderPaging_NoProductsFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String productName = "NonExistentProduct";

        when(productRepository.getListProductForSaleOrderPaging(productName, pageable)).thenReturn(Page.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getListProductForSaleOrderPaging(0, 10, productName);
        });
    }

    // Test case: Products found but totalQuantity is zero
    @Test
    public void testGetListProductForSaleOrderPaging_ZeroTotalQuantity() {
        // Arrange
        ProductDTOResponse productDTOResponse = new ProductDTOResponse();
        productDTOResponse.setId(1L);
        productDTOResponse.setProductName("Product1");
        productDTOResponse.setTotalQuantity(0);

        List<ProductDTOResponse> productList = List.of(productDTOResponse);
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(productList);
        Pageable pageable = PageRequest.of(0, 10);
        String productName = "Product1";

        when(productRepository.getListProductForSaleOrderPaging(productName, pageable)).thenReturn(expectedPage);
       // Act
        Page<ProductDTOResponse> result = productService.getListProductForSaleOrderPaging(0, 10, productName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Test case: productName is null or empty
    @Test
    public void testGetListProductForSaleOrderPaging_ProductNameIsNull() {
        // Arrange
        ProductDTOResponse productDTOResponse = new ProductDTOResponse();
        productDTOResponse.setId(1L);
        productDTOResponse.setProductName("Product1");
        productDTOResponse.setTotalQuantity(10);

        List<ProductDTOResponse> productList = List.of(productDTOResponse);
        Page<ProductDTOResponse> expectedPage = new PageImpl<>(productList);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.getListProductForSaleOrderPaging(null, pageable)).thenReturn(expectedPage);

        // Act
        Page<ProductDTOResponse> result = productService.getListProductForSaleOrderPaging(0, 10, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    // Test case: Invalid page size (less than 1)
    @Test
    public void testGetListProductForSaleOrderPaging_InvalidPageSize() {
        // Arrange
        int invalidSize = 0; // size không hợp lệ
        int page = 0;
        String productName = "Product1";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getListProductForSaleOrderPaging(page, invalidSize, productName);
        });
    }

    // Test case: Invalid page number (negative)
    @Test
    public void testGetListProductForSaleOrderPaging_InvalidPageNumber() {
        // Arrange
        int invalidPage = -1; // page không hợp lệ
        int size = 10;
        String productName = "Product1";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getListProductForSaleOrderPaging(invalidPage, size, productName);
        });
    }

    // Test case: Exception in repository (if any)
    @Test
    public void testGetListProductForSaleOrderPaging_RepositoryException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String productName = "Product1";

        when(productRepository.getListProductForSaleOrderPaging(productName, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getListProductForSaleOrderPaging(0, 10, productName);
        });
    }
}
