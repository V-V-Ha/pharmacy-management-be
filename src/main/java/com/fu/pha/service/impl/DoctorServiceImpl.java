package com.fu.pha.service.impl;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.entity.Doctor;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.DoctorRepository;
import com.fu.pha.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional
    @Override
    public void createDoctor(DoctorDTORequest doctorDTORequest) {
        Doctor doctor = new Doctor();
        doctor.setFullName(doctorDTORequest.getFullName());
        doctor.setAddress(doctorDTORequest.getAddress());
        doctor.setPhoneNumber(doctorDTORequest.getPhoneNumber());
        doctor.setNote(doctorDTORequest.getNote());
        doctor.setStatus(Status.ACTIVE);
        doctorRepository.save(doctor);
    }

    @Transactional
    @Override
    public void updateDoctor(DoctorDTORequest doctorDTORequest) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(doctorDTORequest.getId());
        if (doctorOptional.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }

        Doctor doctor = doctorOptional.get();
        doctor.setFullName(doctorDTORequest.getFullName());
        doctor.setAddress(doctorDTORequest.getAddress());
        doctor.setPhoneNumber(doctorDTORequest.getPhoneNumber());
        doctor.setNote(doctorDTORequest.getNote());
        doctorRepository.save(doctor);
    }

    @Override
    public void activeDoctor(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }
        doctor.get().setStatus(Status.ACTIVE);
        doctorRepository.save(doctor.get());
    }

    @Override
    public void deActiveDoctor(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }
        doctor.get().setStatus(Status.INACTIVE);
        doctorRepository.save(doctor.get());
    }

    @Override
    public DoctorDTOResponse getDoctorById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }
        return new DoctorDTOResponse(doctor.get());
    }

    @Override
    public Page<DoctorDTOResponse> getAllDoctorByPaging(int size, int index, String doctorName, String status) {
        Pageable pageable = PageRequest.of(size, index);
        Status doctorStatus = null;
        if (status != null) {
            try {
                doctorStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }

        Page<DoctorDTOResponse> doctorDTOResponses = doctorRepository.getListDoctorPaging(doctorName, doctorStatus, pageable);
        if (doctorDTOResponses.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }
        return doctorDTOResponses;
    }

    @Override
    public List<DoctorDTOResponse> getDoctorByDoctorName(String doctorName) {
        Optional<List<DoctorDTOResponse>> doctors = doctorRepository.findByDoctorName(doctorName);
        if (doctors.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }

        return doctors.get();
    }
}
