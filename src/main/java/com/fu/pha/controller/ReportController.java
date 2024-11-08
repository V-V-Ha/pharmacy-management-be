package com.fu.pha.controller;

import com.fu.pha.dto.response.report.StockReportDto;
import com.fu.pha.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/get-full-stock-report")
    public ResponseEntity<StockReportDto> getFullStockReport(
            @RequestParam(required = false, name = "fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,

            @RequestParam(required = false, name = "toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @RequestParam Integer minimumStockThreshold) {

        if (fromDate == null) {
            fromDate = LocalDate.now().minusMonths(1); // mặc định từ 1 tháng trước
        }
        if (toDate == null) {
            toDate = LocalDate.now(); // mặc định là ngày hiện tại
        }
        Instant startOfPeriod = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfPeriod = toDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        // Gọi service với thời gian đã chuyển đổi
        StockReportDto report = reportService.getFullStockReport(startOfPeriod, endOfPeriod, minimumStockThreshold);

        return ResponseEntity.ok(report);
    }

}
