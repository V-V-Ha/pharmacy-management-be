package com.fu.pha.Service.Doctor;

import com.fu.pha.entity.Doctor;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.DoctorRepository;
import com.fu.pha.service.impl.DoctorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DoctorViewDetailTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
        doctor = new Doctor(); // Khởi tạo đối tượng doctor
        doctor.setId(1L);
        doctor.setFullName("Vu Van Ha");
    }

    //Test trường hợp lấy thông tin doctor thành công
    @Test
    void UTCDVD01() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        doctorService.getDoctorById(1L);

        verify(doctorRepository).findById(1L);
    }

    //Test trường hợp lấy thông tin doctor không thành công do không tìm thấy doctor
    @Test
    void UTCDVD02() {
        when(doctorRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getDoctorById(200L);
        });

        assertEquals(Message.DOCTOR_NOT_FOUND, exception.getMessage());
    }



}
