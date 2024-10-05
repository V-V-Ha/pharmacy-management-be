package com.fu.pha.Service;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Instant;
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

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTORequest productDTORequest;
    private Product product;
    private Category category;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("minhhieu");


        category = new Category();
        category.setCategoryName("Thuốc ho");

        product = new Product();
        product.setId(1L);
        product.setProductName("Paracetamol");
        product.setCategoryId(category);
        product.setRegistrationNumber("REG12345");
        product.setActiveIngredient("Paracetamol");
        product.setDosageConcentration("10mg");
        product.setPackingMethod("Hộp 10 vỉ");
        product.setManufacturer("Bayer AG");
        product.setCountryOfOrigin("Việt Nam");
        product.setImportPrice(Double.valueOf(100));
        product.setProductCode("PCT12345");
        product.setIndication("Giảm đau");
        product.setContraindication("Bệnh gan");
        product.setSideEffect("Buồn nôn");
        product.setDosageForms("nước, bột");
        product.setDescription("thuốc giảm đau đầu");
        product.setImageProduct("Image1");
        product.setCreateDate(Instant.now());
        product.setCreateBy("minhhieu");
        product.setLastModifiedDate(Instant.now());
        product.setLastModifiedBy("minhhieu");

        productDTORequest = new ProductDTORequest(product);
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMockedStatic != null) {
            securityContextHolderMockedStatic.close();
        }
    }

    @Test
    void testCreateProduct() {
        when(productRepository.findByProductCode(anyString())).thenReturn(Optional.empty());
        when(productRepository.findByRegistrationNumber(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.getCategoryByCategoryName(anyString())).thenReturn(Optional.ofNullable(category));

        productService.createProduct(productDTORequest, null);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct() {
        when(productRepository.getProductById(anyLong())).thenReturn(Optional.ofNullable(product));
        when(productRepository.findByProductCode(anyString())).thenReturn(null);
        when(productRepository.findByRegistrationNumber(anyString())).thenReturn(null);
        when(categoryRepository.getCategoryByCategoryName(anyString())).thenReturn(Optional.ofNullable(category));

        productService.createProduct(productDTORequest, null);

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetAllProductPaging() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductDTOResponse> page = new PageImpl<>(Collections.singletonList(new ProductDTOResponse(product)));

        when(productRepository.getListProductPaging(anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        Page<ProductDTOResponse> result = productService.getAllProductPaging(0, 10, "Product1", "Category1");

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetProductById() {
        when(productRepository.getProductById(anyLong())).thenReturn(Optional.ofNullable(product));

        ProductDTOResponse result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getProductName());
    }

    @Test
    void testCreateProductWithExistingProductCode() {
        when(productRepository.findByProductCode(anyString())).thenReturn(Optional.of(new Product()));

        assertThrows(BadRequestException.class, () -> productService.createProduct(productDTORequest, null));
    }

    @Test
    void testCreateProductWithExistingRegistrationNumber() {
        when(productRepository.findByRegistrationNumber(anyString())).thenReturn(Optional.of(new Product()));

        assertThrows(BadRequestException.class, () -> productService.createProduct(productDTORequest, null));
    }

    @Test
    void testUpdateProductNotFound() {
        when(productRepository.getProductById(anyLong())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(productDTORequest, null));
    }
}