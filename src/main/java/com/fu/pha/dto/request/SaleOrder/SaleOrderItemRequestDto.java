package com.fu.pha.dto.request.SaleOrder;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SaleOrderItemRequestDto {

    private Long productId;
    private Integer quantity;
    private Double unitPrice;
    private String unit;
    private Double discount;
    private Double totalAmount;
    private String batchNumber;
    private String dosage;
    private Integer conversionFactor;
}
