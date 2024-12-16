package com.fu.pha.service.impl;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.customer.CustomerInvoiceDto;
import com.fu.pha.dto.response.report.customer.CustomerInvoiceProjection;
import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.InventoryProductReportDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.dto.response.report.product.ProductSalesDto;
import com.fu.pha.dto.response.report.reportEntity.ImportItemReportDto;
import com.fu.pha.dto.response.report.reportEntity.ProductReportDto;
import com.fu.pha.dto.response.report.sale.SalesTransactionDto;
import com.fu.pha.dto.response.report.supplier.SupplierInvoiceDto;
import com.fu.pha.dto.response.report.supplier.SupplierInvoiceProjection;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.repository.*;
import com.fu.pha.service.ReportService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImportItemRepository importItemRepository;

    @Autowired
    private ExportSlipItemRepository exportSlipItemRepository;

    @Autowired
    private SaleOrderItemRepository saleOrderItemRepository;

    @Autowired
    private ReturnOrderItemRepository returnOrderItemRepository;

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ExportSlipRepository exportSlipRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;



    // -------------------- Báo cáo kho --------------------

    @Override
    public InventoryReportDto getInventoryReport(LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        InventoryReportDto report = new InventoryReportDto();
        Instant startInstant;
        Instant endInstant;


        if (month != null && year != null) {
            // Nếu có tháng và năm, tính startDate và endDate cho tháng đó
            startInstant = LocalDate.of(year, month, 1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
            endInstant = LocalDate.of(year, month, startDate.lengthOfMonth())
                    .atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        } else if (year != null) {
            // Nếu chỉ có năm, tính startDate và endDate cho cả năm
            startInstant = LocalDate.of(year, 1, 1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
            endInstant = LocalDate.of(year, 12, 31)
                    .atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        } else if (startDate != null && endDate != null) {
            // Nếu có startDate và endDate, sử dụng chúng làm khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        }else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        // Tính tồn kho đầu kỳ
        int beginningInventoryQuantity = calculateBeginningInventoryQuantity(startInstant);
        double beginningInventoryAmount = calculateBeginningInventoryAmount(startInstant);
        report.setBeginningInventoryQuantity(!(beginningInventoryQuantity == 0) ? beginningInventoryQuantity : 0);
        report.setBeginningInventoryAmount(beginningInventoryAmount != 0 ? beginningInventoryAmount : 0.0);

        // Tính nhập kho trong kỳ
        int goodsReceivedQuantity = calculateGoodsReceivedQuantity(startInstant, endInstant);
        double goodsReceivedAmount = calculateGoodsReceivedAmount(startInstant, endInstant);
        report.setGoodsReceivedQuantity(!(goodsReceivedQuantity == 0) ? goodsReceivedQuantity : 0);
        report.setGoodsReceivedAmount(goodsReceivedAmount != 0 ? goodsReceivedAmount : 0.0);

        // Tính xuất kho trong kỳ
        int goodsIssuedQuantity = calculateGoodsIssuedQuantity(startInstant, endInstant);
        double goodsIssuedAmount = calculateGoodsIssuedAmount(startInstant, endInstant);
        report.setGoodsIssuedQuantity(!(goodsIssuedQuantity == 0) ? goodsIssuedQuantity : 0);
        report.setGoodsIssuedAmount(goodsIssuedAmount != 0 ? goodsIssuedAmount : 0.0);

        // Tính xuất hủy
        int goodsDestroyedQuantity = calculateGoodsDestroyedQuantity(startInstant, endInstant);
        double goodsDestroyedAmount = calculateGoodsDestroyedAmount(startInstant, endInstant);
        report.setGoodsDestroyedQuantity(!(goodsDestroyedQuantity == 0) ? goodsDestroyedQuantity : 0);
        report.setGoodsDestroyedAmount(goodsDestroyedAmount != 0 ? goodsDestroyedAmount : 0.0);

        // Tính xuất trả
        int goodsReturnedQuantity = calculateGoodsReturnedQuantity(startInstant, endInstant);
        double goodsReturnedAmount = calculateGoodsReturnedAmount(startInstant, endInstant);
        report.setGoodsReturnedQuantity(!(goodsReturnedQuantity == 0) ? goodsReturnedQuantity : 0);
        report.setGoodsReturnedAmount(goodsReturnedAmount != 0 ? goodsReturnedAmount : 0.0);

        // Tính tồn kho hiện tại
        Integer currentInventoryQuantity = beginningInventoryQuantity + goodsReceivedQuantity - goodsIssuedQuantity;
        double currentInventoryAmount = beginningInventoryAmount + goodsReceivedAmount - goodsIssuedAmount;
        report.setCurrentInventoryQuantity(currentInventoryQuantity != null ? currentInventoryQuantity : 0);
        report.setCurrentInventoryAmount(currentInventoryAmount >= 0 ? currentInventoryAmount : 0.0);

        // Tính số lượng sản phẩm sắp hết hàng
        int nearlyOutOfStockProducts = productRepository.findNearlyOutOfStockProducts(10).size();
        report.setNearlyOutOfStockProducts(!(nearlyOutOfStockProducts == 0) ? nearlyOutOfStockProducts : 0);

        // Tính số lượng sản phẩm hết hàng
        int outOfStockProducts = productRepository.countOutOfStock();
        report.setOutOfStockProducts(!(outOfStockProducts == 0) ? outOfStockProducts : 0);

        Instant thresholdDate = Instant.now().plus(60, ChronoUnit.DAYS);

        // Tính sản phẩm sắp hết hạn
        int nearlyExpiredItems = importItemRepository.findNearlyExpiredItems(Instant.now(),thresholdDate).size();
        report.setNearlyExpiredItems(!(nearlyExpiredItems == 0) ? nearlyExpiredItems : 0);

        // Tính sản phẩm hết hạn
        int expiredItems = importItemRepository.findExpiredItems(Instant.now().plus(1, ChronoUnit.DAYS)).size();
        report.setExpiredItems(!(expiredItems == 0) ? expiredItems : 0);


        return report;
    }


    // Các phương thức hỗ trợ cho báo cáo kho
    private int calculateBeginningInventoryQuantity(Instant startDate) {
        Integer totalReceivedBeforeStart = Optional.ofNullable(importItemRepository.sumQuantityBeforeDate(startDate)).orElse(0);
        Integer totalExportedBeforeStart = Optional.ofNullable(exportSlipItemRepository.sumQuantityBeforeDate(startDate)).orElse(0);
        Integer totalSoldBeforeStart = Optional.ofNullable(saleOrderItemRepository.sumQuantityBeforeDate(startDate)).orElse(0);
        Integer totalReturnedBeforeStart = Optional.ofNullable(returnOrderItemRepository.sumQuantityBeforeDate(startDate)).orElse(0);

        // Tồn kho đầu kỳ (số lượng) = Tổng nhập trước kỳ - (Tổng xuất trước kỳ + Tổng bán trước kỳ) + Tổng hàng trả lại trước kỳ
        return totalReceivedBeforeStart - (totalExportedBeforeStart + totalSoldBeforeStart) + totalReturnedBeforeStart;
    }

    private double calculateBeginningInventoryAmount(Instant startDate) {
        Double totalReceivedAmountBeforeStart = Optional.ofNullable(importItemRepository.sumAmountBeforeDate(startDate)).orElse(0.0);
        Double totalExportedAmountBeforeStart = Optional.ofNullable(exportSlipItemRepository.sumAmountBeforeDate(startDate)).orElse(0.0);
        Double totalSoldAmountBeforeStart = Optional.ofNullable(saleOrderItemRepository.sumAmountBeforeDate(startDate)).orElse(0.0);
        Double totalReturnedAmountBeforeStart = Optional.ofNullable(returnOrderItemRepository.sumAmountBeforeDate(startDate)).orElse(0.0);

        // Tồn kho đầu kỳ (tổng tiền) = Tổng tiền nhập trước kỳ - (Tổng tiền xuất trước kỳ + Tổng tiền bán trước kỳ) + Tổng tiền hàng trả lại trước kỳ
        return totalReceivedAmountBeforeStart - (totalExportedAmountBeforeStart + totalSoldAmountBeforeStart) + totalReturnedAmountBeforeStart;
    }

    private int calculateGoodsReceivedQuantity(Instant startDate, Instant endDate) {
        return Optional.ofNullable(importItemRepository.sumQuantityBetweenDates(startDate, endDate)).orElse(0);
    }

    private double calculateGoodsReceivedAmount(Instant startDate, Instant endDate) {
        return Optional.ofNullable(importItemRepository.sumAmountBetweenDates(startDate, endDate)).orElse(0.0);
    }

    private int calculateGoodsIssuedQuantity(Instant startDate, Instant endDate) {
        Integer totalExported = Optional.ofNullable(exportSlipItemRepository.sumQuantityBetweenDates(startDate, endDate)).orElse(0);
        Integer totalSold = Optional.ofNullable(saleOrderItemRepository.sumQuantityBetweenDates(startDate, endDate)).orElse(0);
        Integer totalReturned = Optional.ofNullable(returnOrderItemRepository.sumQuantityBetweenDates(startDate, endDate)).orElse(0);

        // Tổng xuất kho (số lượng) = Tổng xuất trong kỳ + Tổng bán trong kỳ - Tổng hàng trả lại trong kỳ
        return totalExported + totalSold - totalReturned;
    }

    private double calculateGoodsIssuedAmount(Instant startDate, Instant endDate) {
        Double totalExportedAmount = Optional.ofNullable(exportSlipItemRepository.sumAmountBetweenDates(startDate, endDate)).orElse(0.0);
        Double totalSoldAmount = Optional.ofNullable(saleOrderItemRepository.sumAmountBetweenDates(startDate, endDate)).orElse(0.0);
        Double totalReturnedAmount = Optional.ofNullable(returnOrderItemRepository.sumAmountBetweenDates(startDate, endDate)).orElse(0.0);

        // Tổng xuất kho (tổng tiền) = Tổng tiền xuất trong kỳ + Tổng tiền bán trong kỳ - Tổng tiền hàng trả lại trong kỳ
        return totalExportedAmount + totalSoldAmount - totalReturnedAmount;
    }

    private int calculateGoodsDestroyedQuantity(Instant startDate, Instant endDate) {
        return Optional.ofNullable(exportSlipItemRepository.sumQuantityByTypeBetweenDates(ExportType.DESTROY, startDate, endDate)).orElse(0);
    }

    private double calculateGoodsDestroyedAmount(Instant startDate, Instant endDate) {
        return Optional.ofNullable(exportSlipItemRepository.sumAmountByTypeBetweenDates(ExportType.DESTROY, startDate, endDate)).orElse(0.0);
    }

    private int calculateGoodsReturnedQuantity(Instant startDate, Instant endDate) {
        return Optional.ofNullable(exportSlipItemRepository.sumQuantityByTypeBetweenDates(ExportType.RETURN_TO_SUPPLIER, startDate, endDate)).orElse(0);
    }

    private double calculateGoodsReturnedAmount(Instant startDate, Instant endDate) {
        return Optional.ofNullable(exportSlipItemRepository.sumAmountByTypeBetweenDates(ExportType.RETURN_TO_SUPPLIER, startDate, endDate)).orElse(0.0);
    }



    @Override
    public Page<InventoryProductReportDto> getInventoryReportByProduct(
            LocalDate startDate, LocalDate endDate, Integer month, Integer year,
            String productCode, String productName, Long categoryId,
            int pageNumber, int pageSize
    ) {
        // Thêm điều kiện sắp xếp theo id tăng dần
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());

        Instant startInstant;
        Instant endInstant;

        // Xác định khoảng thời gian
        if (month != null && year != null) {
            startInstant = LocalDate.of(year, month, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth()).atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault()).toInstant();
        } else if (year != null) {
            startInstant = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = LocalDate.of(year, 12, 31).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null && endDate != null) {
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        // Xây dựng Specification để tìm kiếm và lọc
        Specification<Product> spec = Specification.where(null);

        if (productCode != null && !productCode.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.upper(root.get("productCode").as(String.class)),
                            "%" + productCode.toUpperCase() + "%"
                    )
            );
        }

        if (productName != null && !productName.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.upper(root.get("productName").as(String.class)),
                            "%" + productName.toUpperCase() + "%"
                    )
            );
        }

        if (categoryId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("categoryId").get("id"), categoryId));
        }

        // Lấy danh sách sản phẩm theo Specification và phân trang
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<InventoryProductReportDto> reportList = new ArrayList<>();

        // Lặp qua các sản phẩm trong trang hiện tại
        for (Product product : productPage.getContent()) {
            Long productId = product.getId();
            InventoryProductReportDto report = new InventoryProductReportDto();
            report.setProductName(product.getProductName());
            report.setProductCode(product.getProductCode());

            // Tồn đầu kỳ
            int beginningQuantity = calculateBeginningInventoryQuantityByProduct(productId, startInstant);
            double beginningAmount = calculateBeginningInventoryAmountByProduct(productId, startInstant);
            report.setBeginningInventoryQuantity(Math.max(beginningQuantity, 0));
            report.setBeginningInventoryAmount(Math.max(beginningAmount, 0.0));

            // Nhập kho
            int receivedQuantity = calculateGoodsReceivedQuantityByProduct(productId, startInstant, endInstant);
            double receivedAmount = calculateGoodsReceivedAmountByProduct(productId, startInstant, endInstant);
            report.setGoodsReceivedQuantity(Math.max(receivedQuantity, 0));
            report.setGoodsReceivedAmount(Math.max(receivedAmount, 0.0));

            // Xuất kho
            int issuedQuantity = calculateGoodsIssuedQuantityByProduct(productId, startInstant, endInstant);
            double issuedAmount = calculateGoodsIssuedAmountByProduct(productId, startInstant, endInstant);
            report.setGoodsIssuedQuantity(Math.max(issuedQuantity, 0));
            report.setGoodsIssuedAmount(Math.max(issuedAmount, 0.0));

            // Tồn cuối kỳ
            int endingQuantity = beginningQuantity + receivedQuantity - issuedQuantity;
            double endingAmount = beginningAmount + receivedAmount - issuedAmount;
            report.setEndingInventoryQuantity(Math.max(endingQuantity, 0));
            report.setEndingInventoryAmount(Math.max(endingAmount, 0.0));

            reportList.add(report);
        }

        // Trả về kết quả phân trang
        return new PageImpl<>(reportList, pageable, productPage.getTotalElements());
    }




    // Các phương thức hỗ trợ cho báo cáo kho theo sản phẩm

    private int calculateBeginningInventoryQuantityByProduct(Long productId, Instant startDate) {
        Integer totalReceivedBeforeStart = Optional.ofNullable(importItemRepository.sumQuantityBeforeDateByProduct(productId, startDate)).orElse(0);
        Integer totalExportedBeforeStart = Optional.ofNullable(exportSlipItemRepository.sumQuantityBeforeDateByProduct(productId, startDate)).orElse(0);
        Integer totalSoldBeforeStart = Optional.ofNullable(saleOrderItemRepository.sumQuantityBeforeDateByProduct(productId, startDate)).orElse(0);
        Integer totalReturnedBeforeStart = Optional.ofNullable(returnOrderItemRepository.sumQuantityBeforeDateByProduct(productId, startDate)).orElse(0);

        // Tồn đầu kỳ (số lượng) = Tổng nhập trước kỳ - (Tổng xuất trước kỳ + Tổng bán trước kỳ) + Tổng trả lại trước kỳ
        return totalReceivedBeforeStart - (totalExportedBeforeStart + totalSoldBeforeStart) + totalReturnedBeforeStart;
    }

    private double calculateBeginningInventoryAmountByProduct(Long productId, Instant startDate) {
        Double totalReceivedAmountBeforeStart = Optional.ofNullable(importItemRepository.sumAmountBeforeDateByProduct(productId, startDate)).orElse(0.0);
        Double totalExportedAmountBeforeStart = Optional.ofNullable(exportSlipItemRepository.sumAmountBeforeDateByProduct(productId, startDate)).orElse(0.0);
        Double totalSoldAmountBeforeStart = Optional.ofNullable(saleOrderItemRepository.sumAmountBeforeDateByProduct(productId, startDate)).orElse(0.0);
        Double totalReturnedAmountBeforeStart = Optional.ofNullable(returnOrderItemRepository.sumAmountBeforeDateByProduct(productId, startDate)).orElse(0.0);

        // Tồn đầu kỳ (tổng tiền) = Tổng nhập trước kỳ - (Tổng xuất trước kỳ + Tổng bán trước kỳ) + Tổng trả lại trước kỳ
        return totalReceivedAmountBeforeStart - (totalExportedAmountBeforeStart + totalSoldAmountBeforeStart) + totalReturnedAmountBeforeStart;
    }

    private int calculateGoodsReceivedQuantityByProduct(Long productId, Instant startDate, Instant endDate) {
        return Optional.ofNullable(importItemRepository.sumQuantityByProductBetweenDates(productId, startDate, endDate)).orElse(0);
    }

    private double calculateGoodsReceivedAmountByProduct(Long productId, Instant startDate, Instant endDate) {
        return Optional.ofNullable(importItemRepository.sumAmountByProductBetweenDates(productId, startDate, endDate)).orElse(0.0);
    }


    private int calculateGoodsIssuedQuantityByProduct(Long productId, Instant startDate, Instant endDate) {
        Integer totalExported = Optional.ofNullable(exportSlipItemRepository.sumQuantityByProductBetweenDates(productId, startDate, endDate)).orElse(0);
        Integer totalSold = Optional.ofNullable(saleOrderItemRepository.sumQuantityByProductBetweenDates(productId, startDate, endDate)).orElse(0);
        Integer totalReturned = Optional.ofNullable(returnOrderItemRepository.sumQuantityByProductBetweenDates(productId, startDate, endDate)).orElse(0);

        // Tổng xuất kho (số lượng) = Tổng xuất trong kỳ + Tổng bán trong kỳ - Tổng trả lại trong kỳ
        return totalExported + totalSold - totalReturned;
    }

    private double calculateGoodsIssuedAmountByProduct(Long productId, Instant startDate, Instant endDate) {
        Double totalExportedAmount = Optional.ofNullable(exportSlipItemRepository.sumAmountByProductBetweenDates(productId, startDate, endDate)).orElse(0.0);
        Double totalSoldAmount = Optional.ofNullable(saleOrderItemRepository.sumAmountByProductBetweenDates(productId, startDate, endDate)).orElse(0.0);
        Double totalReturnedAmount = Optional.ofNullable(returnOrderItemRepository.sumAmountByProductBetweenDates(productId, startDate, endDate)).orElse(0.0);

        // Tổng xuất kho (tổng tiền) = Tổng tiền xuất trong kỳ + Tổng tiền bán trong kỳ - Tổng tiền trả lại trong kỳ
        return totalExportedAmount + totalSoldAmount - totalReturnedAmount;
    }


    // --List hàng hết hàng
    @Override
    public Page<OutOfStockProductDto> getOutOfStockProducts(
            Long categoryId,
            String searchText,
            int pageNumber,
            int pageSize
    ) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        return productRepository.findOutOfStockProducts(
                categoryId,
                searchText,
                pageable
        );
    }

    // --List hàng hết hạn

    public Page<ExpiredProductDto> getExpiredProducts(
            Long categoryId,
            String searchText,
            int warningDays,
            int pageNumber,
            int pageSize
    ) {
        PageRequest pageable = PageRequest.of(pageNumber, pageSize);
        Instant currentDate = Instant.now();
        Instant warningDate = currentDate.plus(Duration.ofDays(warningDays));

        return productRepository.findExpiredProducts(
                categoryId,
                searchText,
                warningDate,
                pageable
        );
    }

    @Override
    public void exportInventoryReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // 1. Lấy dữ liệu
        InventoryReportDto inventoryReport = getInventoryReport(fromDate, toDate, null, null);
        Page<InventoryProductReportDto> productReportPage = getInventoryReportByProduct(
                fromDate, toDate, null, null, null, null, null, 0, Integer.MAX_VALUE);
        Page<OutOfStockProductDto> outOfStockPage = getOutOfStockProducts(null, null, 0, Integer.MAX_VALUE);
        Page<ExpiredProductDto> expiredProductsPage = getExpiredProducts(null, null, 30, 0, Integer.MAX_VALUE);

        // 2. Tạo sheet gộp dữ liệu
        createCombinedInventorySheet(workbook, inventoryReport, productReportPage.getContent(),
                outOfStockPage.getContent(), expiredProductsPage.getContent());

        // 3. Ghi dữ liệu vào response
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Bao_cao_ton_kho.xlsx");

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createCombinedInventorySheet(Workbook workbook, InventoryReportDto inventoryReport,
                                              List<InventoryProductReportDto> products,
                                              List<OutOfStockProductDto> outOfStockProducts,
                                              List<ExpiredProductDto> expiredProducts) {
        Sheet sheet = workbook.createSheet("Báo cáo tồn kho");

        // Định dạng tiêu đề
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex()); // Màu chữ #09446d

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); // Màu nền #c6e1f8
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Định dạng dữ liệu
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Định dạng dữ liệu align left
        CellStyle leftAlignCellStyle = workbook.createCellStyle();
        leftAlignCellStyle.cloneStyleFrom(dataCellStyle);
        leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);

        // Định dạng số tiền
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(leftAlignCellStyle); // Hiển thị align left
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Định dạng ngày tháng
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy   HH:mm"));

        int currentRow = 0;

        // Phần 1: Tổng hợp tồn kho
        currentRow = writeInventorySummary(sheet, headerCellStyle, dataCellStyle, currencyStyle, inventoryReport, currentRow);

        // Phần 2: Tồn kho theo sản phẩm
        currentRow = writeInventoryProducts(sheet, headerCellStyle, dataCellStyle, currencyStyle, leftAlignCellStyle, products, currentRow);

        // Phần 3: Hàng hết hàng
        currentRow = writeOutOfStockProducts(sheet, headerCellStyle, dataCellStyle, outOfStockProducts, currentRow);

        // Phần 4: Hàng hết hạn
        currentRow = writeExpiredProducts(sheet, headerCellStyle, dataCellStyle, dateStyle, expiredProducts, currentRow);

        // Tự động chỉnh kích thước các cột
        for (int i = 0; i < 15; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private int writeInventorySummary(Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, CellStyle currencyStyle, InventoryReportDto report, int startRow) {
        String[] headers = {"Tồn đầu kỳ", "Tổng giá trị tồn đầu kỳ", "Tổng số lượng nhập", "Tổng giá trị nhập",
                "Tổng số lượng xuất", "Tổng giá trị xuất", "Tồn cuối kỳ", "Tổng giá trị tồn cuối kỳ",
                "Hết hàng", "Sắp hết hàng", "Hết hạn", "Sắp hết hạn"};
        Row headerRow = sheet.createRow(startRow++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Row dataRow = sheet.createRow(startRow++);
        createCellWithStyle(dataRow, 0, report.getBeginningInventoryQuantity(), dataStyle);
        createCellWithStyle(dataRow, 1, report.getBeginningInventoryAmount(), currencyStyle);
        createCellWithStyle(dataRow, 2, report.getGoodsReceivedQuantity(), dataStyle);
        createCellWithStyle(dataRow, 3, report.getGoodsReceivedAmount(), currencyStyle);
        createCellWithStyle(dataRow, 4, report.getGoodsIssuedQuantity(), dataStyle);
        createCellWithStyle(dataRow, 5, report.getGoodsIssuedAmount(), currencyStyle);
        createCellWithStyle(dataRow, 6, report.getCurrentInventoryQuantity(), dataStyle);
        createCellWithStyle(dataRow, 7, report.getCurrentInventoryAmount(), currencyStyle);
        createCellWithStyle(dataRow, 8, report.getOutOfStockProducts(), dataStyle);
        createCellWithStyle(dataRow, 9, report.getNearlyOutOfStockProducts(), dataStyle);
        createCellWithStyle(dataRow, 10, report.getExpiredItems(), dataStyle);
        createCellWithStyle(dataRow, 11, report.getNearlyExpiredItems(), dataStyle);

        return startRow + 1; // Dòng trống
    }

    private int writeInventoryProducts(Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, CellStyle currencyStyle, CellStyle leftAlignCellStyle, List<InventoryProductReportDto> products, int startRow) {
        String[] headers = {"Mã sản phẩm", "Tên sản phẩm", "Tồn đầu kỳ", "Giá trị tồn đầu kỳ", "Số lượng nhập", "Giá trị nhập",
                "Số lượng xuất", "Giá trị xuất", "Tồn cuối kỳ", "Giá trị tồn cuối kỳ"};
        Row headerRow = sheet.createRow(startRow++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (InventoryProductReportDto product : products) {
            Row row = sheet.createRow(startRow++);
            createCellWithStyle(row, 0, product.getProductCode(), dataStyle);
            createCellWithStyle(row, 1, product.getProductName(), leftAlignCellStyle); // Align left
            createCellWithStyle(row, 2, product.getBeginningInventoryQuantity(), dataStyle);
            createCellWithStyle(row, 3, product.getBeginningInventoryAmount(), currencyStyle); // Align left
            createCellWithStyle(row, 4, product.getGoodsReceivedQuantity(), dataStyle);
            createCellWithStyle(row, 5, product.getGoodsReceivedAmount(), currencyStyle); // Align left
            createCellWithStyle(row, 6, product.getGoodsIssuedQuantity(), dataStyle);
            createCellWithStyle(row, 7, product.getGoodsIssuedAmount(), currencyStyle); // Align left
            createCellWithStyle(row, 8, product.getEndingInventoryQuantity(), dataStyle);
            createCellWithStyle(row, 9, product.getEndingInventoryAmount(), currencyStyle); // Align left
        }

        return startRow + 1; // Dòng trống
    }

    private int writeOutOfStockProducts(Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, List<OutOfStockProductDto> products, int startRow) {
        String[] headers = {"Mã sản phẩm", "Tên sản phẩm", "Danh mục", "Đơn vị", "Số lượng cảnh báo", "Tổng số lượng"};
        Row headerRow = sheet.createRow(startRow++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (OutOfStockProductDto product : products) {
            Row row = sheet.createRow(startRow++);
            createCellWithStyle(row, 0, product.getProductCode(), dataStyle);
            createCellWithStyle(row, 1, product.getProductName(), dataStyle);
            createCellWithStyle(row, 2, product.getCategoryName(), dataStyle);
            createCellWithStyle(row, 3, product.getUnitName(), dataStyle);
            createCellWithStyle(row, 4, product.getNumberWarning(), dataStyle);
            createCellWithStyle(row, 5, product.getTotalQuantity(), dataStyle);
        }

        return startRow + 1; // Dòng trống
    }

    private int writeExpiredProducts(Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, CellStyle dateStyle, List<ExpiredProductDto> products, int startRow) {
        String[] headers = {"Mã sản phẩm", "Tên sản phẩm", "Danh mục", "Đơn vị", "Số lô", "Ngày hết hạn", "Số ngày còn lại"};
        Row headerRow = sheet.createRow(startRow++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (ExpiredProductDto product : products) {
            Row row = sheet.createRow(startRow++);
            createCellWithStyle(row, 0, product.getProductCode(), dataStyle);
            createCellWithStyle(row, 1, product.getProductName(), dataStyle);
            createCellWithStyle(row, 2, product.getCategoryName(), dataStyle);
            createCellWithStyle(row, 3, product.getUnitName(), dataStyle);
            createCellWithStyle(row, 4, product.getBatchNumber(), dataStyle);
            createCellWithStyle(row, 5, product.getExpiryDate().toString(), dateStyle);
            createCellWithStyle(row, 6, product.getDaysRemaining(), dataStyle);
        }

        return startRow + 1; // Dòng trống
    }


    // -------------------- Báo cáo bán hàng --------------------

    @Override
    public SalesReportDto getSalesReport(LocalDate startDate, LocalDate endDate) {
        SalesReportDto report = new SalesReportDto();

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        // Tổng số hóa đơn
        long totalInvoices = saleOrderRepository.countSaleOrdersBetweenDates(startInstant, endInstant);
        report.setTotalInvoices(totalInvoices);

        // Doanh thu tổng
        Double totalRevenue = saleOrderRepository.sumTotalAmountBetweenDates(startInstant, endInstant);
        report.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // Tổng số lượng bán hàng
        Integer totalQuantitySold = saleOrderItemRepository.sumTotalQuantityBetweenDates(startInstant, endInstant);
        report.setTotalQuantitySold(totalQuantitySold != null ? totalQuantitySold : 0);

        // Doanh thu theo tền mặt
        Double cashRevenue = saleOrderRepository.sumTotalAmountByPaymentMethodBetweenDates(startInstant, endInstant ,PaymentMethod.CASH);
        report.setCashRevenue(cashRevenue != null ? cashRevenue : 0.0);
        // Doang thu theo chuyển khoản
        Double transferRevenue = saleOrderRepository.sumTotalAmountByPaymentMethodBetweenDates(startInstant, endInstant ,PaymentMethod.TRANSFER);
        report.setTransferRevenue(transferRevenue != null ? transferRevenue : 0.0);

        return report;
    }

    @Override
    public Page<SalesTransactionDto> getSalesTransactions(
            String paymentMethod,
            String voucherType,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        return saleOrderRepository.findSalesTransactions(
                paymentMethod, voucherType, startInstant, endInstant, pageable);
    }


    @Override
    public Page<ProductSalesDto> getProductSales(
            String productName,
            String productCode,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        return saleOrderRepository.findProductSales(
                productName, productCode, startInstant, endInstant, pageable);
    }

    @Override
    public void exportSalesReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException {
        // Step 1: Get sales report data
        SalesReportDto salesReport = getSalesReport(fromDate, toDate);

        // Step 2: Get sales transactions
        Instant startInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<SalesTransactionDto> salesTransactions = saleOrderRepository.findSalesTransactions(
                null, null, startInstant, endInstant, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        // Step 3: Get product sales data
        List<ProductSalesDto> productSales = saleOrderRepository.findProductSales(
                null, null, startInstant, endInstant, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        // Step 4: Create Excel workbook
        Workbook workbook = new XSSFWorkbook();

        // Add the combined sales sheet
        createCombinedSalesSheet(workbook, salesReport, salesTransactions, productSales);

        // Write the workbook to the response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createCombinedSalesSheet(Workbook workbook, SalesReportDto report, List<SalesTransactionDto> transactions, List<ProductSalesDto> products) {
        Sheet sheet = workbook.createSheet("Báo cáo bán hàng");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Left-aligned styling for specific columns
        CellStyle leftAlignCellStyle = workbook.createCellStyle();
        leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);
        leftAlignCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        leftAlignCellStyle.setBorderTop(BorderStyle.THIN);
        leftAlignCellStyle.setBorderBottom(BorderStyle.THIN);
        leftAlignCellStyle.setBorderLeft(BorderStyle.THIN);
        leftAlignCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(leftAlignCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);

        // Date styling
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy   HH:mm"));
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);

        int currentRow = 0;

        // Part 1: Sales Summary
        Row summaryHeaderRow = sheet.createRow(currentRow++);
        String[] summaryHeaders = {"Tổng hóa đơn", "Doanh thu hóa đơn", "Số lượng đã bán", "Doanh thu tiền mặt", "Doanh thu chuyển khoản"};
        for (int i = 0; i < summaryHeaders.length; i++) {
            Cell cell = summaryHeaderRow.createCell(i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        Row summaryDataRow = sheet.createRow(currentRow++);
        createCellWithStyle(summaryDataRow, 0, report.getTotalInvoices(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 1, report.getTotalRevenue(), currencyStyle);
        createCellWithStyle(summaryDataRow, 2, report.getTotalQuantitySold(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 3, report.getCashRevenue(), currencyStyle);
        createCellWithStyle(summaryDataRow, 4, report.getTransferRevenue(), currencyStyle);

        currentRow++;

        // Part 2: Sales Transactions
        Row transactionHeaderRow = sheet.createRow(currentRow++);
        String[] transactionHeaders = {"STT", "Mã hóa đơn", "Ngày tạo", "Khách hàng", "Loại hóa đơn", "Phương thức thanh toán", "Tổng tiền"};
        for (int i = 0; i < transactionHeaders.length; i++) {
            Cell cell = transactionHeaderRow.createCell(i);
            cell.setCellValue(transactionHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        for (int i = 0; i < transactions.size(); i++) {
            SalesTransactionDto transaction = transactions.get(i);
            Row row = sheet.createRow(currentRow++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle);
            createCellWithStyle(row, 1, transaction.getInvoiceNumber(), dataCellStyle);

            // Convert Instant to formatted LocalDateTime
            createCellWithStyle(row, 2, DateTimeFormatter.ofPattern("dd-MM-yyyy   HH:mm")
                    .format(transaction.getCreationDate().atZone(ZoneId.systemDefault())), dateStyle);

            createCellWithStyle(row, 3, transaction.getCustomerName(), leftAlignCellStyle);
            createCellWithStyle(row, 4, transaction.getVoucherType(), leftAlignCellStyle);

            // Map payment method
            String paymentMethod = transaction.getPaymentMethod();
            if ("TRANSFER".equals(paymentMethod)) {
                paymentMethod = "Chuyển khoản";
            } else if ("CASH".equals(paymentMethod)) {
                paymentMethod = "Tiền mặt";
            } else {
                paymentMethod = "Tiền mặt";
            }

            createCellWithStyle(row, 5, paymentMethod, leftAlignCellStyle);
            createCellWithStyle(row, 6, transaction.getTotalAmount(), currencyStyle);
        }

        currentRow++;

        // Part 3: Product Sales
        Row productHeaderRow = sheet.createRow(currentRow++);
        String[] productHeaders = {"STT", "Mã sản phẩm", "Tên sản phẩm", "Đơn vị", "Số lượng bán", "Số giao dịch", "Tổng tiền"};
        for (int i = 0; i < productHeaders.length; i++) {
            Cell cell = productHeaderRow.createCell(i);
            cell.setCellValue(productHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        for (int i = 0; i < products.size(); i++) {
            ProductSalesDto product = products.get(i);
            Row row = sheet.createRow(currentRow++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle);
            createCellWithStyle(row, 1, product.getProductCode(), dataCellStyle);
            createCellWithStyle(row, 2, product.getProductName(), leftAlignCellStyle);
            createCellWithStyle(row, 3, product.getUnit(), dataCellStyle);
            createCellWithStyle(row, 4, product.getQuantitySold(), dataCellStyle);
            createCellWithStyle(row, 5, product.getTransactionCount(), dataCellStyle);
            createCellWithStyle(row, 6, product.getTotalAmount(), currencyStyle);
        }

        // Auto-size all columns
        for (int i = 0; i < Math.max(summaryHeaders.length, Math.max(transactionHeaders.length, productHeaders.length)); i++) {
            sheet.autoSizeColumn(i);
        }
    }


    // -------------------- Báo cáo nhà cung cấp --------------------

    @Override
    public SupplierReportDto getSupplierReport(LocalDate startDate, LocalDate endDate) {
        SupplierReportDto report = new SupplierReportDto();

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        // Số lượng nhà cung cấp mới và tổng tiền
        long newSuppliers = 0;
        if (startInstant != null && endInstant != null) {
            newSuppliers = supplierRepository.countNewSuppliersBetweenDates(startInstant, endInstant);
        }
        Double totalImportNewAmount1 = importRepository.sumTotalImportNewAmountBetweenDates(startInstant, endInstant);
        Double totalImportNewAmount = totalImportNewAmount1 != null ? totalImportNewAmount1 : 0.0;
        report.setNewSuppliers(newSuppliers != 0L ? newSuppliers : 0);
        report.setNewSuppliersAmount(totalImportNewAmount);

        // Số lượng nhà cung cấp cũ và tổng tiền
        long oldSuppliers = supplierRepository.countOldSuppliersBeforeDate(startInstant);
        Double totalImportOldAmount1 = importRepository.sumTotalImportAmountBeforeDate(startInstant ,endInstant);
        Double totalImportOldAmount = totalImportOldAmount1 != null ? totalImportOldAmount1 : 0.0;
        report.setOldSuppliers(oldSuppliers != 0L ? oldSuppliers : 0);
        report.setOldSuppliersAmount(totalImportOldAmount);

        // Tổng số nhà cung cấp
        long totalSuppliers = supplierRepository.countTotalSuppliers();
        report.setTotalSuppliers(totalSuppliers != 0L ? totalSuppliers : 0);

        // Tổng tiền đã nhập hàng từ nhà cung cấp
        Double totalImportAmount = totalImportNewAmount + totalImportOldAmount;
        report.setTotalImportAmount(totalImportAmount);

        // Tổng số lượng nhập hàng từ nhà cung cấp
        Integer totalImportQuantity = importRepository.sumTotalImportQuantityBetweenDates(startInstant, endInstant);
        report.setTotalImportQuantity(totalImportQuantity != null ? totalImportQuantity : 0);

        return report;
    }

    //List

    @Override
    public Page<SupplierInvoiceDto> getSupplierInvoiceList(
            String name,
            Boolean isNewSupplier,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        ZoneId systemZone = ZoneId.systemDefault();

        Instant startInstant = (startDate != null)
                ? startDate.atStartOfDay(systemZone).toInstant()
                : ZonedDateTime.now(systemZone).minusYears(1).toInstant(); // Một năm trước hiện tại

        Instant endInstant = (endDate != null)
                ? endDate.atTime(23, 59, 59).atZone(systemZone).toInstant()
                : Instant.now();

        Timestamp startTimestamp = Timestamp.from(startInstant);
        Timestamp endTimestamp = Timestamp.from(endInstant);

        Page<SupplierInvoiceProjection> projections = supplierRepository.findSupplierInvoices(
                name,
                isNewSupplier,
                startTimestamp,
                endTimestamp,
                pageable);

        // Chuyển đổi từ Projection sang DTO
        Page<SupplierInvoiceDto> dtos = projections.map(projection -> new SupplierInvoiceDto(
                projection.getSupplierId(),
                projection.getSupplierName(),
                projection.getPhoneNumber(),
                projection.getInvoiceCount(),
                projection.getTotalProductQuantity(),
                projection.getTotalReturnAmount(),
                projection.getTotalImportAmount()
        ));

        return dtos;
    }

    @Override
    public void exportSupplierReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException {
        // Step 1: Get supplier report data
        SupplierReportDto supplierReport = getSupplierReport(fromDate, toDate);

        // Step 2: Get supplier invoices data
        Instant startInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<SupplierInvoiceDto> supplierInvoices = supplierRepository.findSupplierInvoices(
                        null, null,
                        Timestamp.from(startInstant), Timestamp.from(endInstant),
                        PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .map(projection -> new SupplierInvoiceDto(
                        projection.getSupplierId(),
                        projection.getSupplierName(),
                        projection.getPhoneNumber(),
                        projection.getInvoiceCount(),
                        projection.getTotalProductQuantity(),
                        projection.getTotalReturnAmount(),
                        projection.getTotalImportAmount()))
                .collect(Collectors.toList());

        // Step 3: Create Excel workbook
        Workbook workbook = new XSSFWorkbook();

        // Add the combined sheet
        createCombinedSupplierSheet(workbook, supplierReport, supplierInvoices);

        // Write the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createCombinedSupplierSheet(Workbook workbook, SupplierReportDto report, List<SupplierInvoiceDto> invoices) {
        Sheet sheet = workbook.createSheet("Báo cáo nhà cung cấp");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Left-aligned styling for specific columns
        CellStyle leftAlignCellStyle = workbook.createCellStyle();
        leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);
        leftAlignCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        leftAlignCellStyle.setBorderTop(BorderStyle.THIN);
        leftAlignCellStyle.setBorderBottom(BorderStyle.THIN);
        leftAlignCellStyle.setBorderLeft(BorderStyle.THIN);
        leftAlignCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(leftAlignCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Part 1: Supplier Summary
        int currentRow = 0;
        Row summaryHeaderRow = sheet.createRow(currentRow++);
        String[] summaryHeaders = {"Nhà cung cấp mới", "Tổng tiền nhập từ nhà cung cấp mới", "Nhà cung cấp cũ",
                "Tổng tiền nhập từ nhà cung cấp cũ", "Tổng số nhà cung cấp", "Tổng tiền nhập hàng",
                "Tổng số lượng sản phẩm"};
        for (int i = 0; i < summaryHeaders.length; i++) {
            Cell cell = summaryHeaderRow.createCell(i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        Row summaryDataRow = sheet.createRow(currentRow++);
        createCellWithStyle(summaryDataRow, 0, report.getNewSuppliers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 1, report.getNewSuppliersAmount(), currencyStyle);
        createCellWithStyle(summaryDataRow, 2, report.getOldSuppliers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 3, report.getOldSuppliersAmount(), currencyStyle);
        createCellWithStyle(summaryDataRow, 4, report.getTotalSuppliers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 5, report.getTotalImportAmount(), currencyStyle);
        createCellWithStyle(summaryDataRow, 6, report.getTotalImportQuantity(), dataCellStyle);

        // Part 2: Supplier Invoices
        int invoiceStartRow = currentRow + 1;
        Row invoiceHeaderRow = sheet.createRow(invoiceStartRow++);
        String[] invoiceHeaders = {"STT", "Tên nhà cung cấp", "Số điện thoại", "Tổng hóa đơn",
                "Tổng sản phẩm", "Tổng tiền trả hàng", "Tổng tiền nhập hàng"};
        for (int i = 0; i < invoiceHeaders.length; i++) {
            Cell cell = invoiceHeaderRow.createCell(i);
            cell.setCellValue(invoiceHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        for (int i = 0; i < invoices.size(); i++) {
            SupplierInvoiceDto invoice = invoices.get(i);
            Row row = sheet.createRow(invoiceStartRow++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle); // STT
            createCellWithStyle(row, 1, invoice.getSupplierName(), leftAlignCellStyle); // Tên nhà cung cấp
            createCellWithStyle(row, 2, invoice.getPhoneNumber(), leftAlignCellStyle); // Số điện thoại
            createCellWithStyle(row, 3, invoice.getInvoiceCount(), dataCellStyle); // Tổng hóa đơn
            createCellWithStyle(row, 4, invoice.getTotalProductQuantity(), dataCellStyle); // Tổng sản phẩm
            createCellWithStyle(row, 5, invoice.getTotalReturnAmount(), currencyStyle); // Tổng tiền trả hàng
            createCellWithStyle(row, 6, invoice.getTotalImportAmount(), currencyStyle); // Tổng tiền nhập hàng
        }

        // Auto-size all columns
        for (int i = 0; i < Math.max(summaryHeaders.length, invoiceHeaders.length); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // -------------------- Báo cáo khách hàng --------------------

    @Override
    public CustomerReportDto getCustomerReport(LocalDate startDate, LocalDate endDate) {
        CustomerReportDto report = new CustomerReportDto();

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        // Số lượng khách hàng mới
        long newCustomers = customerRepository.countNewCustomersBetweenDates(startInstant, endInstant);
        report.setNewCustomers(newCustomers != 0L ? newCustomers : 0);

        // Tổng tiền khách hàng mới
        Double amountNewCustomers = saleOrderRepository.sumTotalAmountFromNewCustomersBetweenDates(startInstant, endInstant);
        report.setAmountNewCustomers(amountNewCustomers != null ? amountNewCustomers : 0.0);

        // Số lượng khách hàng cũ
        long oldCustomers = customerRepository.countOldCustomersBeforeDate(startInstant);
        report.setOldCustomers(oldCustomers != 0L ? oldCustomers : 0);

        // Tổng tiền khách hàng cũ
        Double amountOldCustomers = saleOrderRepository.sumTotalAmountFromOldCustomersBetweenDates(startInstant, endInstant);
        report.setAmountOldCustomers(amountOldCustomers != null ? amountOldCustomers : 0.0);


        // Tổng tiền thu từ khách hàng
        Double totalRevenueFromCustomers = saleOrderRepository.sumTotalAmountByCustomersBetweenDates(startInstant, endInstant);
        report.setTotalRevenueFromCustomers(totalRevenueFromCustomers != null ? totalRevenueFromCustomers : 0.0);

        // Tổng số lượng bán hàng cho khách hàng
        Integer totalQuantitySoldToCustomers = saleOrderItemRepository.sumTotalQuantitySoldToCustomersBetweenDates(startInstant, endInstant);
        report.setTotalQuantitySoldToCustomers(totalQuantitySoldToCustomers != null ? totalQuantitySoldToCustomers : 0);

        // Số lượng khách vãng lai (khách lẻ)
        long walkInCustomers = saleOrderRepository.countWalkInCustomersBetweenDates(startInstant, endInstant);
        report.setWalkInCustomers(walkInCustomers != 0L ? walkInCustomers : 0);

        //Tổng tiền khách vãng lai (khách lẻ)
        Double amountWalkinCustomer = saleOrderRepository.sumTotalAmountFromWalkInCustomersBetweenDates(startInstant, endInstant);
        report.setAmountWalkInCustomers(amountWalkinCustomer != null ? amountWalkinCustomer : 0.0);


        // Tổng số khách hàng
        long totalCustomers = oldCustomers + newCustomers + walkInCustomers;
        report.setTotalCustomers(totalCustomers != 0L ? totalCustomers : 0);

        return report;
    }

    //List
    @Override
    public Page<CustomerInvoiceDto> getCustomerInvoiceList(
            String searchTerm,
            Boolean isNewCustomer,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        Timestamp startTimestamp = Timestamp.from(startInstant);
        Timestamp endTimestamp = Timestamp.from(endInstant);

        Page<CustomerInvoiceProjection> projections = customerRepository.findCustomerInvoices(
                searchTerm,
                isNewCustomer,
                startTimestamp,
                endTimestamp,
                pageable);

        // Chuyển đổi từ Projection sang DTO
        Page<CustomerInvoiceDto> dtos = projections.map(projection -> new CustomerInvoiceDto(
                projection.getCustomerId(),
                projection.getCustomerName(),
                projection.getPhoneNumber(),
                projection.getInvoiceCount(),
                projection.getTotalProductQuantity(),
                projection.getTotalAmount()
        ));

        return dtos;
    }

    @Override
    public void exportCustomerReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException {
        // Step 1: Get customer report data
        CustomerReportDto customerReport = getCustomerReport(fromDate, toDate);

        // Step 2: Get customer invoices data
        Instant startInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<CustomerInvoiceDto> customerInvoices = customerRepository.findCustomerInvoices(
                        null, null,
                        Timestamp.from(startInstant), Timestamp.from(endInstant),
                        PageRequest.of(0, Integer.MAX_VALUE)
                ).stream()
                .map(projection -> new CustomerInvoiceDto(
                        projection.getCustomerId(),
                        projection.getCustomerName(),
                        projection.getPhoneNumber(),
                        projection.getInvoiceCount(),
                        projection.getTotalProductQuantity(),
                        projection.getTotalAmount()
                ))
                .collect(Collectors.toList());

        // Step 3: Create Excel workbook
        Workbook workbook = new XSSFWorkbook();

        // Add the combined sheet
        createCombinedCustomerSheet(workbook, customerReport, customerInvoices);

        // Write the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createCombinedCustomerSheet(Workbook workbook, CustomerReportDto report, List<CustomerInvoiceDto> customerInvoices) {
        Sheet sheet = workbook.createSheet("Báo cáo khách hàng");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Left-aligned styling
        CellStyle leftAlignCellStyle = workbook.createCellStyle();
        leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);
        leftAlignCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        leftAlignCellStyle.setBorderTop(BorderStyle.THIN);
        leftAlignCellStyle.setBorderBottom(BorderStyle.THIN);
        leftAlignCellStyle.setBorderLeft(BorderStyle.THIN);
        leftAlignCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling with left alignment
        CellStyle leftAlignCurrencyStyle = workbook.createCellStyle();
        leftAlignCurrencyStyle.cloneStyleFrom(leftAlignCellStyle);
        leftAlignCurrencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Part 1: Customer summary
        int currentRow = 0;
        Row summaryHeaderRow = sheet.createRow(currentRow++);
        String[] summaryHeaders = {"Khách hàng mới", "Doanh thu từ khách mới", "Khách hàng cũ", "Doanh thu từ khách cũ",
                "Khách vãng lai", "Doanh thu từ khách vãng lai", "Tổng khách hàng", "Tổng doanh thu", "Tổng sản phẩm bán"};
        for (int i = 0; i < summaryHeaders.length; i++) {
            Cell cell = summaryHeaderRow.createCell(i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        Row summaryDataRow = sheet.createRow(currentRow++);
        createCellWithStyle(summaryDataRow, 0, report.getNewCustomers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 1, report.getAmountNewCustomers() != null ? report.getAmountNewCustomers() : 0.0, leftAlignCurrencyStyle);
        createCellWithStyle(summaryDataRow, 2, report.getOldCustomers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 3, report.getAmountOldCustomers() != null ? report.getAmountOldCustomers() : 0.0, leftAlignCurrencyStyle);
        createCellWithStyle(summaryDataRow, 4, report.getWalkInCustomers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 5, report.getAmountWalkInCustomers() != null ? report.getAmountWalkInCustomers() : 0.0, leftAlignCurrencyStyle);
        createCellWithStyle(summaryDataRow, 6, report.getTotalCustomers(), dataCellStyle);
        createCellWithStyle(summaryDataRow, 7, report.getTotalRevenueFromCustomers(), leftAlignCurrencyStyle);
        createCellWithStyle(summaryDataRow, 8, report.getTotalQuantitySoldToCustomers(), dataCellStyle);

        // Auto-size columns after data insertion
        for (int i = 0; i < summaryHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Add a blank row for spacing
        currentRow++;

        // Part 2: Customer invoices
        int invoiceStartRow = currentRow;
        Row invoiceHeaderRow = sheet.createRow(invoiceStartRow++);
        String[] invoiceHeaders = {"STT", "Tên khách hàng", "Số điện thoại", "Tổng hóa đơn", "Tổng sản phẩm", "Tổng giá trị"};
        for (int i = 0; i < invoiceHeaders.length; i++) {
            Cell cell = invoiceHeaderRow.createCell(i);
            cell.setCellValue(invoiceHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        for (int i = 0; i < customerInvoices.size(); i++) {
            CustomerInvoiceDto invoice = customerInvoices.get(i);
            Row row = sheet.createRow(invoiceStartRow++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle); // STT
            createCellWithStyle(row, 1, invoice.getCustomerName(), leftAlignCellStyle); // Tên khách hàng
            createCellWithStyle(row, 2, invoice.getPhoneNumber(), leftAlignCellStyle); // Số điện thoại
            createCellWithStyle(row, 3, invoice.getInvoiceCount(), dataCellStyle); // Tổng hóa đơn
            createCellWithStyle(row, 4, invoice.getTotalProductQuantity(), dataCellStyle); // Tổng sản phẩm
            createCellWithStyle(row, 5, invoice.getTotalAmount(), leftAlignCurrencyStyle); // Tổng giá trị
        }

        // Auto-size columns after data insertion
        for (int i = 0; i < invoiceHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }

    }

    private void createCellWithStyle(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }
        cell.setCellStyle(style);
    }


    // -------------------- Báo cáo thu chi --------------------

    @Override
    public FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate) {
        FinancialReportDto report = new FinancialReportDto();

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        // Tổng thu từ bán hàng
        Double totalSales = saleOrderRepository.sumTotalAmountBetweenDates(startInstant, endInstant);
        totalSales = totalSales != null ? totalSales : 0.0;

        // Tổng thu từ trả hàng lại nhà cung cấp
        Double totalExportReturns = exportSlipRepository.sumTotalExportsByTypeBetweenDates(ExportType.RETURN_TO_SUPPLIER, startInstant, endInstant);
        totalExportReturns = totalExportReturns != null ? totalExportReturns : 0.0;

        // Tổng thu
        double totalIncome = totalSales + totalExportReturns;
        report.setTotalIncome(totalIncome);

        // Tổng chi từ trả hàng lại khách hàng
        Double totalRefunds = returnOrderRepository.sumTotalRefundsBetweenDates(startInstant, endInstant);
        totalRefunds = totalRefunds != null ? totalRefunds : 0.0;


        // Tổng chi từ nhập hàng
        Double totalImports = importRepository.sumTotalImportAmountBetweenDates(startInstant, endInstant);
        totalImports = totalImports != null ? totalImports : 0.0;


        // Tổng chi
        double totalExpense = totalImports + totalRefunds ;
        report.setTotalExpense(totalExpense);



        // Lợi nhuận
        double profit = totalIncome - totalExpense;
        report.setProfit(profit);


        return report;
    }

    @Override
    public Page<FinancialTransactionDto> getFinancialTransactions(
            String paymentMethod,
            String category,
            String receiptType,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize) {

        PageRequest pageable = PageRequest.of(pageNumber, pageSize);

        Instant startInstant;
        Instant endInstant;

        if (startDate != null && endDate != null) {
            // Nếu có cả startDate và endDate, sử dụng chúng để xác định khoảng thời gian
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        } else if (startDate != null) {
            // Nếu chỉ có startDate, tính từ đầu ngày đến cuối ngày đó
            startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            endInstant = startDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();  // Cuối ngày
        } else {
            endInstant = Instant.now();
            ZonedDateTime oneYearAgoZoned = ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1);
            startInstant = oneYearAgoZoned.toInstant();
        }

        return saleOrderRepository.findFinancialTransactions(
                paymentMethod, category, receiptType, startInstant, endInstant, pageable);
    }

    @Override
    public void exportFinancialReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException {
        // Step 1: Get the financial report data
        FinancialReportDto financialReport = getFinancialReport(fromDate, toDate);

        // Step 2: Get financial transactions data
        Instant startInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<FinancialTransactionDto> financialTransactions = saleOrderRepository.findFinancialTransactions(
                null, null, null, startInstant, endInstant,
                PageRequest.of(0, Integer.MAX_VALUE)).getContent();

        // Step 3: Create Excel workbook
        Workbook workbook = new XSSFWorkbook();

        // Add the combined sheet
        createCombinedFinancialSheet(workbook, financialReport, financialTransactions);

        // Write the workbook to the response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createCombinedFinancialSheet(Workbook workbook, FinancialReportDto report, List<FinancialTransactionDto> transactions) {
        Sheet sheet = workbook.createSheet("Báo cáo tài chính");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.DARK_BLUE.getIndex()); // Text color close to #09446D

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex()); // Background color close to #C6E1F8
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling for numeric values (aligned center)
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Left-aligned styling for specific columns
        CellStyle leftAlignCellStyle = workbook.createCellStyle();
        leftAlignCellStyle.setAlignment(HorizontalAlignment.LEFT);
        leftAlignCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        leftAlignCellStyle.setBorderTop(BorderStyle.THIN);
        leftAlignCellStyle.setBorderBottom(BorderStyle.THIN);
        leftAlignCellStyle.setBorderLeft(BorderStyle.THIN);
        leftAlignCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(leftAlignCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));
        currencyStyle.setBorderTop(BorderStyle.THIN);
        currencyStyle.setBorderBottom(BorderStyle.THIN);
        currencyStyle.setBorderLeft(BorderStyle.THIN);
        currencyStyle.setBorderRight(BorderStyle.THIN);

        // Date styling
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy     HH:mm"));
        dateStyle.setBorderTop(BorderStyle.THIN);
        dateStyle.setBorderBottom(BorderStyle.THIN);
        dateStyle.setBorderLeft(BorderStyle.THIN);
        dateStyle.setBorderRight(BorderStyle.THIN);

        // Part 1: Financial summary
        int currentRow = 0;
        Row summaryHeaderRow = sheet.createRow(currentRow++);
        String[] summaryHeaders = {"Tổng thu", "Tổng chi", "Lợi nhuận"};
        for (int i = 0; i < summaryHeaders.length; i++) {
            Cell cell = summaryHeaderRow.createCell(i);
            cell.setCellValue(summaryHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        Row summaryDataRow = sheet.createRow(currentRow++);
        createCellWithStyle(summaryDataRow, 0, report.getTotalIncome(), currencyStyle);
        createCellWithStyle(summaryDataRow, 1, report.getTotalExpense(), currencyStyle);
        createCellWithStyle(summaryDataRow, 2, report.getProfit(), currencyStyle);

        // Add a blank row for spacing
        currentRow++;

        // Part 2: Financial transactions
        int transactionStartRow = currentRow;
        Row transactionHeaderRow = sheet.createRow(transactionStartRow++);
        String[] transactionHeaders = {"STT", "Mã phiếu", "Loại phiếu", "Ngày", "Danh mục", "Phương thức thanh toán", "Tổng tiền"};
        for (int i = 0; i < transactionHeaders.length; i++) {
            Cell cell = transactionHeaderRow.createCell(i);
            cell.setCellValue(transactionHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy     HH:mm").withZone(ZoneId.systemDefault());

        for (int i = 0; i < transactions.size(); i++) {
            FinancialTransactionDto transaction = transactions.get(i);
            Row row = sheet.createRow(transactionStartRow++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle); // STT
            createCellWithStyle(row, 1, transaction.getInvoiceNumber(), dataCellStyle); // Mã phiếu
            createCellWithStyle(row, 2, transaction.getReceiptType(), dataCellStyle); // Loại phiếu
            createCellWithStyle(row, 3, formatter.format(transaction.getCreationDate()), dateStyle); // Ngày
            createCellWithStyle(row, 4, transaction.getCategory(), leftAlignCellStyle); // Danh mục

            // Map payment method
            String paymentMethod = transaction.getPaymentMethod();
            if ("TRANSFER".equals(paymentMethod)) {
                paymentMethod = "Chuyển khoản";
            } else if ("CASH".equals(paymentMethod)) {
                paymentMethod = "Tiền mặt";
            } else {
                paymentMethod = "Tiền mặt";
            }
            createCellWithStyle(row, 5, paymentMethod, leftAlignCellStyle); // Phương thức thanh toán
            createCellWithStyle(row, 6, transaction.getTotalAmount(), currencyStyle); // Tổng tiền
        }

        // Auto-size all columns
        for (int i = 0; i < transactionHeaders.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }


}