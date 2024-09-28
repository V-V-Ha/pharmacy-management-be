package com.fu.pha.controller;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.service.CategoryService;
import com.fu.pha.exception.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

     @PostMapping("/create-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER') and hasRole('STOCK')")
     public ResponseEntity<Object> createCategory(@RequestBody CategoryDto request) {
         categoryService.createCategory(request);
         return ResponseEntity.ok(new MessageResponse(Message.CREATE_SUCCESS, HttpStatus.OK.value()));
     }

     @PostMapping("/update-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER') and hasRole('STOCK')")
     public ResponseEntity<Object> updateCategory(@RequestBody CategoryDto request) {
         categoryService.updateCategory(request);
         return ResponseEntity.ok(new MessageResponse(Message.UPDATE_SUCCESS, HttpStatus.OK.value()));
     }

     @GetMapping("/get-all-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER') and hasRole('STOCK')")
        public ResponseEntity<Object> getAllCategoryPaging(@RequestParam int page,
                                                           @RequestParam(required = false) String name) {
            int size = 10;
            Page<CategoryDto> content = categoryService.getAllCategoryPaging(page, size, name);

         PageResponseModel<CategoryDto> response = PageResponseModel.<CategoryDto>builder()
                 .page(page)
                 .size(size)
                 .total(content.getTotalElements())
                 .listData(content.getContent())
                 .build();

            return ResponseEntity.ok(response);
        }

}
