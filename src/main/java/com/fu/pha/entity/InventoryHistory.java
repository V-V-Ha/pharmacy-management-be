package com.fu.pha.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "inventory_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "import_item_id")
    private ImportItem importItem;

    @Column(name = "record_date")
    private Instant recordDate;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

    @Column(name = "change_quantity")
    private Integer changeQuantity;

    @Column(name = "reason")
    private String reason;
}
