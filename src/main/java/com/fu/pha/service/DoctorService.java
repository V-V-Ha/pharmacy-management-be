package com.fu.pha.service;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.dto.response.DoctorDTOResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DoctorService {
    void createDoctor(DoctorDTORequest doctorDTORequest);

    void updateDoctor(DoctorDTORequest doctorDTORequest);

    void updateDoctorStatus(Long id);

    DoctorDTOResponse getDoctorById(Long id);

    List<DoctorDTOResponse> getDoctorByDoctorName(String doctorName);

    Page<DoctorDTOResponse> getAllDoctorByPaging(int size, int index, String doctorName, String status);
}
