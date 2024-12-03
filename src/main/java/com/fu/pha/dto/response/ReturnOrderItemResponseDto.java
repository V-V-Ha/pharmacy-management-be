package com.fu.pha.dto.response;


import com.fu.pha.entity.ReturnOrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderItemResponseDto {
    private ProductDTOResponse product;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalAmount;
    private String unit;
    private Integer conversionFactor;
    private List<SaleOrderItemBatchResponseDto> batchResponseDtos;

    public ReturnOrderItemResponseDto (ReturnOrderItem returnOrderItem) {
        this.product = returnOrderItem.getProduct() != null ? new ProductDTOResponse(returnOrderItem.getProduct()) : null;
        this.productName = returnOrderItem.getProduct().getProductName();
        this.quantity = returnOrderItem.getQuantity();
        this.unitPrice = returnOrderItem.getUnitPrice();
        this.totalAmount = returnOrderItem.getTotalAmount();
        this.unit = returnOrderItem.getUnit();
        this.conversionFactor = returnOrderItem.getConversionFactor();
        this.batchResponseDtos = null;
    }
}
