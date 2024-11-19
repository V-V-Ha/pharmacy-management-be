
package com.fu.pha.controller;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.product.InventoryProductReportDto;
import com.fu.pha.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/report-by-product")
    public ResponseEntity<List<InventoryProductReportDto>> getInventoryReportByProduct(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        List<InventoryProductReportDto> report = reportService.getInventoryReportByProduct(startDate, endDate, month, year);
        return ResponseEntity.ok(report);
    }


    // Báo cáo bán hàng
    @GetMapping("/sales")
    public ResponseEntity<SalesReportDto> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SalesReportDto report = reportService.getSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // Báo cáo nhà cung cấp
    @GetMapping("/suppliers")
    public ResponseEntity<SupplierReportDto> getSupplierReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SupplierReportDto report = reportService.getSupplierReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // Báo cáo khách hàng
    @GetMapping("/customers")
    public ResponseEntity<CustomerReportDto> getCustomerReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        CustomerReportDto report = reportService.getCustomerReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    // Báo cáo thu chi
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportDto> getFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        FinancialReportDto report = reportService.getFinancialReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
