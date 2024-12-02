package com.fu.pha.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sale_order_item_batch")
public class SaleOrderItemBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_order_item_id", nullable = false)
    private SaleOrderItem saleOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_item_id", nullable = false)
    private ImportItem importItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_order_item_id")
    private ReturnOrderItem returnOrderItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "returned_quantity")
    private Integer returnedQuantity;
}
