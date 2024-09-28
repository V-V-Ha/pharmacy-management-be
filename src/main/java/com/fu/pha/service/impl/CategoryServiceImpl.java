package com.fu.pha.service.impl;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.exception.CustomUpdateException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;


    @Override
    public void createCategory(CategoryDto request) {
        // Validate the request
        if (request == null || request.getName() == null || request.getName().isEmpty()) {
            throw new CustomUpdateException(Message.NULL_FILED);
        }
        //existing category
        Category categoryExist = categoryRepository.findByName(request.getName());
        if (categoryExist != null) {
            throw new CustomUpdateException(Message.CATEGORY_EXIST);
        }

        // Create a new category entity
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setCreateDate(Instant.now());
        category.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
        category.setLastModifiedDate(Instant.now());
        category.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the category to the database
        categoryRepository.save(category);
    }


    @Override
    public void updateCategory(CategoryDto request) {
        // Validate the request
        if (request == null || request.getId() == null || request.getName() == null || request.getName().isEmpty()) {
            throw new CustomUpdateException(Message.NULL_FILED);
        }

        // Find the existing category by ID
        Category existingCategory = categoryRepository.findById(request.getId()).orElse(null);
        if (existingCategory == null) {
            throw new CustomUpdateException(Message.CATEGORY_NOT_FOUND);
        }
        // Check if the category name is already taken
        Category category = categoryRepository.findByName(request.getName());
        if (category != null && !category.getId().equals(request.getId())) {
            throw new CustomUpdateException(Message.CATEGORY_EXIST);
        }

        // Update the category fields
        existingCategory.setName(request.getName());
        existingCategory.setDescription(request.getDescription());
        existingCategory.setLastModifiedDate(Instant.now());
        existingCategory.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the updated category to the database
        categoryRepository.save(existingCategory);
    }

    @Override
    public ResponseEntity<Object> deleteCategory(Long id) {
        //condition delete if
        //if
        return null;
    }

    @Override
    public Page<CategoryDto> getAllCategoryPaging(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDto> categoryPage = categoryRepository.findAllByNameContaining(name, pageable);
        if(categoryPage.isEmpty()){
            throw new CustomUpdateException(Message.CATEGORY_NOT_FOUND);
        }
        return categoryPage;
    }

}
