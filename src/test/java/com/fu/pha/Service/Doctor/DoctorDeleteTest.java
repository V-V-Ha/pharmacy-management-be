//package com.fu.pha.Service.Doctor;
//
//import com.fu.pha.entity.Doctor;
//import com.fu.pha.exception.Message;
//import com.fu.pha.exception.ResourceNotFoundException;
//import com.fu.pha.repository.DoctorRepository;
//import com.fu.pha.service.impl.DoctorServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class DoctorDeleteTest {
//
//    @Mock
//    private DoctorRepository doctorRepository;
//
//    @InjectMocks
//    private DoctorServiceImpl doctorService;
//    private Doctor doctor;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
//        doctor = new Doctor(); // Khởi tạo đối tượng doctor
//        doctor.setId(1L);
//        doctor.setFullName("Vu Van Ha");
//        doctor.setDeleted(false); // Thiết lập giá trị ban đầu
//    }
//
//    //Test trường hợp xóa doctor thành công
//    @Test
//    void UTCDD01() {
//        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
//
//        doctorService.deleteDoctor(1L);
//
//        verify(doctorRepository).save(doctor);
//        assertTrue(doctor.isDeleted());
//    }
//
//    //Test trường hợp xóa doctor không thành công do không tìm thấy doctor
//    @Test
//    void UTCDD02() {
//        when(doctorRepository.findById(123L)).thenReturn(Optional.empty());
//
//        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
//            doctorService.deleteDoctor(123L);
//        });
//
//        assertEquals(Message.DOCTOR_NOT_FOUND, exception.getMessage());
//    }
//
//}
