package com.fu.pha.dto.response.report;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpeningStockDTO {
    private Long openingQuantity;
    private Double openingValue;

    public OpeningStockDTO(Long openingQuantity, Double openingValue) {
        this.openingQuantity = openingQuantity;
        this.openingValue = openingValue;
    }
}