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
    private String productCode;
    private String indication;
    private String contraindication;
    private String sideEffect;
    private String dosageForms;
    private String description;
    private String categoryId;
    private String imageProduct;
    private Boolean prescriptionDrug;
    private List<ProductUnitDTORequest> productUnitList;

    public ProductDTORequest(Product product){
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
        this.categoryId = product.getCategoryId().getCategoryName();
        this.imageProduct =product.getImageProduct() != null ? product.getImageProduct() : "";
        this.prescriptionDrug = product.getPrescriptionDrug();
    }
}
