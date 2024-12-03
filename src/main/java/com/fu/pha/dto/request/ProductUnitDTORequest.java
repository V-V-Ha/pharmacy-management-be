package com.fu.pha.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnitDTORequest {

    private Long id;
    private Long unitId;
    private Long productId;
    private Integer conversionFactor;
    private Double importPrice;
    private Double retailPrice;
}
