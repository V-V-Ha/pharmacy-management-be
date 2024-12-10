package com.fu.pha.Service.Doctor;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.entity.Doctor;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.DoctorRepository;
import com.fu.pha.service.impl.DoctorServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DoctorUpdateTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private DoctorDTORequest doctorDTORequest;
    private Validator validator;
    private Doctor doctor;

    @BeforeEach
    void setUpUpdate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        doctorDTORequest = new DoctorDTORequest();
        doctorDTORequest.setId(1L);
        doctorDTORequest.setFullName("Vũ Văn Hà");
        doctorDTORequest.setPhoneNumber("0987654321");
        doctorDTORequest.setAddress("Cầu giấy, Hà Nội");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setFullName("Vũ Văn Hà");
        doctor.setPhoneNumber("0987654321");
        doctor.setAddress("Cầu giấy, Hà Nội");
    }

    //Test trường hợp update doctor thành công
    @Test
    void UTCDU01() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        doctorService.updateDoctor(doctorDTORequest);

        verify(doctorRepository).save(doctor);
        assertEquals("Vũ Văn Hà", doctor.getFullName());
    }

    //Test trường hợp update doctor không thành công do không tìm thấy doctor
    @Test
    void UTCDU02() {
        doctorDTORequest.setId(123L);
        when(doctorRepository.findById(123L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.updateDoctor(doctorDTORequest);
        });

        assertEquals(Message.DOCTOR_NOT_FOUND, exception.getMessage());
    }

    //Test trường hợp update doctor không thành công do full name không hợp lệ
    @Test
    void UTCDU03() {
        doctorDTORequest.setFullName("Vũ Văn Hà@");

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid full name");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_NAME, violation.getMessage());
    }

    //Test trường hợp update doctor không thành công do full name null
    @Test
    void UTCDU04() {
        doctorDTORequest.setFullName(null);

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null full name");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_NAME, violation.getMessage());
    }

    //Test trường hợp update doctor không thành công do address null
    @Test
    void UTCDU05() {
        doctorDTORequest.setAddress("");

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null address");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    //Test trường hợp update doctor không thành công do phone number không hợp lệ
    @Test
    void UTCDU06() {
        doctorDTORequest.setPhoneNumber("'0987654321a");

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    //Test trường hợp update doctor không thành công do phone number null
    @Test
    void UTCDU07() {
        doctorDTORequest.setPhoneNumber(null);

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null phone number");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

}
