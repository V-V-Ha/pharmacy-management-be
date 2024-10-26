package com.fu.pha.dto.response;

import com.fu.pha.entity.Doctor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DoctorDTOResponse {

    private Long id;
    private String fullName;
    private String address;
    private String phoneNumber;
    private String note;

    public DoctorDTOResponse(Doctor doctor) {
        this.id = doctor.getId();
        this.fullName = doctor.getFullName();
        this.address = doctor.getAddress();
        this.phoneNumber = doctor.getPhoneNumber();
        this.note = doctor.getNote();
    }
}
