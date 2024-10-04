package com.fu.pha.service.impl;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.dto.request.UserDto;
import com.fu.pha.dto.response.MessageResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.CategoryService;
import com.fu.pha.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;


    @Override
    public void createCategory(CategoryDto request) {
        // Validate the request
        if (request == null || request.getName() == null || request.getName().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }
        // existing category
        Optional<Category> categoryExist = categoryRepository.findByCategoryName(request.getName());
        if (categoryExist.isPresent()) {
            throw new BadRequestException(Message.CATEGORY_EXIST);
        }

        try {
            // Create a new category entity
            Category category = new Category();
            category.setCategoryName(request.getName());
            category.setDescription(request.getDescription());
            category.setCreateDate(Instant.now());
            category.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
            category.setLastModifiedDate(Instant.now());
            category.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

            // Save the category to the database
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category", e);
        }
    }


    @Override
    @Transactional
    public void updateCategory(CategoryDto request) {
        // Validate the request
        if (request == null || request.getName() == null || request.getName().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }

        // Find the existing category
        Optional<Category> categoryOptional = categoryRepository.findById(request.getId());
        Category category = categoryOptional.orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        // Check for existing category with the same name
        Optional<Category> categoryExist = categoryRepository.findByCategoryName(request.getName());
        if (categoryExist.isPresent() && !categoryExist.get().getId().equals(request.getId())) {
            throw new BadRequestException(Message.CATEGORY_EXIST);
        }

        // Update the category entity
        category.setCategoryName(request.getName());
        category.setDescription(request.getDescription());
        category.setLastModifiedDate(Instant.now());
        category.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the updated category to the database
        categoryRepository.save(category);
    }


    @Override
    public Page<CategoryDto> getAllCategoryPaging(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryDto> categoryPage = categoryRepository.findAllByNameContaining(name, pageable);
        if(categoryPage.isEmpty()){
            throw new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND);
        }
        return categoryPage;
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));
        return new CategoryDto(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));
        //soft delete
        category.setDeleted(true);
        categoryRepository.save(category);
    }

}
