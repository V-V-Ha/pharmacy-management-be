package com.fu.pha.dto.request;

import com.fu.pha.entity.Category;
import com.fu.pha.entity.Product;
import com.fu.pha.entity.ProductUnit;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
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

    public ProductDTORequest(Product product){
        this.id = product.getId();
        this.productName = product.getProductName();
        this.registrationNumber = product.getRegistrationNumber();
        this.activeIngredient = product.getActiveIngredient();
        this.dosageConcentration = product.getDosageConcentration();
        this.packingMethod = product.getPackingMethod();
        this.manufacturer = product.getManufacturer();
        this.countryOfOrigin = product.getCountryOfOrigin();
        this.indication = product.getIndication();
        this.contraindication = product.getContraindication();
        this.sideEffect = product.getSideEffect();
        this.dosageForms = product.getDosageForms();
        this.description = product.getDescription();
        this.categoryId = product.getCategoryId().getId();
        this.imageProduct =product.getImageProduct() != null ? product.getImageProduct() : "";
        this.prescriptionDrug = product.getPrescriptionDrug();
        this.createBy = product.getCreateBy();
        this.lastModifiedBy = product.getLastModifiedBy();
        this.createDate = product.getCreateDate();
        this.lastModifiedDate = product.getLastModifiedDate();

    }
}
