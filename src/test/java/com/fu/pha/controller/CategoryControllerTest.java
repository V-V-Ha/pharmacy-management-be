//package com.fu.pha.controller;
//
//import com.fu.pha.dto.request.CategoryDto;
//import com.fu.pha.dto.response.PageResponseModel;
//import com.fu.pha.exception.Message;
//import com.fu.pha.service.CategoryService;
//import org.apache.coyote.BadRequestException;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.Collections;
//import java.util.Objects;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class CategoryControllerTest {
//
//    @Mock
//    private CategoryService categoryService;
//
//    @InjectMocks
//    private CategoryController categoryController;
//
//    public CategoryControllerTest() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testCreateCategory() throws BadRequestException {
//        CategoryDto categoryDto = new CategoryDto();
//        doNothing().when(categoryService).createCategory(any(CategoryDto.class));
//
//        ResponseEntity<String> response = categoryController.createCategory(categoryDto);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(Message.CREATE_SUCCESS, response.getBody());
//
//        // Verify the service method was called once
//        verify(categoryService).createCategory(any(CategoryDto.class));
//    }
//
//    @Test
//    void testCreateCategory_BadRequest() throws BadRequestException {
//        CategoryDto categoryDto = new CategoryDto();
//        doNothing().when(categoryService).createCategory(any(CategoryDto.class));
//
//        // Simulate exception
//        doThrow(new BadRequestException("Bad request")).when(categoryService).createCategory(any(CategoryDto.class));
//
//        // Test case for bad request
//        assertThrows(BadRequestException.class, () -> {
//            categoryController.createCategory(categoryDto);
//        });
//    }
//
//    @Test
//    void testUpdateCategory() throws BadRequestException {
//        CategoryDto categoryDto = new CategoryDto();
//        doNothing().when(categoryService).updateCategory(any(CategoryDto.class));
//
//        ResponseEntity<String> response = categoryController.updateCategory(categoryDto);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(Message.UPDATE_SUCCESS, response.getBody());
//
//        // Verify the service method was called once
//        verify(categoryService).updateCategory(any(CategoryDto.class));
//    }
//
//    @Test
//    void testGetCategoryById() {
//        CategoryDto categoryDto = new CategoryDto();
//        when(categoryService.getCategoryById(anyLong())).thenReturn(categoryDto);
//
//        ResponseEntity<CategoryDto> response = categoryController.getCategoryById(1L);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals(categoryDto, response.getBody());
//
//        // Verify the service method was called once
//        verify(categoryService).getCategoryById(anyLong());
//    }
//
//    @Test
//    void testGetAllCategoryPaging() {
//        Page<CategoryDto> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
//        when(categoryService.getAllCategoryPaging(anyInt(), anyInt(), anyString())).thenReturn(page);
//
//        ResponseEntity<PageResponseModel<CategoryDto>> response = categoryController.getAllCategoryPaging(0, 10, "");
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(0, Objects.requireNonNull(response.getBody()).getTotal());
//
//        // Verify the service method was called once
//        verify(categoryService).getAllCategoryPaging(anyInt(), anyInt(), anyString());
//    }
//
//    @Test
//    void testDeleteCategory() {
//       // doNothing().when(categoryService).deleteCategory(anyLong());
//
//       // ResponseEntity<String> response = categoryController.deleteCategory(1L);
//
//      //  assertEquals(HttpStatus.OK, response.getStatusCode());
//    //    assertEquals(Message.DELETE_SUCCESS, response.getBody());
//
//        // Verify the service method was called once
//      //  verify(categoryService).deleteCategory(anyLong());
//    }
//
//    @Test
//    void testDeleteCategory_NotFound() {
//        // Simulate exception when category not found
//       // doThrow(new BadRequestException("Category not found")).when(categoryService).deleteCategory(anyLong());
//
//        assertThrows(BadRequestException.class, () -> {
//            //categoryController.deleteCategory(1L);
//        });
//    }
//}