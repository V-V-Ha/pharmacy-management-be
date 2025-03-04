package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class Customer extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "yob")
    private Integer yob;

    @OneToMany(mappedBy = "customer")
    @JsonManagedReference
    private List<SaleOrder> saleOrderList;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
}
