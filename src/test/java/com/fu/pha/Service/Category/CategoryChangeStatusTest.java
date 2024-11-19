package com.fu.pha.Service.Category;

import com.fu.pha.entity.Category;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryChangeStatusTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    //test trường hợp change status category thành công
    @Test
    void UTCCCS01() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        categoryService.updateCategoryStatus(categoryId);

        verify(categoryRepository).save(category);
    }

    //test trường hợp change status category không tồn tại
    @Test
    void UTCCCS02() {
        when(categoryRepository.findById(200L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.updateCategoryStatus(200L);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }
}
