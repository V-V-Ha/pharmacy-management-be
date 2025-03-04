package com.fu.pha.dto.response;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.entity.Product;
import com.fu.pha.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTOResponse {

    private Long id;
    private String productName;
    private String registrationNumber;
    private String activeIngredient;
    private String dosageConcentration;
    private String packingMethod;
    private String manufacturer;
    private String countryOfOrigin;
    private String unit;
    private String productCode;
    private String indication;
    private String contraindication;
    private String sideEffect;
    private String dosageForms;
    private String description;
    private String category;
    private Long categoryId;
    private String categoryName;
    private String imageProduct;
    private Boolean prescriptionDrug;
    private List<ProductUnitDTOResponse> productUnitList;
    private Instant createDate;
    private Instant lastModifiedDate;
    private String createBy;
    private String lastModifiedBy;
    private Integer totalQuantity;
    private Status status;
    private Integer numberWarning;

    public ProductDTOResponse(Product product){
        this.id = product.getId();
        this.productName = product.getProductName();
        this.registrationNumber = product.getRegistrationNumber();
        this.activeIngredient = product.getActiveIngredient();
        this.dosageConcentration = product.getDosageConcentration();
        this.packingMethod = product.getPackingMethod();
        this.manufacturer = product.getManufacturer();
        this.countryOfOrigin = product.getCountryOfOrigin();
        this.productCode = product.getProductCode();
        this.indication = product.getIndication();
        this.contraindication = product.getContraindication();
        this.sideEffect = product.getSideEffect();
        this.dosageForms = product.getDosageForms();
        this.description = product.getDescription();
        this.category = product.getCategoryId().getCategoryName();
        this.categoryId = product.getCategoryId().getId();
        this.imageProduct =product.getImageProduct() != null ? product.getImageProduct() : "";
        this.prescriptionDrug = product.getPrescriptionDrug();
        this.productUnitList = product.getProductUnitList().stream()
                .map(productUnit -> new ProductUnitDTOResponse(
                        productUnit.getId(),
                        new UnitDto(productUnit.getUnit().getId(), productUnit.getUnit().getUnitName()),
                        productUnit.getProduct().getId(),
                        productUnit.getConversionFactor(),
                        productUnit.getImportPrice(),
                        productUnit.getRetailPrice(),
                        productUnit.getUnit().getUnitName()
                ))
                .collect(Collectors.toList());
        this.createBy = product.getCreateBy();
        this.lastModifiedBy = product.getLastModifiedBy();
        this.createDate = product.getCreateDate();
        this.lastModifiedDate = product.getLastModifiedDate();
        this.totalQuantity = product.getTotalQuantity();
        this.categoryName = product.getCategoryId().getCategoryName();
        this.status = product.getStatus();
        this.numberWarning = product.getNumberWarning();
    }
}
