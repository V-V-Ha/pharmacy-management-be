package com.fu.pha.dto.response.SaleOrder;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.SaleOrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaleOrderItemResponseDto {
    private Integer quantity;
    private Double unitPrice;
    private String unit;
    private Double discount;
    private Double totalAmount;
    private String batchNumber;
    private String dosage;
    private Integer conversionFactor;
    private ProductDTOResponse product;

    public SaleOrderItemResponseDto(SaleOrderItem saleOrderItem) {
        this.quantity = saleOrderItem.getQuantity();
        this.unitPrice = saleOrderItem.getUnitPrice();
        this.unit = saleOrderItem.getUnit();
        this.discount = saleOrderItem.getDiscount();
        this.totalAmount = saleOrderItem.getTotalAmount();
        this.dosage = saleOrderItem.getDosage();
        this.conversionFactor = saleOrderItem.getConversionFactor();
        if (saleOrderItem.getProduct() != null) {
            this.product = new ProductDTOResponse(saleOrderItem.getProduct());
        }
    }
}
