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
import java.util.Optional;

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
        Double currentInventoryAmount = productRepository.calculateCurrentInventoryAmount();
        report.setCurrentInventoryQuantity(currentInventoryQuantity != null ? currentInventoryQuantity : 0);
        report.setCurrentInventoryAmount(currentInventoryAmount != null ? currentInventoryAmount : 0.0);


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
            startInstant = Instant.MIN;  // Mốc thời gian rất xa trong quá khứ
            endInstant = Instant.now();  // Thời gian hiện tại
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

        // Số lượng nhà cung cấp mới
        long newSuppliers = 0;
        if (startDate != null && endDate != null) {
            newSuppliers = supplierRepository.countNewSuppliersBetweenDates(startInstant, endInstant);
        }
        Double totalImportNewAmount = importRepository.sumTotalImportNewAmountBetweenDates(startInstant, endInstant);
        report.setNewSuppliers(newSuppliers);
        report.setNewSuppliersAmount(totalImportNewAmount != null ? totalImportNewAmount : 0.0);

        // Số lượng nhà cung cấp cũ
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
            startInstant = Instant.MIN;  // Mốc thời gian rất xa trong quá khứ
            endInstant = Instant.now();  // Thời gian hiện tại
        }

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

        // Chi tiết thu
        Map<String, Double> incomeBySource = new HashMap<>();
        incomeBySource.put("Bán hàng", totalSales);
        incomeBySource.put("Trả lại nhà cung cấp", totalExportReturns);

        report.setIncomeBySource(incomeBySource);

        // Chi tiết chi
        Map<String, Double> expenseBySource = new HashMap<>();
        expenseBySource.put("Nhập hàng", totalImports);
        expenseBySource.put("Khách hàng trả lại", totalRefunds);
        report.setExpenseBySource(expenseBySource);

        return report;
    }
}

