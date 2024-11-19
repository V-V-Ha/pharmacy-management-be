package com.fu.pha.dto.request;

import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import com.fu.pha.validate.anotation.ValidFullName;
import com.fu.pha.validate.anotation.ValidPhoneNumber;
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

    @ValidPhoneNumber
    private String phoneNumber;

    private Integer yob;
    private Gender gender;
    private Status status;
}
