package com.fu.pha.controller;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.service.ProductService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @PostMapping("/import-products")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<?> importProducts(@RequestParam("file") MultipartFile file) {
        try {
            productService.importProductsFromExcel(file);
            return ResponseEntity.ok(Message.CREATE_SUCCESS);
        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi nhập file Excel: " + e.getMessage());
        }
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

    @PutMapping("/set-warning-number")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> setWarningNumber(@RequestParam Long id, @RequestParam Integer numberWarning) {
        productService.setWarningNumber(id, numberWarning);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @PutMapping("/change-status-product")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> updateProductStatus(@RequestParam Long id) {
        productService.updateProductStatus(id);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        return productService.exportProductsToExcel();
    }

    @GetMapping("/download-template-import")
    public ResponseEntity<Resource> downloadExcelTemplate() {
        try {
            // Gọi service để tạo file Excel template
            productService.exportExcelTemplate();

            // Đường dẫn file template đã được tạo
            String filePath = "Mau_them_san_pham.xlsx";
            Path path = Paths.get(filePath).toAbsolutePath();

            // Kiểm tra file tồn tại
            if (!Files.exists(path)) {
                throw new RuntimeException("File template không tồn tại.");
            }

            // Tạo Resource từ file
            Resource resource = new UrlResource(path.toUri());

            // Kiểm tra resource hợp lệ
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Không thể đọc file template.");
            }

            // Trả file về client với header tải xuống
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tạo hoặc tải xuống file template: " + e.getMessage());
        }
    }

}