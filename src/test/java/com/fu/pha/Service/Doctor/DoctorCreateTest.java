package com.fu.pha.Service.Doctor;

import com.fu.pha.dto.request.DoctorDTORequest;
import com.fu.pha.entity.Doctor;
import com.fu.pha.exception.Message;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DoctorCreateTest {


    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private DoctorDTORequest doctorDTORequest;
    private Validator validator;

    @BeforeEach
    void setUpCreate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        doctorDTORequest = new DoctorDTORequest();
        doctorDTORequest.setFullName("Vũ Văn Hà");
        doctorDTORequest.setPhoneNumber("0987654321");
        doctorDTORequest.setAddress("Cầu giấy, Hà Nội");
    }

    //Test trường hợp create doctor thành công
    @Test
    void UTCDC01() {
        doctorService.createDoctor(doctorDTORequest);

        verify(doctorRepository).save(any(Doctor.class));
    }

    //Test trường hợp create doctor không thành công do full name không hợp lệ
    @Test
    void UTCDC02() {
        doctorDTORequest.setFullName("Vũ Văn Hà@");

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid full name");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_NAME, violation.getMessage());
    }

    //Test trường hợp create doctor không thành công do full name null
    @Test
    void UTCDC03() {
        doctorDTORequest.setFullName(null);

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null full name");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    //Test trường hợp create doctor không thành công do address null
    @Test
    void UTCDC04() {
        doctorDTORequest.setAddress("");

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null address");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    //Test trường hợp create doctor không thành công do phone number không hợp lệ
    @Test
    void UTCDC05() {
        doctorDTORequest.setPhoneNumber("0987654321a");

        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    //Test trường hợp create doctor không thành công do phone number null
    @Test
    void UTCDC06() {
        // Đặt phoneNumber thành null để kiểm tra ràng buộc
        doctorDTORequest.setPhoneNumber(null);

        // Xác thực các ràng buộc
        Set<ConstraintViolation<DoctorDTORequest>> violations = validator.validate(doctorDTORequest);

        // Kiểm tra nếu có lỗi ràng buộc
        assertFalse(violations.isEmpty(), "Expected validation errors for null phone number");

        // Lấy thông báo lỗi đầu tiên và kiểm tra nội dung của nó
        ConstraintViolation<DoctorDTORequest> violation = violations.iterator().next();
        assertEquals("Thông tin không được trống", violation.getMessage()); // Sửa thông báo để khớp với thực tế
    }




}
