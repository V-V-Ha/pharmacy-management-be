package com.fu.pha.service;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CategoryService {
    //create category
    void createCategory(CategoryDto request) throws BadRequestException;

    //update category
    void updateCategory(CategoryDto request) throws BadRequestException;


    //get all category and paging
    Page<CategoryDto> getAllCategoryPaging(int page, int size, String name);

    CategoryDto getCategoryById(Long id);

    //delete category
    void deleteCategory(Long id);

    List<CategoryDto> getAllCategory();

}
