
package com.fu.pha.controller;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.customer.CustomerInvoiceDto;
import com.fu.pha.dto.response.report.product.ExpiredProductDto;
import com.fu.pha.dto.response.report.product.InventoryProductReportDto;
import com.fu.pha.dto.response.report.product.OutOfStockProductDto;
import com.fu.pha.dto.response.report.product.ProductSalesDto;
import com.fu.pha.dto.response.report.sale.SalesTransactionDto;
import com.fu.pha.dto.response.report.supplier.SupplierInvoiceDto;
import com.fu.pha.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Báo cáo kho
    @GetMapping("/inventory-report")
    public ResponseEntity<InventoryReportDto> getInventoryReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        InventoryReportDto report = reportService.getInventoryReport(startDate, endDate, month, year);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/inventory-report-product")
    public ResponseEntity<Page<InventoryProductReportDto>> getInventoryReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        Page<InventoryProductReportDto> reportPage = reportService.getInventoryReportByProduct(startDate, endDate, month, year, productCode,
                productName, categoryId, pageNumber, pageSize);
        return ResponseEntity.ok(reportPage);
    }

    @GetMapping("/out-of-stock")
    public Page<OutOfStockProductDto> getOutOfStockProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return reportService.getOutOfStockProducts(
                categoryId,
                searchText,
                pageNumber,
                pageSize
        );
    }
    @GetMapping("/expired")
    public Page<ExpiredProductDto> getExpiredProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String searchText,
            @RequestParam(defaultValue = "30") int warningDays, // Default warning days
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return reportService.getExpiredProducts(
                categoryId,
                searchText,
                warningDays,
                pageNumber,
                pageSize
        );
    }

    @GetMapping("/export-inventory-report")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public void exportInventoryReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Set response type and headers for Excel download
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bao_cao_kho.xlsx";
        response.setHeader(headerKey, headerValue);

        // Call the service to generate and write the Excel file
        reportService.exportInventoryReportToExcel(response, fromDate, toDate);
    }

    // Báo cáo bán hàng
    @GetMapping("/sales")
    public ResponseEntity<SalesReportDto> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SalesReportDto report = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales-list")
    public Page<SalesTransactionDto> getSalesTransactions(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return reportService.getSalesTransactions(
                paymentMethod, voucherType, startDate, endDate, pageNumber, pageSize);
    }

    @GetMapping("/sales-product")
    public Page<ProductSalesDto> getProductSales(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return reportService.getProductSales(
                productName, productCode, startDate, endDate, pageNumber, pageSize);
    }

    @GetMapping("/export-excel-sales-report")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public void exportSalesReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Set response type and headers for Excel download
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bao_cao_ban_hang.xlsx";
        response.setHeader(headerKey, headerValue);

        // Call the service to generate and write the Excel file
        reportService.exportSalesReportToExcel(response, fromDate, toDate);
    }

    // Báo cáo nhà cung cấp
    @GetMapping("/suppliers")
    public ResponseEntity<SupplierReportDto> getSupplierReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SupplierReportDto report = reportService.getSupplierReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/list-suppliers")
    public ResponseEntity<Page<SupplierInvoiceDto>> getSupplierInvoices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean isNewSupplier,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SupplierInvoiceDto> result = reportService.getSupplierInvoiceList(
                name,
                isNewSupplier,
                startDate,
                endDate,
                page,
                size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/export-excel-supplier-report")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public void exportSupplierReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Set response type and headers for Excel download
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bao_cao_nha_cung_cap.xlsx";
        response.setHeader(headerKey, headerValue);

        // Call the service to generate and write the Excel file
        reportService.exportSupplierReportToExcel(response, fromDate, toDate);
    }

    // Báo cáo khách hàng
    @GetMapping("/customers")
    public ResponseEntity<CustomerReportDto> getCustomerReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        CustomerReportDto report = reportService.getCustomerReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/list-customers")
    public ResponseEntity<Page<CustomerInvoiceDto>> getCustomerInvoices(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean isNewCustomer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CustomerInvoiceDto> result = reportService.getCustomerInvoiceList(
                searchTerm,
                isNewCustomer,
                startDate,
                endDate,
                page,
                size);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/export-excel-customer-report")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public void exportCustomerReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Set response type and headers for Excel download
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bao_cao_khach_hang.xlsx";
        response.setHeader(headerKey, headerValue);

        // Call the service to generate and write the Excel file
        reportService.exportCustomerReportToExcel(response, fromDate, toDate);
    }


    // Báo cáo thu chi
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDto> getFinancialReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        FinancialReportDto report = reportService.getFinancialReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/financial-transactions")
    public Page<FinancialTransactionDto> getFinancialTransactions(
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String receiptType,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {

        return reportService.getFinancialTransactions(
                paymentMethod, category, receiptType, startDate, endDate, pageNumber, pageSize);
    }

    @GetMapping("/export-excel-financial-report")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public void exportFinancialReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            HttpServletResponse response) throws IOException {

        // Set response type and headers for Excel download
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Bao_cao_thu_chi.xlsx";
        response.setHeader(headerKey, headerValue);

        // Call the service to generate and write the Excel file
        reportService.exportFinancialReportToExcel(response, fromDate, toDate);
    }
}
