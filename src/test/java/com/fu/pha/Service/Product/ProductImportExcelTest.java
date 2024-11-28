package com.fu.pha.Service.Product;

import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.Unit;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.impl.ProductServiceImpl;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductImportExcelTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private ProductServiceImpl productService;

    private Workbook workbook;
    private Sheet sheet;
    private Row row;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mocks a basic Excel file with Apache POI
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet();
        row = sheet.createRow(0);

        // Tạo dữ liệu giả trong dòng đầu tiên của Excel (giả sử có các cột phù hợp)
        createTestExcelRow(row);
    }

    private void createTestExcelRow(Row row) {
        row.createCell(0).setCellValue("Product Name");
        row.createCell(1).setCellValue("Category");
        row.createCell(2).setCellValue("Registration Number");
        row.createCell(3).setCellValue("Active Ingredient");
        row.createCell(4).setCellValue("Dosage Concentration");
        row.createCell(5).setCellValue("Packing Method");
        row.createCell(6).setCellValue("Manufacturer");
        row.createCell(7).setCellValue("Country");
        row.createCell(8).setCellValue("Dosage Form");
        row.createCell(9).setCellValue(10);
        row.createCell(14).setCellValue("Có");  // Prescription drug
        row.createCell(15).setCellValue("Indication");
        row.createCell(16).setCellValue("Contraindication");
        row.createCell(17).setCellValue("Side Effect");
        row.createCell(18).setCellValue("Description");
    }

    @Test
    void testImportProductsFromExcel_ValidData() throws Exception {
        // Mock category repository to return a category for the test
        Category category = new Category();
        category.setCategoryName("Category");
        category.setId(1L);
        when(categoryRepository.findByCategoryName("Category")).thenReturn(Optional.of(category));

        // Mock unit repository to return a unit for the test
        Unit unit = new Unit();
        unit.setUnitName("Unit");
        unit.setId(1L);
        when(unitRepository.findByUnitName("Unit")).thenReturn(unit);

        // Prepare a valid Excel file with data
        MultipartFile mockFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new FileInputStream(new File("path/to/mock/file.xlsx")));

        // Call method to import products from Excel
        productService.importProductsFromExcel(mockFile);

        // Verify that productRepository.save() was called
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testImportProductsFromExcel_EmptyFile() throws Exception {
        // Prepare an empty Excel file
        Sheet emptySheet = workbook.createSheet("EmptySheet");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));  // Empty input stream

        // Test with empty file input
        productService.importProductsFromExcel(file);

        // Verify no products were saved
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testImportProductsFromExcel_InvalidCategory() throws Exception {
        // Prepare an Excel file where category is not found
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("Test Product");
        row.createCell(1).setCellValue("NonExistentCategory");
        when(categoryRepository.findByCategoryName("NonExistentCategory")).thenReturn(Optional.empty());

        // Prepare mock file
        MultipartFile mockFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream("mock data".getBytes()));

        // Call the method and expect exception
        assertThrows(ResourceNotFoundException.class, () -> productService.importProductsFromExcel(mockFile));
    }

    @Test
    void testImportProductsFromExcel_InvalidUnit() throws Exception {
        // Prepare Excel file with valid data but invalid unit
        Row row = sheet.createRow(1);
        row.createCell(10).setCellValue("InvalidUnit");
        when(unitRepository.findByUnitName("InvalidUnit")).thenReturn(null); // No such unit

        // Prepare mock file
        MultipartFile mockFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream("mock data".getBytes()));

        // Call method and expect exception
        assertThrows(ResourceNotFoundException.class, () -> productService.importProductsFromExcel(mockFile));
    }

    @Test
    void testImportProductsFromExcel_InvalidPrescriptionDrugValue() throws Exception {
        // Test case where "Có"/"Không" value for prescription drug is incorrect
        Row row = sheet.createRow(1);
        row.createCell(14).setCellValue("InvalidValue"); // Invalid prescription drug value

        // Prepare mock file
        MultipartFile mockFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream("mock data".getBytes()));

        // Expect IllegalArgumentException for invalid value
        assertThrows(IllegalArgumentException.class, () -> productService.importProductsFromExcel(mockFile));
    }

    @Test
    void testImportProductsFromExcel_IOException() throws Exception {
        // Simulate IOException when trying to read the file
        when(file.getInputStream()).thenThrow(new IOException("File reading error"));

        // Expect IOException to be thrown
        assertThrows(IOException.class, () -> productService.importProductsFromExcel(file));
    }
}
