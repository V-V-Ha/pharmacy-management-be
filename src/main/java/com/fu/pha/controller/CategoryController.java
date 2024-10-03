package com.fu.pha.controller;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.User;
import com.fu.pha.service.CategoryService;
import com.fu.pha.exception.Message;
import org.apache.coyote.BadRequestException;
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
     @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
        public ResponseEntity<String> createCategory(@RequestBody CategoryDto request) throws BadRequestException {
            categoryService.createCategory(request);
            return ResponseEntity.ok(Message.CREATE_SUCCESS);
        }

     @PutMapping("/update-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
     public ResponseEntity<String> updateCategory(@RequestBody CategoryDto request) throws BadRequestException {
            categoryService.updateCategory(request);
            return ResponseEntity.ok(Message.UPDATE_SUCCESS);
     }

     //get category by id
    @GetMapping("/get-category-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<CategoryDto> getCategoryById(@RequestParam Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

     @GetMapping("/get-all-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
        public ResponseEntity<PageResponseModel<CategoryDto>> getAllCategoryPaging(@RequestParam int page,
                                                                                    @RequestParam int size,
                                                           @RequestParam(required = false) String name) {

            Page<CategoryDto> content = categoryService.getAllCategoryPaging(page, size, name);

         PageResponseModel<CategoryDto> response = PageResponseModel.<CategoryDto>builder()
                 .page(page)
                 .size(size)
                 .total(content.getTotalElements())
                 .listData(content.getContent())
                 .build();
            return ResponseEntity.ok(response);
        }

    @DeleteMapping("/delete-category")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<String> deleteCategory(@RequestParam Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Message.DELETE_SUCCESS);
    }
}
