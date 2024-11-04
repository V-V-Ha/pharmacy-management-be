package com.fu.pha.service.impl;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.entity.Doctor;
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
    public void deleteDoctor(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }
        doctor.get().setDeleted(true);
        doctorRepository.save(doctor.get());
    }

    @Override
    public DoctorDTOResponse getDoctorById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isEmpty()) {
            throw new ResourceNotFoundException(Message.DOCTOR_NOT_FOUND);
        }

        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        doctorDTOResponse.setId(doctor.get().getId());
        doctorDTOResponse.setFullName(doctor.get().getFullName());
        doctorDTOResponse.setAddress(doctor.get().getAddress());
        doctorDTOResponse.setPhoneNumber(doctor.get().getPhoneNumber());
        doctorDTOResponse.setNote(doctor.get().getNote());
        return doctorDTOResponse;
    }

    @Override
    public Page<DoctorDTOResponse> getAllDoctorByPaging(int size, int index, String doctorName) {
        Pageable pageable = PageRequest.of(size, index);
        Page<DoctorDTOResponse> doctorDTOResponses = doctorRepository.getListDoctorPaging(doctorName, pageable);
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
