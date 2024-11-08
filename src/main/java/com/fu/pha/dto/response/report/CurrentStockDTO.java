package com.fu.pha.dto.response.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CurrentStockDTO {
    private Long currentQuantity;
    private Double currentValue;

    public CurrentStockDTO(Long currentQuantity, Double currentValue) {
        this.currentQuantity = currentQuantity;
        this.currentValue = currentValue;
    }
}