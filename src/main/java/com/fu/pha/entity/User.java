package com.fu.pha.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fu.pha.dto.request.LoginDtoRequest;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone")
        })
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username")
    private String username;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(  name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "address")
    private String address;

    @Column(name = "dob")
    private Instant dob;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar", columnDefinition = "TEXT")
    private String avatar;

    @Column(name = "phone")
    private String phone;

    @Column(name = "cic")
    private String cic;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name ="note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<ExportSlip> exportSlipList;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Import> importList;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<SaleOrder> saleOrderList;

    public User(LoginDtoRequest request){
        this.username = request.getUsername();
        this.password = request.getPassword();
    }
}
