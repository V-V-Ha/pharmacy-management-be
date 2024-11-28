package com.fu.pha.dto.response;

import com.fu.pha.entity.Doctor;
import com.fu.pha.enums.Status;
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
    private Status status;

    public DoctorDTOResponse(Doctor doctor) {
        this.id = doctor.getId();
        this.fullName = doctor.getFullName();
        this.address = doctor.getAddress();
        this.phoneNumber = doctor.getPhoneNumber();
        this.note = doctor.getNote();
        this.status = doctor.getStatus();
    }

    public DoctorDTOResponse(Long id,String fullName){
        this.id = id;
        this.fullName = fullName;
    }

    public DoctorDTOResponse(long id, String name, String phone, Status status) {
        this.id = id;
        this.fullName = name;
        this.phoneNumber = phone;
        this.status = status;
    }
}
