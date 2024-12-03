package com.fu.pha.Service.Product;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
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

    private Product productMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Category categoryMock = new Category();
        categoryMock.setId(1L);
        categoryMock.setCategoryName("Category A");

        productMock = new Product();
        productMock.setId(1L);
        productMock.setProductName("Product A");
        productMock.setTotalQuantity(10);
        productMock.setStatus(Status.ACTIVE);
        productMock.setCategoryId(categoryMock); // Set the Category object
        productMock.setProductUnitList(Collections.emptyList()); // Initialize the productUnitList
        // Set other fields as needed
    }

    @Test
    public void testGetListProductForSaleOrderPaging_FoundProducts() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ProductDTOResponse> productList = Arrays.asList(new ProductDTOResponse(productMock));
        Page<ProductDTOResponse> productPage = new PageImpl<>(productList, pageRequest, productList.size());

        when(productRepository.getListProductPaging(anyString(), anyString(), any(Status.class), eq(pageRequest)))
                .thenReturn(productPage);

        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, "Product A", "Category A", "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).getListProductPaging(anyString(), anyString(), any(Status.class), eq(pageRequest));
    }

    @Test
    public void testGetListProductForSaleOrderPaging_ProductNameIsNull() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ProductDTOResponse> productList = Arrays.asList(new ProductDTOResponse(productMock));
        Page<ProductDTOResponse> productPage = new PageImpl<>(productList, pageRequest, productList.size());

        when(productRepository.getListProductPaging(isNull(), anyString(), any(Status.class), eq(pageRequest)))
                .thenReturn(productPage);

        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, null, "Category A", "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).getListProductPaging(isNull(), anyString(), any(Status.class), eq(pageRequest));
    }

    @Test
    public void testGetListProductForSaleOrderPaging_ZeroTotalQuantity() {
        productMock.setTotalQuantity(0);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ProductDTOResponse> productList = Arrays.asList(new ProductDTOResponse(productMock));
        Page<ProductDTOResponse> productPage = new PageImpl<>(productList, pageRequest, productList.size());

        when(productRepository.getListProductPaging(anyString(), anyString(), any(Status.class), eq(pageRequest)))
                .thenReturn(productPage);

        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, "Product A", "Category A", "ACTIVE");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().get(0).getTotalQuantity());
        verify(productRepository, times(1)).getListProductPaging(anyString(), anyString(), any(Status.class), eq(pageRequest));
    }

    // Test case: No products found
    @Test
    public void testGetListProductForSaleOrderPaging_NoProductsFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String productName = "NonExistentProduct";
        Boolean isPrescription = false;

        when(productRepository.getListProductForSaleOrderPaging(productName, isPrescription,pageable)).thenReturn(Page.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getListProductForSaleOrderPaging(0, 10, productName, isPrescription);
        });
    }

    // Test case: Invalid page size (less than 1)
    @Test
    public void testGetListProductForSaleOrderPaging_InvalidPageSize() {
        // Arrange
        int invalidSize = 0; // size không hợp lệ
        int page = 0;
        String productName = "Product1";
        Boolean isPrescription = false;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getListProductForSaleOrderPaging(page, invalidSize, productName, isPrescription);
        });
    }

    // Test case: Invalid page number (negative)
    @Test
    public void testGetListProductForSaleOrderPaging_InvalidPageNumber() {
        // Arrange
        int invalidPage = -1; // page không hợp lệ
        int size = 10;
        String productName = "Product1";
        Boolean isPrescription = false;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productService.getListProductForSaleOrderPaging(invalidPage, size, productName, isPrescription);
        });
    }

    // Test case: Exception in repository (if any)
    @Test
    public void testGetListProductForSaleOrderPaging_RepositoryException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String productName = "Product1";
        Boolean isPrescription = false;

        when(productRepository.getListProductForSaleOrderPaging(productName,isPrescription ,pageable))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getListProductForSaleOrderPaging(0, 10, productName, isPrescription);
        });
    }
}
