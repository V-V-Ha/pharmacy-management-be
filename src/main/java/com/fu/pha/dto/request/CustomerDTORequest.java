package com.fu.pha.dto.request;

import com.fu.pha.enums.Gender;
import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
import com.fu.pha.validate.anotation.ValidFullName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTORequest {

    private Long id;

    @ValidFullName
    private String customerName;

    private String address;

    @Pattern(regexp = Constants.REGEX_PHONE, message = Message.INVALID_PHONE)
    private String phoneNumber;
    private Integer yob;
    private Gender gender;
}
