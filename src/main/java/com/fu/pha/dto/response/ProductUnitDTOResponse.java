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
    private Double importPrice;
    private Double retailPrice;
    private String unitName;

    public ProductUnitDTOResponse(ProductUnit productUnit) {
        this.unit = new UnitDto(productUnit.getUnit().getId(), productUnit.getUnit().getUnitName());
        this.productId = productUnit.getProduct().getId();
        this.conversionFactor = productUnit.getConversionFactor();
        this.importPrice = productUnit.getImportPrice();
        this.retailPrice = productUnit.getRetailPrice();
        this.unitName = productUnit.getUnit().getUnitName();
    }
}
