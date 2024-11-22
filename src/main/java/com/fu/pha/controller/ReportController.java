
package com.fu.pha.controller;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.customer.CustomerInvoiceDto;
import com.fu.pha.dto.response.report.product.InventoryProductReportDto;
import com.fu.pha.dto.response.report.sale.SalesTransactionDto;
import com.fu.pha.dto.response.report.supplier.SupplierInvoiceDto;
import com.fu.pha.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // Báo cáo nhà cung cấp
    @GetMapping("/suppliers")
    public ResponseEntity<SupplierReportDto> getSupplierReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

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

    // Báo cáo khách hàng
    @GetMapping("/customers")
    public ResponseEntity<CustomerReportDto> getCustomerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        CustomerReportDto report = reportService.getCustomerReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/list-customers")
    public ResponseEntity<Page<CustomerInvoiceDto>> getCustomerInvoices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Boolean isNewCustomer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CustomerInvoiceDto> result = reportService.getCustomerInvoiceList(
                name,
                phone,
                isNewCustomer,
                startDate,
                endDate,
                page,
                size);

        return ResponseEntity.ok(result);
    }

    // Báo cáo thu chi
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDto> getFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        FinancialReportDto report = reportService.getFinancialReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/transactions")
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
}
