package com.fu.pha.Service.Doctor;

import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.DoctorRepository;
import com.fu.pha.service.impl.DoctorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DoctorViewListTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Pageable pageable;

    @Test
    public void testGetAllDoctorByPaging_FoundDoctors() {
        // Arrange
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse(1L, "Doctor1", "123456789", Status.ACTIVE);
        Page<DoctorDTOResponse> expectedPage = new PageImpl<>(List.of(doctorDTOResponse));
        int size = 10;
        int index = 0;

        when(doctorRepository.getListDoctorPaging("Doctor1", Status.ACTIVE, PageRequest.of(index, size)))
                .thenReturn(expectedPage);

        // Act
        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(size, index, "Doctor1", "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(doctorRepository).getListDoctorPaging("Doctor1", Status.ACTIVE, PageRequest.of(index, size));
    }

    @Test
    public void testGetAllDoctorByPaging_NoDoctorsFound() {
        // Arrange
        int size = 10;
        int index = 0;

        when(doctorRepository.getListDoctorPaging("NonExistentDoctor", Status.ACTIVE, PageRequest.of(index, size)))
                .thenReturn(Page.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(size, index, "NonExistentDoctor", "ACTIVE");
        });
    }

    @Test
    public void testGetAllDoctorByPaging_InvalidSize() {
        // Arrange
        int invalidSize = 0; // size không hợp lệ
        int index = 0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            doctorService.getAllDoctorByPaging(invalidSize, index, "Doctor1", "ACTIVE");
        });
    }

    @Test
    public void testGetAllDoctorByPaging_InvalidIndex() {
        // Arrange
        int size = 10;
        int invalidIndex = -1; // index không hợp lệ

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            doctorService.getAllDoctorByPaging(size, invalidIndex, "Doctor1", "ACTIVE");
        });
    }

    @Test
    public void testGetAllDoctorByPaging_InvalidStatus() {
        // Arrange
        int size = 10;
        int index = 0;
        String invalidStatus = "NOT_EXIST"; // status không hợp lệ

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(size, index, "Doctor1", invalidStatus);
        });
    }

    @Test
    public void testGetAllDoctorByPaging_DoctorNameAndStatusNull() {
        // Arrange
        int size = 10;
        int index = 0;

        when(doctorRepository.getListDoctorPaging(null, null, PageRequest.of(index, size)))
                .thenReturn(Page.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            doctorService.getAllDoctorByPaging(size, index, null, null);
        });
    }

    @Test
    public void testGetAllDoctorByPaging_DoctorNameValidAndStatusNull() {
        // Arrange
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse(1L, "Doctor1", "123456789", Status.ACTIVE);
        Page<DoctorDTOResponse> expectedPage = new PageImpl<>(List.of(doctorDTOResponse));
        int size = 10;
        int index = 0;

        when(doctorRepository.getListDoctorPaging("Doctor1", null, PageRequest.of(index, size)))
                .thenReturn(expectedPage);

        // Act
        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(size, index, "Doctor1", null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(doctorRepository).getListDoctorPaging("Doctor1", null, PageRequest.of(index, size));
    }

    @Test
    public void testGetAllDoctorByPaging_ValidDoctorNameAndStatus() {
        // Arrange
        DoctorDTOResponse doctorDTOResponse = new DoctorDTOResponse(1L, "Doctor1", "123456789", Status.ACTIVE);
        Page<DoctorDTOResponse> expectedPage = new PageImpl<>(List.of(doctorDTOResponse));
        int size = 10;
        int index = 0;

        when(doctorRepository.getListDoctorPaging("Doctor1", Status.ACTIVE, PageRequest.of(index, size)))
                .thenReturn(expectedPage);

        // Act
        Page<DoctorDTOResponse> result = doctorService.getAllDoctorByPaging(size, index, "Doctor1", "ACTIVE");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        verify(doctorRepository).getListDoctorPaging("Doctor1", Status.ACTIVE, PageRequest.of(index, size));
    }

}
