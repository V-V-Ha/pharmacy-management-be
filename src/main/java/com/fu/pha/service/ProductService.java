package com.fu.pha.service;

import com.fu.pha.dto.request.ProductDTORequest;
import org.springframework.http.ResponseEntity;

public interface ProductService {
    ResponseEntity<Object> createProduct(ProductDTORequest request);
    ResponseEntity<Object> getAllProductPaging(int size, int index, String productName, String category);
}
