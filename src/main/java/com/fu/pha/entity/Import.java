package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fu.pha.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "import")
public class Import extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "import_date")
    private Instant importDate;

    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "tax_dentification_number")
    private String taxiDentificationNumber;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "importR")
    @JsonManagedReference
    private List<ImportItem> importItemList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
}

