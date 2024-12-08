package com.fu.pha.dto.request;

import com.fu.pha.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTORequest {

    private Long id;
    private String productName;
    private String registrationNumber;
    private String activeIngredient;
    private String dosageConcentration;
    private String packingMethod;
    private String manufacturer;
    private String countryOfOrigin;
    private String indication;
    private String contraindication;
    private String sideEffect;
    private String dosageForms;
    private String description;
    private Long categoryId;
    private String imageProduct;
    private Boolean prescriptionDrug;
    private List<ProductUnitDTORequest> productUnitListDTO;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;
    private Status status;
    private Integer numberWarning;

}
