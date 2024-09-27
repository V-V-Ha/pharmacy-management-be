package com.fu.pha.service;

import com.fu.pha.dto.request.CategoryDto;
import org.springframework.http.ResponseEntity;

public interface CategoryService {
    //create category
    void createCategory(CategoryDto request);

    //update category
    void updateCategory(CategoryDto request);

    //delete category
    ResponseEntity<Object> deleteCategory(Long id);

    //get all category and paging
    ResponseEntity<Object> getAllCategoryPaging(int page, int size, String name);

}
