package com.fu.pha.service;

import com.fu.pha.dto.response.report.StockReportDto;

import java.time.Instant;

public interface ReportService {
    StockReportDto getFullStockReport(Instant startOfPeriod, Instant endOfPeriod, Integer minimumStockThreshold);
}
