package com.fu.pha.dto.response.exportSlip;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BatchInfo {
    private String batchNumber;
    private Integer remainingQuantity;

    public BatchInfo(String batchNumber, Integer remainingQuantity) {
        this.batchNumber = batchNumber;
        this.remainingQuantity = remainingQuantity;
    }
}
