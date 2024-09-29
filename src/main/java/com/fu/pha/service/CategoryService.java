package com.fu.pha.service;

import com.fu.pha.dto.request.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;

public interface CategoryService {
    //create category
    void createCategory(CategoryDto request);

    //update category
    void updateCategory(CategoryDto request);

    //delete category
    ResponseEntity<Object> deleteCategory(Long id);

    //get all category and paging
    Page<CategoryDto> getAllCategoryPaging(int page, int size, String name);

}
