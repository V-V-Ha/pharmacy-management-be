package com.fu.pha.Service;

import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Unit;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.service.impl.CategoryServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private CategoryDto categoryDto;
    private Category category;

    private MockedStatic<SecurityContextHolder> securityContextHolderMockedStatic;

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


}
