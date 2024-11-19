package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fu.pha.enums.Status;
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
@Table(name = "category")
public class Category extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "categoryId")
    @JsonManagedReference
    private List<Product> productList;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

}
