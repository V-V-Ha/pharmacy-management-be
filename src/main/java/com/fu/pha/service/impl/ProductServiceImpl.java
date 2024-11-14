package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.ProductUnitDTOResponse;
import com.fu.pha.entity.*;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.MaxUploadSizeExceededException;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CategoryRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.repository.ProductUnitRepository;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.CloudinaryService;
import com.fu.pha.service.ProductService;
import com.fu.pha.util.FileUploadUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ProductUnitRepository productUnitRepository;

    @Autowired
    GenerateCode generateCode;

    @Autowired
    CloudinaryService cloudinaryService;

    @Transactional
    @Override
    public void createProduct(ProductDTORequest productDTORequest, MultipartFile file) {

        //validate the request
        checkValidateProduct(productDTORequest);

        //check product code and registration number exist
        Optional<Product> registrationNumber = productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber());
        if (registrationNumber.isPresent()) {
            throw new BadRequestException(Message.EXIST_REGISTRATION_NUMBER);
        }

        Category category = categoryRepository.findById(productDTORequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        Product product = new Product();
        product.setProductName(productDTORequest.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(productDTORequest.getRegistrationNumber());
        product.setActiveIngredient(productDTORequest.getActiveIngredient());
        product.setDosageConcentration(productDTORequest.getDosageConcentration());
        product.setPackingMethod(productDTORequest.getPackingMethod());
        product.setManufacturer(productDTORequest.getManufacturer());
        product.setCountryOfOrigin(productDTORequest.getCountryOfOrigin());

        if (productRepository.getLastProductCode() == null) {
            product.setProductCode("SP00001");
        } else {
            product.setProductCode(generateCode.generateNewProductCode(productRepository.getLastProductCode()));
        }
//        product.setProductCode(productDTORequest.getProductCode());
        product.setIndication(productDTORequest.getIndication());
        product.setContraindication(productDTORequest.getContraindication());
        product.setSideEffect(productDTORequest.getSideEffect());
        product.setDosageForms(productDTORequest.getDosageForms());
        product.setDescription(productDTORequest.getDescription());
        product.setPrescriptionDrug(productDTORequest.getPrescriptionDrug());
        product.setStatus(productDTORequest.getStatus());
        // Upload the image product if there is a file
        if (file != null && !file.isEmpty()) {
            String imageProduct = uploadImage(file);
            product.setImageProduct(imageProduct);
        }

        productRepository.save(product);
        List<ProductUnit> productUnitList = new ArrayList<>();
        for (ProductUnitDTORequest productUnitDTORequest : productDTORequest.getProductUnitListDTO()) {
            Unit unit = unitRepository.findUnitById(productUnitDTORequest.getUnitId());
            ProductUnit productUnit = new ProductUnit();
            productUnit.setProduct(product);
            productUnit.setUnit(unit);
            productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
            productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
            productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
            productUnitList.add(productUnit);
        }
        productUnitRepository.saveAll(productUnitList);
    }

    @Override
    @Transactional
    public void updateProduct(ProductDTORequest productDTORequest, MultipartFile file) {
        //validate the request
        checkValidateProduct(productDTORequest);

        //check product exist
        Optional<Product> productOptional = productRepository.getProductById(productDTORequest.getId());
        Product product = productOptional.orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        //check product code and registration number exist
        Optional<Product> registrationNumberExist = productRepository.findByRegistrationNumber(productDTORequest.getRegistrationNumber());
        if (registrationNumberExist.isPresent() && !registrationNumberExist.get().getId().equals(productDTORequest.getId())) {
            throw new BadRequestException(Message.EXIST_REGISTRATION_NUMBER);
        }

        Category category = categoryRepository.findById(productDTORequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(Message.CATEGORY_NOT_FOUND));

        product.setProductName(productDTORequest.getProductName());
        product.setCategoryId(category);
        product.setRegistrationNumber(productDTORequest.getRegistrationNumber());
        product.setActiveIngredient(productDTORequest.getActiveIngredient());
        product.setDosageConcentration(productDTORequest.getDosageConcentration());
        product.setPackingMethod(productDTORequest.getPackingMethod());
        product.setManufacturer(productDTORequest.getManufacturer());
        product.setCountryOfOrigin(productDTORequest.getCountryOfOrigin());
        product.setIndication(productDTORequest.getIndication());
        product.setContraindication(productDTORequest.getContraindication());
        product.setSideEffect(productDTORequest.getSideEffect());
        product.setDosageForms(productDTORequest.getDosageForms());
        product.setDescription(productDTORequest.getDescription());
        product.setPrescriptionDrug(productDTORequest.getPrescriptionDrug());
        product.setStatus(productDTORequest.getStatus());

        // Upload the image product if there is a file
        if (file != null && !file.isEmpty()) {
            String imageProduct = uploadImage(file);
            product.setImageProduct(imageProduct);
        }
        productRepository.save(product);
        if (productDTORequest.getProductUnitListDTO() != null) {
            List<ProductUnit> productUnitList = product.getProductUnitList();

            for (ProductUnitDTORequest productUnitDTORequest : productDTORequest.getProductUnitListDTO()) {

                // Check if the product unit exists
                ProductUnit productUnit = productUnitRepository.findProductUnitsByIdAndProductId(productUnitDTORequest.getId(),
                        productDTORequest.getId());

                if (productUnit != null) {
                    // Update product unit
                    productUnit.setUnit(unitRepository.findUnitById(productUnitDTORequest.getUnitId()));
                    productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
                    productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
                    productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
                    productUnitRepository.save(productUnit);
                } else {
                    // Create a new product unit
                    productUnit = new ProductUnit();
                    Unit unit = unitRepository.findUnitById(productUnitDTORequest.getUnitId());
                    product = productRepository.findProductById(productDTORequest.getId());
                    productUnit.setUnit(unit);
                    productUnit.setProduct(product);
                    productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
                    productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
                    productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
                    productUnitRepository.save(productUnit);
                }

                // Add the product unit to the list
                productUnitList.add(productUnit);
            }

            product.setProductUnitList(productUnitList);
            productRepository.save(product);
        }
    }

    private void checkValidateProduct(ProductDTORequest productDTORequest){
        if(productDTORequest.getProductName().isEmpty() || productDTORequest.getCategoryId() == null ||
                productDTORequest.getRegistrationNumber().isEmpty() || productDTORequest.getActiveIngredient().isEmpty() ||
                productDTORequest.getDosageConcentration().isEmpty() || productDTORequest.getPackingMethod().isEmpty() ||
                productDTORequest.getManufacturer().isEmpty() || productDTORequest.getCountryOfOrigin().isEmpty() ||
                productDTORequest.getDosageForms().isEmpty() || productDTORequest.getStatus() == null){
            throw new ResourceNotFoundException(Message.NULL_FILED);
        }

    }

    public String uploadImage( final MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException(Message.EMPTY_FILE);
        }

        if (!FileUploadUtil.isAllowedExtension(file.getOriginalFilename(), FileUploadUtil.IMAGE_PATTERN)) {
            throw new BadRequestException(Message.INVALID_FILE);
        }

        if (file.getSize() > FileUploadUtil.MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(Message.INVALID_FILE_SIZE);
        }



        String customFileName = UUID.randomUUID() + "image";

        CloudinaryResponse cloudinaryResponse = cloudinaryService.upLoadFile(file, FileUploadUtil.getFileName(customFileName));

        return cloudinaryResponse.getUrl();
    }

    @Override
    public Page<ProductDTOResponse> getAllProductPaging(int page, int size,  String productName, String category) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTOResponse> products = productRepository.getListProductPaging(productName, category, pageable);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        return products;
    }

    @Override
    public ProductDTOResponse getProductById(Long id) {
        Product product = productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));
        return new ProductDTOResponse(product);
    }

    @Override
    public void activeProduct(Long id) {
        Product product = productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        product.setStatus(true);
        productRepository.save(product);
    }

    @Override
    public void deActiveProduct(Long id) {
        Product product = productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        product.setStatus(false);
        productRepository.save(product);
    }

    @Override
    public List<UnitDto> getAllUnit() {
        List<Unit> units = unitRepository.findAll();
        List<UnitDto> unitDtos = new ArrayList<>();
        for (Unit unit : units) {
            UnitDto unitDto = new UnitDto();
            unitDto.setId(unit.getId());
            unitDto.setUnitName(unit.getUnitName());
            unitDtos.add(unitDto);
        }
        return unitDtos;
    }

    @Override
    public List<ProductDTOResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTOResponse> productDTOResponses = new ArrayList<>();
        for (Product product : products) {
            productDTOResponses.add(new ProductDTOResponse(product));
        }
        return productDTOResponses;
    }

    @Override
    public ResponseEntity<byte[]> exportProductsToExcel() throws IOException {
        List<ProductDTOResponse> products = getAllProducts();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Product List");

        // Define columns and create header row
        String[] columns = {"STT", "Mã", "Tên sản phẩm", "Nhóm sản phẩm", "Đơn vị sản phẩm", "Giá nhập", "Giá bán", "Số đăng kí", "Tồn"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = createHeaderCellStyle(workbook);
        CellStyle borderedCellStyle = createBorderedAndCenteredCellStyle(workbook);

        // Set headers with borders
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        for (int i = 0; i < products.size(); i++) {
            ProductDTOResponse product = products.get(i);
            List<ProductUnitDTOResponse> units = product.getProductUnitList();
            int startRow = rowNum; // Track the starting row for merging

            // Create rows for each unit
            for (ProductUnitDTOResponse unit : units) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20); // Set row height for better readability

                // Fill unit-specific data with borders
                createCellWithStyle(row, 4, unit.getUnitName(), borderedCellStyle); // Đơn vị sản phẩm
                createCellWithStyle(row, 5, unit.getImportPrice(), borderedCellStyle); // Giá nhập
                createCellWithStyle(row, 6, unit.getRetailPrice(), borderedCellStyle); // Giá bán
            }

            // Fill product-specific data in the first row only
            Row productRow = sheet.getRow(startRow);
            createCellWithStyle(productRow, 0, i + 1, borderedCellStyle); // STT
            createCellWithStyle(productRow, 1, product.getProductCode(), borderedCellStyle); // Mã
            createCellWithStyle(productRow, 2, product.getProductName(), borderedCellStyle); // Tên sản phẩm
            createCellWithStyle(productRow, 3, product.getCategoryName(), borderedCellStyle); // Nhóm sản phẩm
            createCellWithStyle(productRow, 7, product.getRegistrationNumber(), borderedCellStyle); // Số đăng kí
            createCellWithStyle(productRow, 8, product.getTotalQuantity(), borderedCellStyle); // Tồn

            // Merge cells for product-specific columns and ensure bottom border for merged cells
            if (units.size() > 1) { // Only merge if there are multiple units
                mergeAndApplyBottomBorder(sheet, startRow, rowNum - 1, 0, borderedCellStyle); // STT
                mergeAndApplyBottomBorder(sheet, startRow, rowNum - 1, 1, borderedCellStyle); // Mã
                mergeAndApplyBottomBorder(sheet, startRow, rowNum - 1, 2, borderedCellStyle); // Tên sản phẩm
                mergeAndApplyBottomBorder(sheet, startRow, rowNum - 1, 3, borderedCellStyle); // Nhóm sản phẩm
                mergeAndApplyBottomBorder(sheet, startRow, rowNum - 1, 7, borderedCellStyle); // Số đăng kí
                mergeAndApplyBottomBorder(sheet, startRow, rowNum - 1, 8, borderedCellStyle); // Tồn
            }
        }

        // Auto-size columns to fit content
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Set a minimum width for the "Nhóm sản phẩm" column to ensure visibility
        sheet.setColumnWidth(3, 6000); // Adjust as needed, 5000 units roughly equals 20 characters in width

        // Write data to byte array output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_list.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    // Helper method to create cells with border style and alignment
    private void createCellWithStyle(Row row, int colIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }
        cell.setCellStyle(style);
    }

    // Helper method to create a bordered and centered cell style with thick bottom border
    private CellStyle createBorderedAndCenteredCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN); // Thin top border
        style.setBorderBottom(BorderStyle.THICK); // Thick bottom border
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER); // Center align text
        style.setVerticalAlignment(VerticalAlignment.CENTER); // Middle align text
        return style;
    }

    // Helper method to merge cells and ensure bottom border
    private void mergeAndApplyBottomBorder(Sheet sheet, int startRow, int endRow, int column, CellStyle style) {
        sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, column, column));

        // Apply the style with thick bottom border to each cell in the merged region
        for (int row = startRow; row <= endRow; row++) {
            Row currentRow = sheet.getRow(row);
            Cell cell = currentRow.getCell(column);
            if (cell == null) cell = currentRow.createCell(column);
            cell.setCellStyle(style);
        }
    }

    // Helper method for header cell style with center alignment and white font on blue background
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setBorderTop(BorderStyle.THIN); // Thin top border
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER); // Center align text
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Middle align text
        return headerCellStyle;
    }

    @Override
    public void importProductsFromExcel(MultipartFile file) throws IOException {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            throw new BadRequestException("Invalid file. Please upload an Excel file.");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        List<Product> products = new ArrayList<>();

        int rowNum = 1; // Skip header row
        while (rowNum <= sheet.getLastRowNum()) {
            Row row = sheet.getRow(rowNum);

            // Read product-level data
            String productCode = row.getCell(1).getStringCellValue();
            Product product = new Product();
            product.setProductCode(productCode);
            product.setProductName(row.getCell(2).getStringCellValue());
          //  product.setCategoryName(row.getCell(3).getStringCellValue());
            product.setRegistrationNumber(row.getCell(7).getStringCellValue());
            product.setTotalQuantity((int) row.getCell(8).getNumericCellValue());

            List<ProductUnit> units = new ArrayList<>();

            // Loop to read all units for this product
            do {
                ProductUnit unit = new ProductUnit();
            //    unit.setUnitName(row.getCell(4).getStringCellValue()); // Đơn vị sản phẩm
                unit.setImportPrice(row.getCell(5).getNumericCellValue()); // Giá nhập
                unit.setRetailPrice(row.getCell(6).getNumericCellValue()); // Giá bán
                units.add(unit);

                rowNum++; // Move to the next row

                // Stop if it's the last row or we encounter a new product
                row = (rowNum <= sheet.getLastRowNum()) ? sheet.getRow(rowNum) : null;
            } while (row != null && row.getCell(1) == null); // Continue if productCode cell is empty

            // Save product and units
       //     product.setProductUnits(units);
            products.add(product);
        }

        workbook.close();

        // Save all products in the database
        for (Product product : products) {
            productRepository.save(product);
        //    for (ProductUnit unit : product.getProductUnits()) {
           //     unit.setProduct(product); // Set the relationship
             //   productUnitRepository.save(unit);
            }
        }
    }


