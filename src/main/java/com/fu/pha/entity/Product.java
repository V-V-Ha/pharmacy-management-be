package com.fu.pha.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product")
public class Product extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "active_ingredient")
    private String activeIngredient;

    @Column(name = "dosage_concentration")
    private String dosageConcentration;

    @Column(name = "packing_method")
    private String packingMethod;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "country_of_origin")
    private String countryOfOrigin;

    @Column(name = "unit")
    private String unit;

    @Column(name = "import_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal importPrice;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "indication")
    private String indication;

    @Column(name = "contraindication")
    private String contraindication;

    @Column(name = "side_effect")
    private String sideEffect;

    @Column(name = "dosage_forms")
    private String dosageForms;

    @Column(name = "description")
    private String description;

    @Column(name = "image_product", columnDefinition = "TEXT")
    private String imageProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category categoryId;

    @OneToMany(mappedBy = "productId")
    private List<ProductUnit> productUnitList;

    @OneToMany(mappedBy = "productId")
    private List<RetailPrice> retailPrices;

    @OneToMany(mappedBy = "productId")
    private List<ExportSlipItem> exportSlipItems;

    @OneToMany(mappedBy = "productId")
    private List<ImportItem> importItems;

    @OneToMany(mappedBy = "productId")
    private List<SaleOrderItem> saleOrderItemList;
}
