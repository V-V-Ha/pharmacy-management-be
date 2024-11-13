package com.fu.pha.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "return_order")
public class ReturnOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_id", nullable = false)
    private SaleOrder saleOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "return_date", nullable = false)
    private Instant returnDate;

    @Column(name = "refund_amount", nullable = false)
    private Double refundAmount;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ReturnOrderItem> returnOrderItems;
}
