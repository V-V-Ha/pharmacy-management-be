package com.fu.pha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductUnitDTOReponse {
    private String unitName;
    private Integer conversionFactor;
    private Double retailPrice;
}
