package com.fu.pha.dto.request;

import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.util.OptionalEmail;
import com.fu.pha.validate.anotation.ValidFullName;
import com.fu.pha.validate.anotation.ValidPhoneNumber;
import com.fu.pha.validate.anotation.ValidTax;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private Long id;

    @ValidFullName
    private String supplierName;

    private String address;

    @ValidPhoneNumber
    private String phoneNumber;

    @OptionalEmail
    private String email;

    @ValidTax
    private String tax;

    private Status status;

    private Double totalAmount;

    public SupplierDto(Long id, String supplierName, String address, String phoneNumber, String email, String tax, Status status) {
        this.id = id;
        this.supplierName = supplierName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.tax = tax;
        this.status = status;
    }
}
