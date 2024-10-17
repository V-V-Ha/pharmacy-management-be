package com.fu.pha.dto.response;

import com.fu.pha.dto.request.UnitDto;
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
    private UnitDto unit;
    private Long productId;
    private Integer conversionFactor;
    private Double retailPrice;

    public ProductUnitDTOResponse(ProductUnit productUnit) {
        this.unit = new UnitDto(productUnit.getUnitId().getId(), productUnit.getUnitId().getUnitName());
        this.productId = productUnit.getProductId().getId();
        this.conversionFactor = productUnit.getConversionFactor();
        this.retailPrice = productUnit.getRetailPrice();
    }
}
