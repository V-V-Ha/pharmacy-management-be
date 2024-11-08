package com.fu.pha.dto.response.report;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImportStockDTO {
    private Long importQuantity;
    private Double importValue;

    public ImportStockDTO(Long importQuantity, Double importValue) {
        this.importQuantity = importQuantity;
        this.importValue = importValue;
    }
}
