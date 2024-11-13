package com.fu.pha.controller;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Báo cáo kho
    @GetMapping("/inventory")
    public ResponseEntity<InventoryReportDto> getInventoryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        InventoryReportDto report = reportService.getInventoryReport(startDate, endDate);
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