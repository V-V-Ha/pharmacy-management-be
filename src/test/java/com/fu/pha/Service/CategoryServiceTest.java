package com.fu.pha.Service;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.entity.Category;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.impl.CategoryServiceImpl;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;


    private Category category;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;


    @BeforeEach
    void setUp() {
        securityContextHolderMockedStatic = mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("minhhieu");

        category = new Category();
        category.setCategoryName("Thuốc ho");
    }

    @AfterEach
    void tearDown() {
        if (securityContextHolderMockedStatic != null) {
            securityContextHolderMockedStatic.close();
        }
    }

    @Test
    void testCreateCategory() throws BadRequestException {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("New Category");
        categoryDto.setDescription("New Description");

        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.empty());

        categoryService.createCategory(categoryDto);

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategoryWithExistingName() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setName("Thuốc ho");
        categoryDto.setDescription("Description");

        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(category));

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(categoryDto));
    }

    @Test
    void testUpdateCategory() throws BadRequestException {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("New Category");
        categoryDto.setDescription("New Description");

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.empty());

        categoryService.updateCategory(categoryDto);

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testUpdateCategoryWithExistingName() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Thuốc ho");
        categoryDto.setDescription("Description");
        Category existingCategory = new Category();
        existingCategory.setId(2L); // Set a different id to simulate existing category with the same name
        existingCategory.setCategoryName("Thuốc ho");

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(existingCategory));

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory(categoryDto));
    }

    @Test
    void testUpdateCategoryWithNullName() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName(null);
        categoryDto.setDescription("Description");

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory(categoryDto));
    }

    @Test
    void testUpdateCategoryWithEmptyName() {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("");
        categoryDto.setDescription("Description");

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory(categoryDto));
    }

}
