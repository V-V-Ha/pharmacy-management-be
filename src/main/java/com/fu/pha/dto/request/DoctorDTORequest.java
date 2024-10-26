package com.fu.pha.dto.request;


import com.fu.pha.exception.Message;
import com.fu.pha.validate.Constants;
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

    @NotBlank(message = Message.NULL_FILED)
    @Pattern(regexp = Constants.REGEX_NAME, message = Message.INVALID_NAME)
    private String fullName;

    @NotBlank(message = Message.NULL_FILED)
    private String address;

    @NotBlank(message = Message.NULL_FILED)
    @Pattern(regexp = Constants.REGEX_PHONE, message = Message.INVALID_PHONE)
    private String phoneNumber;
    private String note;
}
