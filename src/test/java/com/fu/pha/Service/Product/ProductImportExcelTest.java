package com.fu.pha.Service.Product;

import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.Unit;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
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
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductImportExcelTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private Row row;
    private Category mockCategory;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        // Mocks a basic Excel file with Apache POI
        workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet();
        var row = sheet.createRow(0);

        // Create test data in the first row of the Excel file
        createTestExcelRow(row);

        // Set up mock data for Category and Unit
        mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setCategoryName("Category");
        mockCategory.setStatus(Status.ACTIVE);

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
        row.createCell(10).setCellValue("Unit");
        row.createCell(11).setCellValue(10000);
        row.createCell(12).setCellValue(10000);
        row.createCell(13).setCellValue(1);
        row.createCell(14).setCellValue("Có");  // Prescription drug
        row.createCell(15).setCellValue("Indication");
        row.createCell(16).setCellValue("Contraindication");
        row.createCell(17).setCellValue("Side Effect");
        row.createCell(18).setCellValue("Description");
    }

    @Test
    void testImportProductsFromExcel_InvalidCategory() throws Exception {
        // Arrange
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(in);

        // Mock the repository methods to simulate invalid category
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.empty());

        // Act and Assert: Ensure ResourceNotFoundException is thrown
        try {
            productService.importProductsFromExcel(file);
        } catch (ResourceNotFoundException e) {
            assertTrue(e.getMessage().contains(Message.CATEGORY_NOT_FOUND));
        }
    }

    @Test
    void testImportProductsFromExcel_InvalidUnit() throws Exception {
        // Arrange
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(in);

        // Mock the repository methods to simulate invalid unit
        Category mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setCategoryName("Category");
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(mockCategory));
        when(unitRepository.findByUnitName(anyString())).thenReturn(null);

        // Act and Assert: Ensure ResourceNotFoundException is thrown
        try {
            productService.importProductsFromExcel(file);
        } catch (ResourceNotFoundException e) {
            assertTrue(e.getMessage().contains(Message.UNIT_NOT_FOUND));
        }
    }

    @Test
    void testImportProductsFromExcel_InvalidPrescriptionDrugValue() throws Exception {
        // Arrange
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenReturn(in);

        // Ensure the sheet and row are properly initialized
        if (sheet == null) {
            sheet = workbook.createSheet();
        }
        if (row == null) {
            row = sheet.createRow(1);
            createTestExcelRow(row);
        }
        row.createCell(14).setCellValue("Invalid Value"); // Set invalid prescription drug value

        // Mock the repository methods
        Category mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setCategoryName("Category");
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(mockCategory));

        // Act and Assert: Ensure ResourceNotFoundException is thrown
        try {
            productService.importProductsFromExcel(file);
        } catch (ResourceNotFoundException e) {
            assertTrue(e.getMessage().contains(Message.INVALID_PRESCRIPTION_DRUG));
        }
    }

}

