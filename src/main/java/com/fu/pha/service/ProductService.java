package com.fu.pha.service;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    void createProduct(ProductDTORequest request);
    void updateProduct(ProductDTORequest request);
    Page<ProductDTOResponse> getAllProductPaging(int size, int index, String productName, String category);
    ProductDTOResponse getProductById(Long id);

}
