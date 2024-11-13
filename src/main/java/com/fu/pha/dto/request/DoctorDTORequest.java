package com.fu.pha.dto.request;


import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
import com.fu.pha.validate.anotation.ValidFullName;
import com.fu.pha.validate.anotation.ValidPhoneNumber;
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
public class DoctorDTORequest {

    private Long id;

    @ValidFullName
    private String fullName;

    @NotBlank(message = Message.NULL_FILED)
    private String address;

    @ValidPhoneNumber
    private String phoneNumber;
    private String note;
}
