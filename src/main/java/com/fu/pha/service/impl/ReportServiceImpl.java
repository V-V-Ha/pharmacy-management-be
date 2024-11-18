package com.fu.pha.service.impl;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.reportEntity.ImportItemReportDto;
import com.fu.pha.dto.response.report.reportEntity.ProductReportDto;
import com.fu.pha.enums.ExportType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.repository.*;
import com.fu.pha.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public InventoryReportDto getInventoryReport(LocalDate startDate, LocalDate endDate) {
        InventoryReportDto report = new InventoryReportDto();

//        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
//
//        // Tính tồn kho đầu kỳ
//        int beginningInventoryQuantity = calculateBeginningInventoryQuantity(startInstant);
//        double beginningInventoryAmount = calculateBeginningInventoryAmount(startInstant);
//        report.setBeginningInventoryQuantity(beginningInventoryQuantity);
//        report.setBeginningInventoryAmount(beginningInventoryAmount);
//
//        // Tính nhập kho trong kỳ
//        int goodsReceivedQuantity = calculateGoodsReceivedQuantity(startInstant, endInstant);
//        double goodsReceivedAmount = calculateGoodsReceivedAmount(startInstant, endInstant);
//        report.setGoodsReceivedQuantity(goodsReceivedQuantity);
//        report.setGoodsReceivedAmount(goodsReceivedAmount);
//
//        // Tính xuất kho trong kỳ
//        int goodsIssuedQuantity = calculateGoodsIssuedQuantity(startInstant, endInstant);
//        double goodsIssuedAmount = calculateGoodsIssuedAmount(startInstant, endInstant);
//        report.setGoodsIssuedQuantity(goodsIssuedQuantity);
//        report.setGoodsIssuedAmount(goodsIssuedAmount);
//
//        // Tính xuất hủy
//        int goodsDestroyedQuantity = calculateGoodsDestroyedQuantity(startInstant, endInstant);
//        double goodsDestroyedAmount = calculateGoodsDestroyedAmount(startInstant, endInstant);
//        report.setGoodsDestroyedQuantity(goodsDestroyedQuantity);
//        report.setGoodsDestroyedAmount(goodsDestroyedAmount);
//
//        // Tính xuất trả
//        int goodsReturnedQuantity = calculateGoodsReturnedQuantity(startInstant, endInstant);
//        double goodsReturnedAmount = calculateGoodsReturnedAmount(startInstant, endInstant);
//        report.setGoodsReturnedQuantity(goodsReturnedQuantity);
//        report.setGoodsReturnedAmount(goodsReturnedAmount);
//
//        // Lấy danh sách sản phẩm hết hàng
//        List<ProductReportDto> outOfStockProducts = productRepository.findOutOfStockProducts();
//        report.setOutOfStockProducts(outOfStockProducts);
//
//        // Lấy danh sách sản phẩm sắp hết hàng (ví dụ: tồn kho <= 10)
//        List<ProductReportDto> nearlyOutOfStockProducts = productRepository.findNearlyOutOfStockProducts(10);
//        report.setNearlyOutOfStockProducts(nearlyOutOfStockProducts);
//
//        // Lấy danh sách sản phẩm đã hết hạn
//        Instant currentDate = Instant.now();
//        List<ImportItemReportDto> expiredItems = importItemRepository.findExpiredItems(currentDate);
//        report.setExpiredItems(expiredItems);
//
//        // Lấy danh sách sản phẩm sắp hết hạn (trong vòng 30 ngày tới)
//        Instant nearExpiryDate = currentDate.plus(30, ChronoUnit.DAYS);
//        List<ImportItemReportDto> nearlyExpiredItems = importItemRepository.findNearlyExpiredItems(currentDate, nearExpiryDate);
//        report.setNearlyExpiredItems(nearlyExpiredItems);
//
//        // Tính tồn kho hiện tại
//        Integer currentInventoryQuantity = productRepository.calculateCurrentInventoryQuantity();
//        Double currentInventoryAmount = productRepository.calculateCurrentInventoryAmount();
//        report.setCurrentInventoryQuantity(currentInventoryQuantity != null ? currentInventoryQuantity : 0);
//        report.setCurrentInventoryAmount(currentInventoryAmount != null ? currentInventoryAmount : 0.0);

        return report;
    }

    // Các phương thức hỗ trợ cho báo cáo kho
    private int calculateBeginningInventoryQuantity(Instant startDate) {
        Integer totalReceivedBeforeStart = importItemRepository.sumQuantityBeforeDate(startDate);
        Integer totalExportedBeforeStart = exportSlipItemRepository.sumQuantityBeforeDate(startDate);
        Integer totalSoldBeforeStart = saleOrderItemRepository.sumQuantityBeforeDate(startDate);
        Integer totalReturnedBeforeStart = returnOrderItemRepository.sumQuantityBeforeDate(startDate);

        // Tồn kho đầu kỳ (số lượng) = Tổng nhập trước kỳ - (Tổng xuất trước kỳ + Tổng bán trước kỳ) + Tổng hàng trả lại trước kỳ
        return totalReceivedBeforeStart - (totalExportedBeforeStart + totalSoldBeforeStart) + totalReturnedBeforeStart;
    }

    private double calculateBeginningInventoryAmount(Instant startDate) {
        Double totalReceivedAmountBeforeStart = importItemRepository.sumAmountBeforeDate(startDate);
        Double totalExportedAmountBeforeStart = exportSlipItemRepository.sumAmountBeforeDate(startDate);
        Double totalSoldAmountBeforeStart = saleOrderItemRepository.sumAmountBeforeDate(startDate);
        Double totalReturnedAmountBeforeStart = returnOrderItemRepository.sumAmountBeforeDate(startDate);

        // Tồn kho đầu kỳ (tổng tiền) = Tổng tiền nhập trước kỳ - (Tổng tiền xuất trước kỳ + Tổng tiền bán trước kỳ) + Tổng tiền hàng trả lại trước kỳ
        return totalReceivedAmountBeforeStart - (totalExportedAmountBeforeStart + totalSoldAmountBeforeStart) + totalReturnedAmountBeforeStart;
    }

    private int calculateGoodsReceivedQuantity(Instant startDate, Instant endDate) {
        return importItemRepository.sumQuantityBetweenDates(startDate, endDate);
    }

    private double calculateGoodsReceivedAmount(Instant startDate, Instant endDate) {
        return importItemRepository.sumAmountBetweenDates(startDate, endDate);
    }

    private int calculateGoodsIssuedQuantity(Instant startDate, Instant endDate) {
        Integer totalExported = exportSlipItemRepository.sumQuantityBetweenDates(startDate, endDate);
        Integer totalSold = saleOrderItemRepository.sumQuantityBetweenDates(startDate, endDate);
        Integer totalReturned = returnOrderItemRepository.sumQuantityBetweenDates(startDate, endDate);

        // Tổng xuất kho (số lượng) = Tổng xuất trong kỳ + Tổng bán trong kỳ - Tổng hàng trả lại trong kỳ
        return totalExported + totalSold - totalReturned;
    }

    private double calculateGoodsIssuedAmount(Instant startDate, Instant endDate) {
        Double totalExportedAmount = exportSlipItemRepository.sumAmountBetweenDates(startDate, endDate);
        Double totalSoldAmount = saleOrderItemRepository.sumAmountBetweenDates(startDate, endDate);
        Double totalReturnedAmount = returnOrderItemRepository.sumAmountBetweenDates(startDate, endDate);

        // Tổng xuất kho (tổng tiền) = Tổng tiền xuất trong kỳ + Tổng tiền bán trong kỳ - Tổng tiền hàng trả lại trong kỳ
        return totalExportedAmount + totalSoldAmount - totalReturnedAmount;
    }

    private int calculateGoodsDestroyedQuantity(Instant startDate, Instant endDate) {
        return exportSlipItemRepository.sumQuantityByTypeBetweenDates(ExportType.DESTROY, startDate, endDate);
    }

    private double calculateGoodsDestroyedAmount(Instant startDate, Instant endDate) {
        return exportSlipItemRepository.sumAmountByTypeBetweenDates(ExportType.DESTROY, startDate, endDate);
    }

    private int calculateGoodsReturnedQuantity(Instant startDate, Instant endDate) {
        return exportSlipItemRepository.sumQuantityByTypeBetweenDates(ExportType.RETURN_TO_SUPPLIER, startDate, endDate);
    }

    private double calculateGoodsReturnedAmount(Instant startDate, Instant endDate) {
        return exportSlipItemRepository.sumAmountByTypeBetweenDates(ExportType.RETURN_TO_SUPPLIER, startDate, endDate);
    }

    // -------------------- Báo cáo bán hàng --------------------

    @Override
    public SalesReportDto getSalesReport(LocalDate startDate, LocalDate endDate) {
        SalesReportDto report = new SalesReportDto();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Tổng số hóa đơn
        long totalInvoices = saleOrderRepository.countSaleOrdersBetweenDates(startInstant, endInstant);
        report.setTotalInvoices(totalInvoices);

        // Doanh thu tổng
        Double totalRevenue = saleOrderRepository.sumTotalAmountBetweenDates(startInstant, endInstant);
        report.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // Tổng số lượng bán hàng
        Integer totalQuantitySold = saleOrderItemRepository.sumTotalQuantityBetweenDates(startInstant, endInstant);
        report.setTotalQuantitySold(totalQuantitySold != null ? totalQuantitySold : 0);

        // Doanh thu theo phương thức thanh toán
        List<Object[]> revenueByPaymentMethodData = saleOrderRepository.sumTotalAmountByPaymentMethodBetweenDates(startInstant, endInstant);
        Map<PaymentMethod, Double> revenueByPaymentMethod = new HashMap<>();
        for (Object[] data : revenueByPaymentMethodData) {
            PaymentMethod paymentMethod = (PaymentMethod) data[0];
            Double amount = (Double) data[1];
            revenueByPaymentMethod.put(paymentMethod, amount != null ? amount : 0.0);
        }
        report.setRevenueByPaymentMethod(revenueByPaymentMethod);

        return report;
    }

    // -------------------- Báo cáo nhà cung cấp --------------------

    @Override
    public SupplierReportDto getSupplierReport(LocalDate startDate, LocalDate endDate) {
        SupplierReportDto report = new SupplierReportDto();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Số lượng nhà cung cấp mới
        long newSuppliers = supplierRepository.countNewSuppliersBetweenDates(startInstant, endInstant);
        report.setNewSuppliers(newSuppliers);

        // Số lượng nhà cung cấp cũ
        long oldSuppliers = supplierRepository.countOldSuppliersBeforeDate(startInstant);
        report.setOldSuppliers(oldSuppliers);

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

    // -------------------- Báo cáo khách hàng --------------------

    @Override
    public CustomerReportDto getCustomerReport(LocalDate startDate, LocalDate endDate) {
        CustomerReportDto report = new CustomerReportDto();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Số lượng khách hàng mới
        long newCustomers = customerRepository.countNewCustomersBetweenDates(startInstant, endInstant);
        report.setNewCustomers(newCustomers);

        // Số lượng khách hàng cũ
        long oldCustomers = customerRepository.countOldCustomersBeforeDate(startInstant);
        report.setOldCustomers(oldCustomers);

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

        return report;
    }

    // -------------------- Báo cáo thu chi --------------------

    @Override
    public FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate) {
        FinancialReportDto report = new FinancialReportDto();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Tổng thu từ bán hàng
        Double totalSales = saleOrderRepository.sumTotalAmountBetweenDates(startInstant, endInstant);
        totalSales = totalSales != null ? totalSales : 0.0;

        // Tổng chi từ trả hàng lại khách hàng
        Double totalRefunds = returnOrderRepository.sumTotalRefundsBetweenDates(startInstant, endInstant);
        totalRefunds = totalRefunds != null ? totalRefunds : 0.0;

        // Tổng thu
        double totalIncome = totalSales;
        report.setTotalIncome(totalIncome);

        // Tổng chi từ nhập hàng
        Double totalImports = importRepository.sumTotalImportAmountBetweenDates(startInstant, endInstant);
        totalImports = totalImports != null ? totalImports : 0.0;

        // Tổng chi từ trả hàng lại nhà cung cấp
        Double totalExportReturns = exportSlipRepository.sumTotalExportsByTypeBetweenDates(ExportType.RETURN_TO_SUPPLIER, startInstant, endInstant);
        totalExportReturns = totalExportReturns != null ? totalExportReturns : 0.0;

        // Tổng chi
        double totalExpense = totalImports + totalRefunds + totalExportReturns;
        report.setTotalExpense(totalExpense);

        // Lợi nhuận
        double profit = totalIncome - totalExpense;
        report.setProfit(profit);

        // Chi tiết thu
        Map<String, Double> incomeBySource = new HashMap<>();
        incomeBySource.put("Bán hàng", totalSales);
        report.setIncomeBySource(incomeBySource);

        // Chi tiết chi
        Map<String, Double> expenseBySource = new HashMap<>();
        expenseBySource.put("Nhập hàng", totalImports);
        expenseBySource.put("Khách hàng trả lại", totalRefunds);
        expenseBySource.put("Trả lại nhà cung cấp", totalExportReturns);
        report.setExpenseBySource(expenseBySource);

        // (Nếu cần thêm số lượng cho các nguồn thu chi, bạn có thể thêm vào Map quantityBySource)

        return report;
    }
}

