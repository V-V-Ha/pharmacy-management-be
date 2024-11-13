package com.fu.pha.dto.request;

import com.fu.pha.exception.Message;
import com.fu.pha.util.OptionalEmail;
import com.fu.pha.validate.Constants;
import com.fu.pha.validate.anotation.ValidPhoneNumber;
import com.fu.pha.validate.anotation.ValidTax;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private Long id;
    @NotBlank(message = Message.NULL_FILED)
    private String supplierName;

    private String address;

    @ValidPhoneNumber
    private String phoneNumber;

    @OptionalEmail
    private String email;

    @ValidTax
    private String tax;

    private Double totalAmount;

    public SupplierDto(Long id, String supplierName, String address, String phoneNumber, String email, String tax) {
        this.id = id;
        this.supplierName = supplierName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.tax = tax;
    }
}
