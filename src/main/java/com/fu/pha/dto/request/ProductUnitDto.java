package com.fu.pha.dto.request;

import com.fu.pha.entity.Product;
import com.fu.pha.entity.Unit;
import lombok.Data;

@Data
public class ProductUnitDto {

    private Long id;

    private Product productId;

    private Unit unitId;

    private Integer conversionFactor;
    private Double retailPrice;
}
