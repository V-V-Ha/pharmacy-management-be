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
@Table(name = "prescription")
public class Prescription extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_code")
    private String prescriptionCode;

    @Column(name = "prescription_date")
    private Instant prescriptionDate;

    @Column(name = "diagnosis")
    private String diagnosis;

    @Column(name = "order_id")
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctorId;

    @OneToMany(mappedBy = "prescriptionId")
    private List<PrescriptionItem> prescriptionItemList;

    @OneToOne(mappedBy = "prescriptionId")
    private SaleOrder saleOrder;
}
