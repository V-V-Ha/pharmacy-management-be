
package com.fu.pha.service;

import com.fu.pha.dto.response.report.*;
import com.fu.pha.dto.response.report.product.InventoryProductReportDto;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    // Báo cáo kho
    InventoryReportDto getInventoryReport(LocalDate startDate, LocalDate endDate, Integer month, Integer year);

    List<InventoryProductReportDto> getInventoryReportByProduct(LocalDate startDate, LocalDate endDate, Integer month, Integer year);

    // Báo cáo bán hàng
    SalesReportDto getSalesReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo nhà cung cấp
    SupplierReportDto getSupplierReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo khách hàng
    CustomerReportDto getCustomerReport(LocalDate startDate, LocalDate endDate);

    // Báo cáo thu chi
    FinancialReportDto getFinancialReport(LocalDate startDate, LocalDate endDate);
}