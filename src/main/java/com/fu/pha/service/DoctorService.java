package com.fu.pha.service;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.dto.response.DoctorDTOResponse;
import org.springframework.data.domain.Page;

public interface DoctorService {
    void createDoctor(DoctorDTORequest doctorDTORequest);

    void updateDoctor(DoctorDTORequest doctorDTORequest);

    void deleteDoctor(Long id);

    DoctorDTOResponse getDoctorById(Long id);

    Page<DoctorDTOResponse> getAllDoctorByPaging(int size, int index, String doctorName);
}
