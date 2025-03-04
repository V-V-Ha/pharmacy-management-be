package com.fu.pha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.DoubleBuffer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaleOrderItemBatchResponseDto {
    private String batchNumber;
    private Integer quantity;
    private Integer returnedQuantity;
    private String invoiceNumber;
    private Double discount;
}
