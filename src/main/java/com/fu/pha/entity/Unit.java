package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "unit")

public class Unit extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "unitId",cascade = CascadeType.ALL ,orphanRemoval = true)
    @JsonManagedReference
    private List<ProductUnit> productUnitList;
}
