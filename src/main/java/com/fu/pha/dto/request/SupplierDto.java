package com.fu.pha.dto.request;

import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierDto {
    private Long id;
    @NotBlank(message = Message.NULL_FILED)
    private String supplierName;

    private String address;

    @NotBlank(message = Message.NULL_FILED)
    @Pattern(regexp = Constants.REGEX_PHONE, message = Message.INVALID_PHONE)
    private String phoneNumber;

    @Pattern(regexp = Constants.REGEX_GMAIL, message = Message.INVALID_GMAIL)
    private String email;

    @NotBlank(message = Message.NULL_FILED)
    @Pattern(regexp = Constants.REGEX_TAX, message = Message.INVALID_TAX)
    private String tax;
}
