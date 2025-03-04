package com.fu.pha.service.impl;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.CategoryService;
import com.fu.pha.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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

        // Normalize the category name to capitalize the first letter of each word
        String normalizedCategoryName = capitalizeWords(request.getName());

        // Check if the category with the normalized name already exists
        Optional<Category> categoryExist = categoryRepository.findByCategoryName(normalizedCategoryName);
        if (categoryExist.isPresent()) {
            throw new BadRequestException(Message.CATEGORY_EXIST);
        }

        try {
            // Create a new category entity with the normalized name
            Category category = new Category();
            category.setCategoryName(normalizedCategoryName); // Set the formatted name
            category.setDescription(request.getDescription());
            category.setCreateDate(Instant.now());
            category.setCreateBy(SecurityContextHolder.getContext().getAuthentication().getName());
            category.setLastModifiedDate(Instant.now());
            category.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
            category.setStatus(Status.ACTIVE);

            // Save the category to the database
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category", e);
        }
    }

    // Helper method to capitalize the first letter of each word
    private String capitalizeWords(String str) {
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalizedWords.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalizedWords.toString().trim();
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

        // Normalize the category name
        String normalizedCategoryName = capitalizeWords(request.getName());

        // Check for existing category with the same normalized name
        Optional<Category> categoryExist = categoryRepository.findByCategoryName(normalizedCategoryName);
        if (categoryExist.isPresent() && !categoryExist.get().getId().equals(request.getId())) {
            throw new BadRequestException(Message.CATEGORY_EXIST);
        }

        // Update the category entity with the normalized name
        category.setCategoryName(normalizedCategoryName);
        category.setDescription(request.getDescription());
        category.setLastModifiedDate(Instant.now());
        category.setLastModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // Save the updated category to the database
        categoryRepository.save(category);
    }

    @Override
    public Page<CategoryDto> getAllCategoryPaging(int page, int size, String name, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Status categoryStatus = null;
        if (status != null) {
            try {
                categoryStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }
        Page<CategoryDto> categoryPage = categoryRepository.findAllByNameContaining(name, categoryStatus, pageable);
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
    public void updateCategoryStatus(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        // Chuyển đổi trạng thái
        if (category.getStatus() == Status.ACTIVE) {
            category.setStatus(Status.INACTIVE);
        } else {
            category.setStatus(Status.ACTIVE);
        }
        categoryRepository.save(category);
    }

    @Override
    public List<CategoryDto> getAllCategory() {
        return categoryRepository.findAllCategory();
    }

}
