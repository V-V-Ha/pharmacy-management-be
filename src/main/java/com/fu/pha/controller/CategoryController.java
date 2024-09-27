package com.fu.pha.controller;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.service.CategoryService;
import com.fu.pha.exception.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

     @PostMapping("/create-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER')")
     public ResponseEntity<Object> createCategory(@RequestBody CategoryDto request) {
         categoryService.createCategory(request);
         return ResponseEntity.ok(new MessageResponse(Message.CREATE_SUCCESS, HttpStatus.OK.value()));
     }

     @PostMapping("/update-category")
     @PreAuthorize("hasRole('PRODUCT_OWNER')")
     public ResponseEntity<Object> updateCategory(@RequestBody CategoryDto request) {
         categoryService.updateCategory(request);
         return ResponseEntity.ok(new MessageResponse(Message.UPDATE_SUCCESS, HttpStatus.OK.value()));
     }


}
