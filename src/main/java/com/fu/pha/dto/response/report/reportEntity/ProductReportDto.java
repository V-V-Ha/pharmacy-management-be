package com.fu.pha.dto.response.report.reportEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductReportDto {

    private Long id;
    private String productName;
    private String productCode;
    private int totalQuantity;
    private double totalAmount;

    public ProductReportDto(Long id, String productName, String productCode, Integer totalQuantity, Double totalAmount) {
        this.id = id;
        this.productName = productName;
        this.productCode = productCode;
        this.totalQuantity = totalQuantity != null ? totalQuantity : 0;
        this.totalAmount = totalAmount != null ? totalAmount : 0.0;
    }

}
