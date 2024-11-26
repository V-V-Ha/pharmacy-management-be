package com.fu.pha.service.impl;

import com.fu.pha.convert.GenerateCode;
import com.fu.pha.dto.request.ProductDTORequest;
import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.dto.response.CloudinaryResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.dto.response.ProductUnitDTOResponse;
import com.fu.pha.entity.*;
import com.fu.pha.enums.Status;
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
import org.apache.poi.ss.util.CellRangeAddressList;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        // Check if category is active
        if (category.getStatus() != Status.ACTIVE) {
            throw new BadRequestException(Message.CATEGORY_INACTIVE);
        }

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
        product.setNumberWarning(productDTORequest.getNumberWarning());
        product.setStatus(Status.ACTIVE);
        // Upload the image product if there is a file
        if (file != null && !file.isEmpty()) {
            String imageProduct = uploadImage(file);
            product.setImageProduct(imageProduct);
        }

        productRepository.save(product);
        List<ProductUnit> productUnitList = new ArrayList<>();
        for (ProductUnitDTORequest productUnitDTORequest : productDTORequest.getProductUnitListDTO()) {
            Unit unit = unitRepository.findById(productUnitDTORequest.getUnitId())
                    .orElseThrow(() -> new ResourceNotFoundException(Message.UNIT_NOT_FOUND));

            // Check if unit is active
            if (unit.getStatus() != Status.ACTIVE) {
                throw new BadRequestException(Message.UNIT_INACTIVE);
            }
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
        // Check if category is active
        if (category.getStatus() != Status.ACTIVE) {
            throw new BadRequestException(Message.CATEGORY_INACTIVE);
        }

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
        product.setNumberWarning(productDTORequest.getNumberWarning());

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
                    Unit unit = unitRepository.findById(productUnitDTORequest.getUnitId())
                            .orElseThrow(() -> new ResourceNotFoundException(Message.UNIT_NOT_FOUND));

                    if (unit.getStatus() != Status.ACTIVE) {
                        throw new BadRequestException(Message.UNIT_INACTIVE);
                    }
                    productUnit.setUnit(unit);
                    productUnit.setConversionFactor(productUnitDTORequest.getConversionFactor());
                    productUnit.setImportPrice(productUnitDTORequest.getImportPrice());
                    productUnit.setRetailPrice(productUnitDTORequest.getRetailPrice());
                    productUnitRepository.save(productUnit);
                } else {
                    // Create a new product unit
                    productUnit = new ProductUnit();
                    Unit unit = unitRepository.findById(productUnitDTORequest.getUnitId())
                            .orElseThrow(() -> new ResourceNotFoundException(Message.UNIT_NOT_FOUND));

                    // Check if unit is active
                    if (unit.getStatus() != Status.ACTIVE) {
                        throw new BadRequestException(Message.UNIT_INACTIVE);
                    }
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
                productDTORequest.getDosageForms().isEmpty() || productDTORequest.getNumberWarning() == null){
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
    public Page<ProductDTOResponse> getAllProductPaging(int page, int size,  String productName, String category, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Status productStatus = null;
        if (status != null) {
            try {
                productStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }
        Page<ProductDTOResponse> products = productRepository.getListProductPaging(productName, category, productStatus, pageable);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND);
        }
        return products;
    }

    @Override
    public Page<ProductDTOResponse> getListProductForSaleOrderPaging(int page, int size,  String productName) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTOResponse> products = productRepository.getListProductForSaleOrderPaging(productName, pageable);
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
    public void updateProductStatus(Long id) {
        Product product = productRepository.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Message.PRODUCT_NOT_FOUND));

        if (product.getStatus() == Status.ACTIVE) {
            product.setStatus(Status.INACTIVE);
        } else {
            product.setStatus(Status.ACTIVE);
        }
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
        Sheet sheet = workbook.createSheet("Danh sách sản phẩm");

        // Define columns and create header row
        String[] columns = {"STT", "Mã", "Tên sản phẩm", "Nhóm sản phẩm", "Đơn vị sản phẩm", "Giá nhập", "Giá bán", "Số đăng kí", "Tồn"};
        Row headerRow = sheet.createRow(0);
        CellStyle headerCellStyle = createHeaderCellStyle(workbook);
        CellStyle borderedCellStyle = createBorderedAndCenteredCellStyle(workbook);
        CellStyle currencyCellStyle = createCurrencyCellStyle(workbook); // Currency style

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
                createCellWithStyle(row, 5, unit.getImportPrice(), currencyCellStyle); // Giá nhập
                createCellWithStyle(row, 6, unit.getRetailPrice(), currencyCellStyle); // Giá bán
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_sach_san_pham.xlsx")
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
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setBorderTop(BorderStyle.THIN); // Thin top border
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER); // Center align text
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Middle align text
        return headerCellStyle;
    }

    // Helper method for currency cell style (formatting as Vietnamese currency)
    private CellStyle createCurrencyCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Vietnamese currency format (₫, no decimals)
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("₫ #,##0"));

        return style;
    }

    @Override
    public void exportExcelTemplate() throws IOException {
        // Lấy danh sách categories từ bảng Category
        List<String> categories = categoryRepository.findAll()
                .stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());

        // Lấy danh sách units từ bảng Unit
        List<String> units = unitRepository.findAll()
                .stream()
                .map(Unit::getUnitName)
                .collect(Collectors.toList());

        // Tạo file Excel template
        createExcelTemplateWithValidation(categories, units);
    }

    private void createExcelTemplateWithValidation(List<String> categories, List<String> units) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mẫu thêm sản phẩm");

        // Tạo dòng tiêu đề
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Tên sản phẩm *", "Nhóm sản phẩm *", "Số đăng ký *", "Thành phần hoạt tính *",
                "Liều lượng", "Phương pháp đóng gói", "Nhà sản xuất *", "Xuất xứ *", "Dạng bào chế *",
                "Hạn mức thông báo *", "Đơn vị sản phẩm *", "Giá nhập", "Giá bán lẻ",
                "Hệ số chuyển đổi", "Thuốc kê theo đơn", "Chỉ định", "Chống chỉ định",
                "Tác dụng phụ", "Mô tả"
        };

        // Gán tiêu đề và định dạng
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            cell.setCellStyle(headerStyle);
        }

        // Thêm danh sách thả xuống cho "Nhóm sản phẩm" và "Đơn vị sản phẩm"
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // Tạo danh sách từ categories (Category table)
        String[] categoryArray = categories.toArray(new String[0]);
        String categoryRange = createExcelDropdownList(workbook, categoryArray, "Categories");

        CellRangeAddressList categoryAddressList = new CellRangeAddressList(1, 100, 1, 1); // Áp dụng cho cột "Nhóm sản phẩm"
        DataValidationConstraint categoryConstraint = validationHelper.createFormulaListConstraint(categoryRange);
        DataValidation categoryValidation = validationHelper.createValidation(categoryConstraint, categoryAddressList);
        sheet.addValidationData(categoryValidation);

        // Tạo danh sách từ units (Unit table)
        String[] unitArray = units.toArray(new String[0]);
        String unitRange = createExcelDropdownList(workbook, unitArray, "Units");

        CellRangeAddressList unitAddressList = new CellRangeAddressList(1, 100, 10, 10); // Áp dụng cho cột "Đơn vị sản phẩm"
        DataValidationConstraint unitConstraint = validationHelper.createFormulaListConstraint(unitRange);
        DataValidation unitValidation = validationHelper.createValidation(unitConstraint, unitAddressList);
        sheet.addValidationData(unitValidation);

        // Thêm danh sách thả xuống cho "Thuốc kê theo đơn"
        String[] prescriptionOptions = {"Có", "Không"};
        String prescriptionRange = createExcelDropdownList(workbook, prescriptionOptions, "Prescription");

        CellRangeAddressList prescriptionAddressList = new CellRangeAddressList(1, 100, 14, 14); // Áp dụng cho cột "Thuốc kê theo đơn"
        DataValidationConstraint prescriptionConstraint = validationHelper.createFormulaListConstraint(prescriptionRange);
        DataValidation prescriptionValidation = validationHelper.createValidation(prescriptionConstraint, prescriptionAddressList);
        sheet.addValidationData(prescriptionValidation);

        // Gộp các ô cho những cột cần gộp 3 hàng
        for (int rowIndex = 1; rowIndex <= 100; rowIndex += 3) {
            for (int colIndex : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 14, 15, 16, 17, 18}) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex + 2, colIndex, colIndex));
            }
        }

        // Định dạng các cột "Đơn vị sản phẩm", "Giá nhập", "Giá bán lẻ", "Hệ số chuyển đổi" (mỗi ô tương ứng một hàng)
        for (int colIndex : new int[]{10, 11, 12, 13}) {
            for (int rowIndex = 1; rowIndex <= 100; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                row.createCell(colIndex);
            }
        }

        // Định dạng tự động kích thước cột
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi Workbook ra file
        try (FileOutputStream fileOut = new FileOutputStream("product_template.xlsx")) {
            workbook.write(fileOut);
        }
        workbook.close();
    }


    // Tạo danh sách dropdown trong Excel
    private String createExcelDropdownList(Workbook workbook, String[] values, String listName) {
        Name namedRange = workbook.createName();
        namedRange.setNameName(listName);
        Sheet hiddenSheet = workbook.createSheet(listName);
        for (int i = 0; i < values.length; i++) {
            Row row = hiddenSheet.createRow(i);
            Cell cell = row.createCell(0);
            cell.setCellValue(values[i]);
        }
        String reference = listName + "!$A$1:$A$" + values.length;
        namedRange.setRefersToFormula(reference);
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true); // Ẩn sheet
        return listName;
    }

    }


