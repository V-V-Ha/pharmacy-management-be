package com.fu.pha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaleOrderItemForReturnDto {
    private Integer quantity;
    private Double unitPrice;
    private String unit;
    private Double totalAmount;
    private Double discount;
    private Integer conversionFactor;
    private ProductDTOResponse product;
    private List<ImportItemBatchDto> importItemBatchDtos;
}
