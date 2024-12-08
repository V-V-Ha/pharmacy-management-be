package com.fu.pha.Service.Category;
import com.fu.pha.dto.request.CategoryDto;
import com.fu.pha.enums.Status;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryViewListTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    // Test trường hợp lấy danh sách category thành công
    @Test
    public void UTCCL01() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto(1L, "Thuốc Ho", Status.ACTIVE);
        Page<CategoryDto> expectedPage = new PageImpl<>(List.of(categoryDto));
        when(categoryRepository.findAllByNameContaining("Thuốc Ho", Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<CategoryDto> result = categoryService.getAllCategoryPaging(0, 10, "Thuốc Ho", "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository).findAllByNameContaining("Thuốc Ho", Status.ACTIVE, pageable);
    }

    // Test trường hợp không tìm thấy category
    @Test
    public void UTCCL02() {
        // Arrange
        Page<CategoryDto> expectedPage = Page.empty();
        when(categoryRepository.findAllByNameContaining("Thuốc Đau Đầu", Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getAllCategoryPaging(0, 10, "Thuốc Đau Đầu", "ACTIVE"));
        assertEquals(Message.CATEGORY_NOT_FOUND, exception.getMessage());
        verify(categoryRepository).findAllByNameContaining("Thuốc Đau Đầu", Status.ACTIVE, pageable);
    }

    @Test
    public void UTCCL03() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                categoryService.getAllCategoryPaging(0, 10, "Thuốc Ho", "INVALID_STATUS"));
        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp lấy danh sách category với status null
    @Test
    public void UTCCL04() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto(1L, "Thuốc Ho", Status.ACTIVE);
        Page<CategoryDto> expectedPage = new PageImpl<>(List.of(categoryDto));
        when(categoryRepository.findAllByNameContaining("Thuốc Ho", null, pageable)).thenReturn(expectedPage);

        // Act
        Page<CategoryDto> result = categoryService.getAllCategoryPaging(0, 10, "Thuốc Ho", null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository).findAllByNameContaining("Thuốc Ho", null, pageable);
    }

    @Test
    public void testGetAllCategoryPaging_NameNull() {
        // Arrange
        CategoryDto categoryDto = new CategoryDto(1L, "Thuốc Ho", Status.ACTIVE);
        Page<CategoryDto> expectedPage = new PageImpl<>(List.of(categoryDto));
        when(categoryRepository.findAllByNameContaining(null, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<CategoryDto> result = categoryService.getAllCategoryPaging(0, 10, null, "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(categoryRepository).findAllByNameContaining(null, Status.ACTIVE, pageable);
    }
}
