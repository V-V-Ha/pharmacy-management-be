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

    @Test
    public void testGetAllDoctorByPaging_FoundDoctors() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging(anyString(), any(), eq(pageable))).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, "John", "ACTIVE");

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging(anyString(), any(), eq(pageable));
    }

    @Test
    public void testGetAllDoctorByPaging_NoDoctorsFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.emptyList());

        when(doctorRepository.getListDoctorPaging(anyString(), any(), eq(pageable))).thenReturn(doctorPage);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(0, 10, "John", "ACTIVE");
        });

        assertEquals(Message.DOCTOR_NOT_FOUND, exception.getMessage());
        verify(doctorRepository, times(1)).getListDoctorPaging(anyString(), any(), eq(pageable));
    }

    @Test
    public void testGetAllDoctorByPaging_InvalidStatus() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(0, 10, "John", "INVALID_STATUS");
        });

        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllDoctorByPaging_DoctorNameAndStatusNull() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging(null, null, pageable)).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, null, null);

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging(null, null, pageable);
    }

    @Test
    public void testGetAllDoctorByPaging_DoctorNameValidAndStatusNull() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging("John", null, pageable)).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, "John", null);

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging("John", null, pageable);
    }

    @Test
    public void testGetAllDoctorByPaging_ValidDoctorNameAndStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse();
        Page<DoctorDTOResponse> doctorPage = new PageImpl<>(Collections.singletonList(doctorDTOResponse));

        when(doctorRepository.getListDoctorPaging("John", Status.ACTIVE, pageable)).thenReturn(doctorPage);

        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(0, 10, "John", "ACTIVE");

        assertFalse(result.isEmpty());
        verify(doctorRepository, times(1)).getListDoctorPaging("John", Status.ACTIVE, pageable);
    }
}
