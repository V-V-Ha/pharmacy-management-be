package com.fu.pha.service;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import com.fu.pha.enums.Status;
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
    Page<CategoryDto> getAllCategoryPaging(int page, int size, String name, Status status);

    CategoryDto getCategoryById(Long id);

    //delete category
    void activeCategory(Long id);
    void deActiveCategory(Long id);

    List<CategoryDto> getAllCategory();

}
