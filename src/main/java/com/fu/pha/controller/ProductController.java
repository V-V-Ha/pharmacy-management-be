package com.fu.pha.controller;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/get-all-product-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<ProductDTOResponse>> getAllProductPaging(@RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size,
                                                                                     @RequestParam(required = false) String productName,
                                                                                     @RequestParam(required = false) String category,
                                                                                     @RequestParam(required = false) String status) {
        Page<ProductDTOResponse> productDTOResponsePage = productService.getAllProductPaging(page, size, productName, category, status);

        PageResponseModel<ProductDTOResponse> response = PageResponseModel.<ProductDTOResponse>builder()
                .page(page)
                .size(size)
                .total(productDTOResponsePage.getTotalElements())
                .listData(productDTOResponsePage.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-product-sale-order-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<PageResponseModel<ProductDTOResponse>> getListProductForSaleOrderPaging(@RequestParam(defaultValue = "0") int page,
                                                                                                  @RequestParam(defaultValue = "10") int size,
                                                                                                  @RequestParam(required = false) String productName) {
        Page<ProductDTOResponse> productDTOResponsePage = productService.getListProductForSaleOrderPaging(page, size, productName);

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
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<?> updateProduct(
            @RequestPart("productDTORequest") ProductDTORequest productDTORequest,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        productService.updateProduct(productDTORequest, file);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-product-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<ProductDTOResponse> getProductById(@RequestParam Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/change-status-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateProductStatus(@RequestParam Long id) {
        productService.updateProductStatus(id);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-all-unit")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<?> getAllUnit() {
        return ResponseEntity.ok(productService.getAllUnit());
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        return productService.exportProductsToExcel();
    }

    @PostMapping("/import-excel")
    public ResponseEntity<?> importProductsFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            productService.importProductsFromExcel(file);
            return ResponseEntity.ok("Products imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to import products: " + e.getMessage());
        }
    }

}