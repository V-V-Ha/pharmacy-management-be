package com.fu.pha.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "export_slip_item")
public class ExportSlipItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "unit")
    private String unit;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "expiration_date")
    private Instant expirationDate;

    @Column(name = "production_date")
    private Instant productionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "export_slip_id", nullable = false)
    private ExportSlip exportSlip;

    @Column(name = "total_amount")
    private Double totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_item_id", nullable = false)
    private ImportItem importItem;
}
