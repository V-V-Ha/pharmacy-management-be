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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            startInstant = Instant.now().minus(1, ChronoUnit.DAYS);
            endInstant = Instant.now();
        }

        // Tính tồn kho đầu kỳ
        int beginningInventoryQuantity = calculateBeginningInventoryQuantity(startInstant);
        double beginningInventoryAmount = calculateBeginningInventoryAmount(startInstant);
        report.setBeginningInventoryQuantity(beginningInventoryQuantity);
        report.setBeginningInventoryAmount(beginningInventoryAmount);

        // Tính nhập kho trong kỳ
        int goodsReceivedQuantity = calculateGoodsReceivedQuantity(startInstant, endInstant);
        double goodsReceivedAmount = calculateGoodsReceivedAmount(startInstant, endInstant);
        report.setGoodsReceivedQuantity(goodsReceivedQuantity);
        report.setGoodsReceivedAmount(goodsReceivedAmount);

        // Tính xuất kho trong kỳ
        int goodsIssuedQuantity = calculateGoodsIssuedQuantity(startInstant, endInstant);
        double goodsIssuedAmount = calculateGoodsIssuedAmount(startInstant, endInstant);
        report.setGoodsIssuedQuantity(goodsIssuedQuantity);
        report.setGoodsIssuedAmount(goodsIssuedAmount);

        // Tính xuất hủy
        int goodsDestroyedQuantity = calculateGoodsDestroyedQuantity(startInstant, endInstant);
        double goodsDestroyedAmount = calculateGoodsDestroyedAmount(startInstant, endInstant);
        report.setGoodsDestroyedQuantity(goodsDestroyedQuantity);
        report.setGoodsDestroyedAmount(goodsDestroyedAmount);

        // Tính xuất trả
        int goodsReturnedQuantity = calculateGoodsReturnedQuantity(startInstant, endInstant);
        double goodsReturnedAmount = calculateGoodsReturnedAmount(startInstant, endInstant);
        report.setGoodsReturnedQuantity(goodsReturnedQuantity);
        report.setGoodsReturnedAmount(goodsReturnedAmount);

        // Tính tồn kho hiện tại
        Integer currentInventoryQuantity = productRepository.calculateCurrentInventoryQuantity();
        double currentInventoryAmount = beginningInventoryAmount + goodsReceivedAmount - goodsIssuedAmount;
        report.setCurrentInventoryQuantity(currentInventoryQuantity != null ? currentInventoryQuantity : 0);
        report.setCurrentInventoryAmount(currentInventoryAmount);

        // Tính số lượng sản phẩm sắp hết hàng
        int nearlyOutOfStockProducts = productRepository.findNearlyOutOfStockProducts(10).size();
        report.setNearlyOutOfStockProducts(nearlyOutOfStockProducts);

        // Tính số lượng sản phẩm hết hàng
        int outOfStockProducts = productRepository.countOutOfStock();
        report.setOutOfStockProducts(outOfStockProducts);


        Instant thresholdDate = Instant.now().plus(60, ChronoUnit.DAYS);

        // Tính sản phẩm sắp hết hạn
        int nearlyExpiredItems = importItemRepository.findNearlyExpiredItems(Instant.now(),thresholdDate).size();
        report.setNearlyExpiredItems(nearlyExpiredItems);

        // Tính sản phẩm hết hạn
        int expiredItems = importItemRepository.findExpiredItems(Instant.now()).size();
        report.setExpiredItems(expiredItems);


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
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

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
            startInstant = Instant.now().minus(1, ChronoUnit.DAYS);
            endInstant = Instant.now();
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
            report.setBeginningInventoryQuantity(beginningQuantity);
            report.setBeginningInventoryAmount(beginningAmount);

            // Nhập kho
            int receivedQuantity = calculateGoodsReceivedQuantityByProduct(productId, startInstant, endInstant);
            double receivedAmount = calculateGoodsReceivedAmountByProduct(productId, startInstant, endInstant);
            report.setGoodsReceivedQuantity(receivedQuantity);
            report.setGoodsReceivedAmount(receivedAmount);

            // Xuất kho
            int issuedQuantity = calculateGoodsIssuedQuantityByProduct(productId, startInstant, endInstant);
            double issuedAmount = calculateGoodsIssuedAmountByProduct(productId, startInstant, endInstant);
            report.setGoodsIssuedQuantity(issuedQuantity);
            report.setGoodsIssuedAmount(issuedAmount);

            // Tồn cuối kỳ
            int endingQuantity = beginningQuantity + receivedQuantity - issuedQuantity;
            double endingAmount = beginningAmount + receivedAmount - issuedAmount;
            report.setEndingInventoryQuantity(endingQuantity);
            report.setEndingInventoryAmount(endingAmount);

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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();
        }

        return saleOrderRepository.findProductSales(
                productName, productCode, startInstant, endInstant, pageable);
    }

    @Override
    public void exportSalesReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException {
        // Step 1: Get report data
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

        // Add sales summary sheet
        createSalesSummarySheet(workbook, salesReport);

        // Add sales transactions sheet
        createSalesTransactionsSheet(workbook, salesTransactions);

        // Add product sales sheet
        createProductSalesSheet(workbook, productSales);

        // Write the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createSalesSummarySheet(Workbook workbook, SalesReportDto report) {
        Sheet sheet = workbook.createSheet("Tổng hợp bán hàng");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tổng hóa đơn", "Doanh thu hóa đơn", "Số lượng đã bán", "Doanh thu tiền mặt", "Doanh thu chuyển khoản"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Add data row
        Row dataRow = sheet.createRow(1);
        createCellWithStyle(dataRow, 0, report.getTotalInvoices(), dataCellStyle);
        createCellWithStyle(dataRow, 1, report.getTotalRevenue(), currencyStyle);
        createCellWithStyle(dataRow, 2, report.getTotalQuantitySold(), dataCellStyle);
        createCellWithStyle(dataRow, 3, report.getCashRevenue(), currencyStyle);
        createCellWithStyle(dataRow, 4, report.getTransferRevenue(), currencyStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSalesTransactionsSheet(Workbook workbook, List<SalesTransactionDto> transactions) {
        Sheet sheet = workbook.createSheet("Chi tiết bán hàng");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Date styling for date columns
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy     HH:mm"));  // Apply same date format as before

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Mã hóa đơn", "Ngày tạo", "Khách hàng", "Loại hóa đơn", "Phương thức thanh toán", "Tổng tiền"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < transactions.size(); i++) {
            SalesTransactionDto transaction = transactions.get(i);
            Row row = sheet.createRow(rowNum++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle);
            createCellWithStyle(row, 1, transaction.getInvoiceNumber(), dataCellStyle);

            // Apply date format to "Ngày tạo"
            createCellWithStyle(row, 2, DateTimeFormatter.ofPattern("dd-MM-yyyy     HH:mm")
                    .withZone(ZoneOffset.ofHours(7)).format(transaction.getCreationDate()), dateStyle);

            createCellWithStyle(row, 3, transaction.getCustomerName(), dataCellStyle);
            createCellWithStyle(row, 4, transaction.getVoucherType(), dataCellStyle);
            createCellWithStyle(row, 5, transaction.getPaymentMethod(), dataCellStyle);
            createCellWithStyle(row, 6, transaction.getTotalAmount(), currencyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createProductSalesSheet(Workbook workbook, List<ProductSalesDto> products) {
        Sheet sheet = workbook.createSheet("Chi tiết sản phẩm bán");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0"));

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Mã sản phẩm", "Tên sản phẩm", "Đơn vị", "Số lượng bán", "Số giao dịch", "Tổng tiền"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < products.size(); i++) {
            ProductSalesDto product = products.get(i);
            Row row = sheet.createRow(rowNum++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle);
            createCellWithStyle(row, 1, product.getProductCode(), dataCellStyle);
            createCellWithStyle(row, 2, product.getProductName(), dataCellStyle);
            createCellWithStyle(row, 3, product.getUnit(), dataCellStyle);
            createCellWithStyle(row, 4, product.getQuantitySold(), dataCellStyle);
            createCellWithStyle(row, 5, product.getTransactionCount(), dataCellStyle);
            createCellWithStyle(row, 6, product.getTotalAmount(), currencyStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();  // Thời gian hiện tại
        }

        // Số lượng nhà cung cấp mới và tổng tiền
        long newSuppliers = 0;
        if (startDate != null && endDate != null) {
            newSuppliers = supplierRepository.countNewSuppliersBetweenDates(startInstant, endInstant);
        }
        Double totalImportNewAmount = importRepository.sumTotalImportNewAmountBetweenDates(startInstant, endInstant);
        report.setNewSuppliers(newSuppliers);
        report.setNewSuppliersAmount(totalImportNewAmount != null ? totalImportNewAmount : 0.0);

        // Số lượng nhà cung cấp cũ và tổng tiền
        long oldSuppliers = supplierRepository.countOldSuppliersBeforeDate(startInstant);
        Double totalImportOldAmount = importRepository.sumTotalImportAmountBeforeDate(startInstant);
        report.setOldSuppliers(oldSuppliers);
        report.setOldSuppliersAmount(totalImportOldAmount != null ? totalImportOldAmount : 0.0);

        // Tổng số nhà cung cấp
        long totalSuppliers = supplierRepository.countTotalSuppliers();
        report.setTotalSuppliers(totalSuppliers);

        // Tổng tiền đã nhập hàng từ nhà cung cấp
        Double totalImportAmount = importRepository.sumTotalImportAmountBetweenDates(startInstant, endInstant);
        report.setTotalImportAmount(totalImportAmount != null ? totalImportAmount : 0.0);

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
        Instant startInstant = (startDate != null)
                ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                : Instant.EPOCH;
        Instant endInstant = (endDate != null)
                ? endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
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
        // Step 1: Get report data
        SupplierReportDto supplierReport = getSupplierReport(fromDate, toDate);

        // Step 2: Get supplier invoices data
        Instant startInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<SupplierInvoiceDto> supplierInvoices = supplierRepository.findSupplierInvoices(
                        null, null,
                        Timestamp.from(startInstant), Timestamp.from(endInstant),
                        PageRequest.of(0, Integer.MAX_VALUE)
                ).stream()
                .map(projection -> new SupplierInvoiceDto(
                        projection.getSupplierId(),
                        projection.getSupplierName(),
                        projection.getPhoneNumber(),
                        projection.getInvoiceCount(),
                        projection.getTotalProductQuantity(),
                        projection.getTotalReturnAmount(),
                        projection.getTotalImportAmount()
                ))
                .collect(Collectors.toList());

        // Step 3: Create Excel workbook
        Workbook workbook = new XSSFWorkbook();

        // Add the supplier summary sheet
        createSupplierSummarySheet(workbook, supplierReport);

        // Add the supplier invoice details sheet
        createSupplierInvoiceSheet(workbook, supplierInvoices);

        // Write the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createSupplierSummarySheet(Workbook workbook, SupplierReportDto report) {
        Sheet sheet = workbook.createSheet("Tổng hợp nhà cung cấp");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0")); // No decimals for currency

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nhà cung cấp mới", "Tổng tiền nhập từ nhà cung cấp mới",
                "Nhà cung cấp cũ", "Tổng tiền nhập từ nhà cung cấp cũ",
                "Tổng số nhà cung cấp", "Tổng tiền nhập hàng", "Tổng số lượng sản phẩm"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Add data row
        Row dataRow = sheet.createRow(1);
        createCellWithStyle(dataRow, 0, report.getNewSuppliers(), dataCellStyle);
        createCellWithStyle(dataRow, 1, report.getNewSuppliersAmount(), currencyStyle);
        createCellWithStyle(dataRow, 2, report.getOldSuppliers(), dataCellStyle);
        createCellWithStyle(dataRow, 3, report.getOldSuppliersAmount(), currencyStyle);
        createCellWithStyle(dataRow, 4, report.getTotalSuppliers(), dataCellStyle);
        createCellWithStyle(dataRow, 5, report.getTotalImportAmount(), currencyStyle);
        createCellWithStyle(dataRow, 6, report.getTotalImportQuantity(), dataCellStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSupplierInvoiceSheet(Workbook workbook, List<SupplierInvoiceDto> supplierInvoices) {
        Sheet sheet = workbook.createSheet("Chi tiết nhà cung cấp");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0")); // No decimals for currency

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Tên nhà cung cấp", "Số điện thoại",
                "Tổng hóa đơn", "Tổng sản phẩm",
                "Tổng tiền trả hàng", "Tổng tiền nhập hàng"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < supplierInvoices.size(); i++) {
            SupplierInvoiceDto invoice = supplierInvoices.get(i);
            Row row = sheet.createRow(rowNum++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle); // STT
            createCellWithStyle(row, 1, invoice.getSupplierName(), dataCellStyle); // Tên nhà cung cấp
            createCellWithStyle(row, 2, invoice.getPhoneNumber(), dataCellStyle); // Số điện thoại
            createCellWithStyle(row, 3, invoice.getInvoiceCount(), dataCellStyle); // Tổng hóa đơn
            createCellWithStyle(row, 4, invoice.getTotalProductQuantity(), dataCellStyle); // Tổng sản phẩm
            createCellWithStyle(row, 5, invoice.getTotalReturnAmount(), currencyStyle); // Tổng tiền trả hàng
            createCellWithStyle(row, 6, invoice.getTotalImportAmount(), currencyStyle); // Tổng tiền nhập hàng
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();  // Thời gian hiện tại
        }

        // Số lượng khách hàng mới
        long newCustomers = customerRepository.countNewCustomersBetweenDates(startInstant, endInstant);
        report.setNewCustomers(newCustomers);

        // Tổng tiền khách hàng mới
        Double amountNewCustomers = saleOrderRepository.sumTotalAmountFromNewCustomersBetweenDates(startInstant, endInstant);
        report.setAmountNewCustomers(amountNewCustomers);

        // Số lượng khách hàng cũ
        long oldCustomers = customerRepository.countOldCustomersBeforeDate(startInstant);
        report.setOldCustomers(oldCustomers);

        // Tổng tiền khách hàng cũ
        Double amountOldCustomers = saleOrderRepository.sumTotalAmountFromOldCustomersBetweenDates(startInstant, endInstant);
        report.setAmountOldCustomers(amountOldCustomers);

        // Tổng số khách hàng
        long totalCustomers = customerRepository.countTotalCustomers();
        report.setTotalCustomers(totalCustomers);

        // Tổng tiền thu từ khách hàng
        Double totalRevenueFromCustomers = saleOrderRepository.sumTotalAmountByCustomersBetweenDates(startInstant, endInstant);
        report.setTotalRevenueFromCustomers(totalRevenueFromCustomers != null ? totalRevenueFromCustomers : 0.0);

        // Tổng số lượng bán hàng cho khách hàng
        Integer totalQuantitySoldToCustomers = saleOrderItemRepository.sumTotalQuantitySoldToCustomersBetweenDates(startInstant, endInstant);
        report.setTotalQuantitySoldToCustomers(totalQuantitySoldToCustomers != null ? totalQuantitySoldToCustomers : 0);

        // Số lượng khách vãng lai (khách lẻ)
        long walkInCustomers = saleOrderRepository.countWalkInCustomersBetweenDates(startInstant, endInstant);
        report.setWalkInCustomers(walkInCustomers);

        //Tổng tiền khách vãng lai (khách lẻ)
        Double amountWalkinCustomer = saleOrderRepository.sumTotalAmountFromWalkInCustomersBetweenDates(startInstant, endInstant);
        report.setAmountWalkInCustomers(amountWalkinCustomer);


        return report;
    }

    //List
    @Override
    public Page<CustomerInvoiceDto> getCustomerInvoiceList(
            String name,
            String phone,
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();  // Thời gian hiện tại
        }

        Timestamp startTimestamp = Timestamp.from(startInstant);
        Timestamp endTimestamp = Timestamp.from(endInstant);

        Page<CustomerInvoiceProjection> projections = customerRepository.findCustomerInvoices(
                name,
                phone,
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
        // Step 1: Get report data
        CustomerReportDto customerReport = getCustomerReport(fromDate, toDate);

        // Step 2: Get customer invoices data
        Instant startInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<CustomerInvoiceDto> customerInvoices = customerRepository.findCustomerInvoices(
                        null, null, null,
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

        // Add the customer summary sheet
        createCustomerSummarySheet(workbook, customerReport);

        // Add the customer invoice details sheet
        createCustomerInvoiceSheet(workbook, customerInvoices);

        // Write the workbook to response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createCustomerSummarySheet(Workbook workbook, CustomerReportDto report) {
        Sheet sheet = workbook.createSheet("Tổng hợp khách hàng");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0")); // No decimals for currency

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Khách hàng mới", "Doanh thu từ khách mới", "Khách hàng cũ", "Doanh thu từ khách cũ",
                "Khách vãng lai", "Doanh thu từ khách vãng lai", "Tổng khách hàng", "Tổng doanh thu", "Tổng sản phẩm bán"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Add data row
        Row dataRow = sheet.createRow(1);
        createCellWithStyle(dataRow, 0, report.getNewCustomers(), dataCellStyle);
        createCellWithStyle(dataRow, 1, report.getAmountNewCustomers() != null ? report.getAmountNewCustomers() : 0.0, currencyStyle);
        createCellWithStyle(dataRow, 2, report.getOldCustomers(), dataCellStyle);
        createCellWithStyle(dataRow, 3, report.getAmountOldCustomers() != null ? report.getAmountOldCustomers() : 0.0, currencyStyle);
        createCellWithStyle(dataRow, 4, report.getWalkInCustomers(), dataCellStyle);
        createCellWithStyle(dataRow, 5, report.getAmountWalkInCustomers() != null ? report.getAmountWalkInCustomers() : 0.0, currencyStyle);
        createCellWithStyle(dataRow, 6, report.getTotalCustomers(), dataCellStyle);
        createCellWithStyle(dataRow, 7, report.getTotalRevenueFromCustomers(), currencyStyle);
        createCellWithStyle(dataRow, 8, report.getTotalQuantitySoldToCustomers(), dataCellStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createCustomerInvoiceSheet(Workbook workbook, List<CustomerInvoiceDto> customerInvoices) {
        Sheet sheet = workbook.createSheet("Chi tiết khách hàng");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0")); // No decimals for currency

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"STT", "Tên khách hàng", "Số điện thoại", "Tổng hóa đơn", "Tổng sản phẩm", "Tổng giá trị"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Fill data rows
        int rowNum = 1;
        for (int i = 0; i < customerInvoices.size(); i++) {
            CustomerInvoiceDto invoice = customerInvoices.get(i);
            Row row = sheet.createRow(rowNum++);

            createCellWithStyle(row, 0, i + 1, dataCellStyle); // STT
            createCellWithStyle(row, 1, invoice.getCustomerName(), dataCellStyle); // Tên khách hàng
            createCellWithStyle(row, 2, invoice.getPhoneNumber(), dataCellStyle); // Số điện thoại
            createCellWithStyle(row, 3, invoice.getInvoiceCount(), dataCellStyle); // Tổng hóa đơn
            createCellWithStyle(row, 4, invoice.getTotalProductQuantity(), dataCellStyle); // Tổng sản phẩm
            createCellWithStyle(row, 5, invoice.getTotalAmount(), currencyStyle); // Tổng giá trị
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.MIN;  // Mốc thời gian rất xa trong quá khứ
            endInstant = Instant.now();  // Thời gian hiện tại
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
            // Nếu không có startDate và endDate, lấy toàn bộ dữ liệu
            startInstant = Instant.EPOCH;
            endInstant = Instant.now();  // Thời gian hiện tại
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

        // Add the financial summary sheet
        createFinancialSummarySheet(workbook, financialReport);

        // Add the financial transactions sheet
        createFinancialTransactionSheet(workbook, financialTransactions);

        // Write the workbook to the response
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    private void createFinancialSummarySheet(Workbook workbook, FinancialReportDto report) {
        Sheet sheet = workbook.createSheet("Tổng hợp thu chi");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0")); // No decimals for currency

        // Date styling for date columns
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy     HH:mm"));  // Add space between date and time

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tổng thu", "Tổng chi", "Lợi nhuận"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Add data row
        Row dataRow = sheet.createRow(1);
        createCellWithStyle(dataRow, 0, report.getTotalIncome(), currencyStyle);
        createCellWithStyle(dataRow, 1, report.getTotalExpense(), currencyStyle);
        createCellWithStyle(dataRow, 2, report.getProfit(), currencyStyle);

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createFinancialTransactionSheet(Workbook workbook, List<FinancialTransactionDto> transactions) {
        Sheet sheet = workbook.createSheet("Chi tiết thu chi");

        // Header styling
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);

        // Data styling
        CellStyle dataCellStyle = workbook.createCellStyle();
        dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
        dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataCellStyle.setBorderBottom(BorderStyle.THIN);
        dataCellStyle.setBorderTop(BorderStyle.THIN);
        dataCellStyle.setBorderLeft(BorderStyle.THIN);
        dataCellStyle.setBorderRight(BorderStyle.THIN);

        // Currency styling
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.cloneStyleFrom(dataCellStyle);
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("₫ #,##0")); // No decimals for currency

        // Date styling for date columns
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.cloneStyleFrom(dataCellStyle);
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd-MM-yyyy     HH:mm"));  // Add space between date and time

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Mã phiếu", "Loại phiếu", "Ngày", "Danh mục", "Phương thức thanh toán", "Tổng tiền"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Define the date formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy     HH:mm")
                .withZone(ZoneOffset.ofHours(7)); // Adjust the zone offset as needed

        // Fill data rows
        int rowNum = 1;
        for (FinancialTransactionDto transaction : transactions) {
            Row row = sheet.createRow(rowNum++);

            createCellWithStyle(row, 0, transaction.getInvoiceNumber(), dataCellStyle); // Mã phiếu
            createCellWithStyle(row, 1, transaction.getReceiptType(), dataCellStyle); // Loại phiếu

            // Apply date format for the "Ngày" (Date) column
            String formattedDate = formatter.format(transaction.getCreationDate());
            createCellWithStyle(row, 2, formattedDate, dateStyle); // Ngày (formatted)

            createCellWithStyle(row, 3, transaction.getCategory(), dataCellStyle); // Danh mục
            createCellWithStyle(row, 4, transaction.getPaymentMethod(), dataCellStyle); // Phương thức thanh toán
            createCellWithStyle(row, 5, transaction.getTotalAmount(), currencyStyle); // Tổng tiền
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }





}