package com.fu.pha.controller;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/get-all-product-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<ProductDTOResponse>> getAllProductPaging(@RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size,
                                                                                     @RequestParam(defaultValue = "", name = "productName") String productName,
                                                                                     @RequestParam(defaultValue = "", name = "category") String category) {
        Page<ProductDTOResponse> productDTOResponsePage = productService.getAllProductPaging(page, size, productName, category);

        PageResponseModel<ProductDTOResponse> response = PageResponseModel.<ProductDTOResponse>builder()
                .page(page)
                .size(size)
                .total(productDTOResponsePage.getTotalElements())
                .listData(productDTOResponsePage.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<?> createProduct(
            @RequestPart("productDTORequest") ProductDTORequest productDTORequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        productService.createProduct(productDTORequest, file);
        return ResponseEntity.ok(Message.PRODUCT_SUCCESS);
    }

    @PutMapping("/update-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<?> updateProduct(
            @RequestPart("productDTORequest") ProductDTORequest productDTORequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        productService.updateProduct(productDTORequest, file);
        return ResponseEntity.ok(Message.PRODUCT_UPDATE_SUCCESS);
    }

    @GetMapping("/get-product-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<ProductDTOResponse> getProductById(@RequestParam Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @DeleteMapping("/delete-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> deleteProduct(@RequestParam Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Message.PRODUCT_DELETE_SUCCESS);
    }

    @GetMapping("/get-all-unit")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<?> getAllUnit() {
        return ResponseEntity.ok(productService.getAllUnit());
    }
}