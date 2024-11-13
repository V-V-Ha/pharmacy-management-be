
package com.fu.pha.service;

import com.fu.pha.dto.response.report.*;

import java.time.LocalDate;

public interface ReportService {
    // Báo cáo kho
    InventoryReportDto getInventoryReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo bán hàng
    SalesReportDto getSalesReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo nhà cung cấp
    SupplierReportDto getSupplierReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo khách hàng
    CustomerReportDto getCustomerReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo thu chi
    FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate);
}