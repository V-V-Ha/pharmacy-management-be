package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product")
@Where(clause = "deleted = false")
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

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "import_price")
    private Double importPrice;

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

    @Column(name = "prescription_drug")
    private Boolean prescriptionDrug;

    @Column(name = "image_product", columnDefinition = "TEXT")
    private String imageProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private Category categoryId;

    @OneToMany(mappedBy = "productId",cascade = CascadeType.ALL,orphanRemoval = true)
    @JsonManagedReference
    private List<ProductUnit> productUnitList;

    @OneToMany(mappedBy = "product")
    private List<ExportSlipItem> exportSlipItems;

    @OneToMany(mappedBy = "product")
    @JsonManagedReference
    private List<ImportItem> importItems;

    @OneToMany(mappedBy = "product")
    private List<SaleOrderItem> saleOrderItemList;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}
