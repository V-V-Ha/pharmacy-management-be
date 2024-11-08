package com.fu.pha.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReportDto {
    private OpeningStockDTO openingStock;
    private ImportStockDTO importStock;
    private Integer expiredStock;
    private Integer nearExpiryStock;
    private Integer outOfStock;
    private Integer lowStock;
    private CurrentStockDTO currentStock;
}
