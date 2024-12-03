package com.fu.pha.dto.response;

import com.fu.pha.entity.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupplierCResponseDto {
    private Long id;
    private String supplierName;
    private String address;
    private String phoneNumber;
    private String email;
    private String tax;
    private String status;

    public SupplierCResponseDto(Supplier supplier) {
        this.id = supplier.getId();
        this.supplierName = supplier.getSupplierName();
        this.address = supplier.getAddress();
        this.phoneNumber = supplier.getPhoneNumber();
        this.email = supplier.getEmail();
        this.tax = supplier.getTax();
        this.status = supplier.getStatus().name();
    }


}
