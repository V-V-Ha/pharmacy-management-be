package com.fu.pha.service;

import com.fu.pha.dto.request.CategoryDto;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {
    //create category
    void createCategory(CategoryDto request) throws BadRequestException;

    //update category
    void updateCategory(CategoryDto request) throws BadRequestException;


    //get all category and paging
    Page<CategoryDto> getAllCategoryPaging(int page, int size, String name, String status);

    CategoryDto getCategoryById(Long id);

    void updateCategoryStatus(Long id);

    List<CategoryDto> getAllCategory();

}
