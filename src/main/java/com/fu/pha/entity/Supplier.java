package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Where(clause = "deleted = false")
public class Supplier extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "tax")
    private String tax;

    @OneToMany(mappedBy = "supplier")
    @JsonManagedReference
    private List<Import> importList;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

}
