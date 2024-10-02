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

        PageResponseModel<ProductDTOResponse> responseModel = PageResponseModel.<ProductDTOResponse>builder()
                .page(page)
                .size(size)
                .total(productDTOResponsePage.getTotalElements())
                .listData(productDTOResponsePage.getContent())
                .build();
        return ResponseEntity.ok(responseModel);
    }


    @PostMapping("/create-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> createProduct(@RequestBody ProductDTORequest productDTORequest) {
        productService.createProduct(productDTORequest);
        return ResponseEntity.ok(Message.PRODUCT_SUCCESS);
    }

    @PutMapping("/update-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateProduct(@RequestBody ProductDTORequest productDTORequest) {
        productService.updateProduct(productDTORequest);
        return ResponseEntity.ok(Message.PRODUCT_UPDATE_SUCCESS);
    }

    @GetMapping("/get-product-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<ProductDTOResponse> getProductById(@RequestParam Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
