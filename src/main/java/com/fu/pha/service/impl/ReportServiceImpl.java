package com.fu.pha.service.impl;

import com.fu.pha.dto.response.report.CurrentStockDTO;
import com.fu.pha.dto.response.report.ImportStockDTO;
import com.fu.pha.dto.response.report.OpeningStockDTO;
import com.fu.pha.dto.response.report.StockReportDto;
import com.fu.pha.repository.ImportItemRepository;
import com.fu.pha.repository.ImportRepository;
import com.fu.pha.repository.ProductRepository;
import com.fu.pha.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    ImportRepository importRepository;

    @Autowired
    ImportItemRepository importItemRepository;

    @Autowired
    ProductRepository productRepository;
    @Override
    public StockReportDto getFullStockReport(Instant startOfPeriod, Instant endOfPeriod, Integer minimumStockThreshold) {
        Optional<OpeningStockDTO> openingStock = importItemRepository.findOpeningStock(startOfPeriod);
        Optional<ImportStockDTO> importStock = importItemRepository.findImportStock(startOfPeriod, endOfPeriod);
        Integer expiredStock = importItemRepository.findExpiredStock();
        Integer nearExpiryStock = importItemRepository.findNearExpiryStock();
        Integer outOfStock = productRepository.findOutOfStock();
        Integer lowStock = productRepository.findLowStock(minimumStockThreshold);
        Optional<CurrentStockDTO> currentStock = productRepository.findCurrentStock();

        return StockReportDto.builder()
                .openingStock(openingStock.orElse(new OpeningStockDTO(0L, 0.0)))
                .importStock(importStock.orElse(new ImportStockDTO(0L, 0.0)))
                .expiredStock(expiredStock)
                .nearExpiryStock(nearExpiryStock)
                .outOfStock(outOfStock)
                .lowStock(lowStock)
                .currentStock(currentStock.orElse(new CurrentStockDTO(0L, 0.0)))
                .build();
    }






}
