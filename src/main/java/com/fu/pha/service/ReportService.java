
package com.fu.pha.service;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.customer.CustomerInvoiceDto;
import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.InventoryProductReportDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.dto.response.report.product.ProductSalesDto;
import com.fu.pha.dto.response.report.sale.SalesTransactionDto;
import com.fu.pha.dto.response.report.supplier.SupplierInvoiceDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    // Báo cáo kho
    InventoryReportDto getInventoryReport(LocalDate startDate, LocalDate endDate, Integer month, Integer year);

    Page<InventoryProductReportDto> getInventoryReportByProduct(
            LocalDate startDate, LocalDate endDate, Integer month, Integer year,
            String productCode,String productName, Long categoryId,
            int pageNumber, int pageSize
    );

    Page<OutOfStockProductDto> getOutOfStockProducts(
            Long categoryId,
            String searchText,
            int pageNumber,
            int pageSize
    );

    Page<ExpiredProductDto> getExpiredProducts(
            Long categoryId,
            String searchText,
            int warningDays,
            int pageNumber,
            int pageSize
    );

    // Báo cáo bán hàng
    SalesReportDto getSalesReport(LocalDate startDate, LocalDate endDate);

    Page<SalesTransactionDto> getSalesTransactions(
            String paymentMethod,
            String voucherType,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize
    );

    Page<ProductSalesDto> getProductSales(
            String productName,
            String productCode,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize
    );

    // Báo cáo nhà cung cấp
    SupplierReportDto getSupplierReport(LocalDate startDate, LocalDate endDate);

    Page<SupplierInvoiceDto> getSupplierInvoiceList(
            String name,
            Boolean isNewSupplier,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size);

    void exportCustomerReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException;
    void exportSupplierReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException;
    void exportFinancialReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException;
    void exportSalesReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException;
    void exportInventoryReportToExcel(HttpServletResponse response, LocalDate fromDate, LocalDate toDate) throws IOException;

    // Báo cáo khách hàng
    CustomerReportDto getCustomerReport(LocalDate startDate, LocalDate endDate);
    Page<CustomerInvoiceDto> getCustomerInvoiceList(
            String searchTerm,
            Boolean isNewCustomer,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size);

    // Báo cáo thu chi
    FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate);

    Page<FinancialTransactionDto> getFinancialTransactions(
            String paymentMethod,
            String category,
            String receiptType,
            LocalDate startDate,
            LocalDate endDate,
            int pageNumber,
            int pageSize);
}