package com.fu.pha.dto.response;

import com.fu.pha.entity.ProductUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnitDTOResponse {
    private Long id;
    private Long unitId;
    private Long productId;
    private Integer conversionFactor;
    private Double retailPrice;
    private String unitName;

    public ProductUnitDTOResponse(ProductUnit productUnit) {
        this.unitId = productUnit.getUnitId().getId();
        this.productId = productUnit.getProductId().getId();
        this.conversionFactor = productUnit.getConversionFactor();
        this.retailPrice = productUnit.getRetailPrice();
        this.unitName = productUnit.getUnitId().getUnitName();
    }
}
