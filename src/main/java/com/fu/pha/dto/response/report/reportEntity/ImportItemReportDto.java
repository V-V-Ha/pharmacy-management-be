package com.fu.pha.dto.response.report.reportEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportItemReportDto {

    private Long id;
    private Long productId;
    private String productName;
    private int remainingQuantity;
    private double remainingAmount;
    private Instant expiryDate;

    public ImportItemReportDto(Long id, Long productId, String productName, Integer remainingQuantity, Double remainingAmount, Instant expiryDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.remainingQuantity = remainingQuantity != null ? remainingQuantity : 0;
        this.remainingAmount = remainingAmount != null ? remainingAmount : 0.0;
        this.expiryDate = expiryDate;
    }
}
