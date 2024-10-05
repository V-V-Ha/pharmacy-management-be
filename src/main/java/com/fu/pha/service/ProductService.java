package com.fu.pha.service;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    void createProduct(ProductDTORequest request, MultipartFile file);
    void updateProduct(ProductDTORequest request, MultipartFile file);
    Page<ProductDTOResponse> getAllProductPaging(int size, int index, String productName, String category);
    ProductDTOResponse getProductById(Long id);
    void deleteProduct(Long id);
}
