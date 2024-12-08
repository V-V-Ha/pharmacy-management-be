package com.fu.pha.Service.Doctor;

import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.enums.Status;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class DoctorViewListTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test case: Get all doctors by paging with doctor name is not null and status is null
    @Test
    public void UTCDL01() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging("Hà", Status.ACTIVE, pageable)).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, "Hà", "ACTIVE");

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging("Hà", Status.ACTIVE, pageable);
    }

    // Test case: Get all doctors by paging with doctor name is not found
    @Test
    public void UTCDL02() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.emptyList());

        when(doctorRepository.getListDoctorPaging(anyString(), any(), eq(pageable))).thenReturn(doctorPage);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(0, 10, "Minh Hiếu", "ACTIVE");
        });

        assertEquals(Message.DOCTOR_NOT_FOUND, exception.getMessage());
        verify(doctorRepository, times(1)).getListDoctorPaging(anyString(), any(), eq(pageable));
    }

    // Test case: Get all doctors by paging with invalid status
    @Test
    public void UTCDL03() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(0, 10, "Hà", "INVALID_STATUS");
        });

        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    // Test case: Get all doctors by paging with doctor name is null and status is null
    @Test
    public void UTCDL04() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging(null, null, pageable)).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, null, null);

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging(null, null, pageable);
    }

    // Test case: Get all doctors by paging with status is not null
    @Test
    public void UTCDL05() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging("Hà", null, pageable)).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, "Hà", null);

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging("Hà", null, pageable);
    }


}
