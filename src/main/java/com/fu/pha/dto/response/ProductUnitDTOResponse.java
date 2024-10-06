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
    private String unitName;
    private Integer conversionFactor;
    private Double retailPrice;

    public ProductUnitDTOResponse(ProductUnit productUnit) {
        this.unitName = productUnit.getUnitId().getUnitName();
        this.conversionFactor = productUnit.getConversionFactor();
    }
}
