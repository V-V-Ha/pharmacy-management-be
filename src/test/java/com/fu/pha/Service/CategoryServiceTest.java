package com.fu.pha.Service;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryDto categoryDto;
    private Category category;

    @BeforeEach
    void setUpCreate() {
        categoryDto = new CategoryDto();
        categoryDto.setName("Thuốc ho");

        category = new Category();
        category.setCategoryName("Thuốc Ho");

        // Thiết lập SecurityContext giả lập
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("minhhieu");
        SecurityContextHolder.setContext(securityContext);
    }

    //test trường hợp tạo category thành công
    @Test
    void testCreateCategory_Success() {
        when(categoryRepository.findByCategoryName("Thuốc Ho")).thenReturn(Optional.empty());

        categoryService.createCategory(categoryDto);

        verify(categoryRepository).save(any(Category.class));
    }

    //test trường hợp tạo category đã tồn tại
    @Test
    void testCreateCategory_CategoryExist() {
        when(categoryRepository.findByCategoryName("Thuốc Ho")).thenReturn(Optional.of(category));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            categoryService.createCategory(categoryDto);
        });

        assertEquals(Message.CATEGORY_EXIST, exception.getMessage());
    }

    //test trường hợp tạo category với tên null
    @Test
    void testCreateCategory_NullCategoryName() {
        categoryDto.setName("");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            categoryService.createCategory(categoryDto);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

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
    void testUpdateCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryName("Thuốc ho")).thenReturn(Optional.empty());

        categoryService.updateCategory(categoryDto);

        verify(categoryRepository).save(category);
        assertEquals("Thuốc ho", category.getCategoryName());
        assertEquals("minhhieu", category.getLastModifiedBy());
    }

    //test trường hợp update category không tìm thấy category
    @Test
    void testUpdateCategory_NotFoundCategory() {

        categoryDto.setId(123L);
        when(categoryRepository.findById(categoryDto.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.updateCategory(categoryDto);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp update category đã tồn tại
    @Test
    void testUpdateCategory_CategoryExist() {
        Category anotherCategory = new Category();
        anotherCategory.setId(2L);
        anotherCategory.setCategoryName("Thuốc ho");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryName("Thuốc ho")).thenReturn(Optional.of(anotherCategory));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            categoryService.updateCategory(categoryDto);
        });

        assertEquals(Message.CATEGORY_EXIST, exception.getMessage());
    }

    //test trường hợp update category với tên null
    @Test
    void testUpdateCategory_NullCategoryName() {
        categoryDto.setName("");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            categoryService.updateCategory(categoryDto);
        });

        assertEquals(Message.NULL_FILED, exception.getMessage());
    }

    //test trường hợp lấy category theo id thành công
    @Test
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertEquals(categoryService.getCategoryById(1L).getName(), "Thuốc ho");
    }

    //test trường hợp lấy category theo id không tồn tại
    @Test
    void testGetCategoryById_CategoryNotFound() {
        when(categoryRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryById(200L);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }

    //test trường hợp xóa category thành công
    @Test
    void testDeleteCategory_Success() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(categoryId);

        assertTrue(category.isDeleted());
        verify(categoryRepository).save(category);
    }

    //test trường hợp xóa category không tồn tại
    @Test
    void testDeleteCategory_CategoryNotFound() {
        when(categoryRepository.findById(200L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.deleteCategory(200L);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }
}
