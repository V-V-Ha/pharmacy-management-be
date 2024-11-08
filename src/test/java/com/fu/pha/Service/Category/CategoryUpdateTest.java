package com.fu.pha.Service.Category;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryUpdateTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryDto categoryDto;
    private Category category;

    @BeforeEach
    void setUpUpdate() {
        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Thuốc ho");

        category = new Category();
        category.setId(1L);
        category.setCategoryName("Thuốc ho");

        // Mock SecurityContext and Authentication
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        // Set up SecurityContextHolder
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("minhhieu");
        SecurityContextHolder.setContext(securityContext);
    }

    //test trường hợp update category thành công
    @Test
    void UTCCU01() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryName("Thuốc Ho")).thenReturn(Optional.empty());

        categoryService.updateCategory(categoryDto);

        verify(categoryRepository).save(category);
        assertEquals("Thuốc Ho", category.getCategoryName());
        assertEquals("minhhieu", category.getLastModifiedBy());
    }

    //test trường hợp update category không tìm thấy category
    @Test
    void UTCCU02() {

        categoryDto.setId(123L);
        when(categoryRepository.findById(categoryDto.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.updateCategory(categoryDto);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp update category với tên null
    @Test
    void UTCCU03() {
        categoryDto.setName("");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            categoryService.updateCategory(categoryDto);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }


    //test trường hợp update category đã tồn tại
    @Test
    void UTCCU04() {
        Category anotherCategory = new Category();
        anotherCategory.setId(2L);
        anotherCategory.setCategoryName("Thuốc Ho");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryName("Thuốc Ho")).thenReturn(Optional.of(anotherCategory));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            categoryService.updateCategory(categoryDto);
        });

        assertEquals(Message.CATEGORY_EXIST, exception.getMessage());
    }


}
