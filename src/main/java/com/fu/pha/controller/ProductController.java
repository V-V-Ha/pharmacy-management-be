package com.fu.pha.controller;


import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/get-all-product-paging")
    public ResponseEntity<Object> getAllProductPaging(@RequestParam(defaultValue = "3") int size,
                                                      @RequestParam(defaultValue = "1") int index,
                                                      @RequestParam(defaultValue = "", name = "productName") String productName,
                                                      @RequestParam(defaultValue = "", name = "category") String category) {
        return productService.getAllProductPaging(size, index, productName, category);
    }

    @PostMapping("/create-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<Object> createProduct(@RequestBody ProductDTORequest productDTORequest) {
        return productService.createProduct(productDTORequest);
    }
}
