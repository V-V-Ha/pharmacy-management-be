package com.fu.pha.Service;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryViewDetailTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryDto categoryDto;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setCategoryName("Thuốc ho");
    }


    //test trường hợp lấy category theo id thành công
    @Test
    void UTCCVD01() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertEquals(categoryService.getCategoryById(1L).getName(), "Thuốc ho");
    }

    //test trường hợp lấy category theo id không tồn tại
    @Test
    void UTCCVD02() {
        when(categoryRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.getCategoryById(200L);
        });

        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
    }
}
