package com.fu.pha.Service.Unit;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.UnitRepository;
import com.fu.pha.service.impl.UnitServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UnitViewListTest {
    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private UnitServiceImpl unitService;

    // Test case: Found units with valid search criteria
    @Test
    public void testGetAllUnitPaging_FoundUnits() {
        // Arrange
        UnitDto unitDto = new UnitDto(1L, "Unit1", "Description1", Instant.now(), Instant.now(), "Admin", "Admin", Status.ACTIVE);
        List<UnitDto> unitList = List.of(unitDto);
        Page<UnitDto> expectedPage = new PageImpl<>(unitList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "Unit1";
        String status = "ACTIVE";

        when(unitRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<UnitDto> result = unitService.getAllUnitPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Unit1", result.getContent().get(0).getUnitName());
    }

    // Test case: No units found
    @Test
    public void testGetAllUnitPaging_NoUnitsFound() {
        // Arrange
        String name = "NonExistentUnit";
        String status = "ACTIVE";
        Pageable pageable = PageRequest.of(0, 10);

        when(unitRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(Page.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            unitService.getAllUnitPaging(0, 10, name, status);
        });
    }

    // Test case: Invalid status value (not found)
    @Test
    public void testGetAllUnitPaging_InvalidStatus() {
        // Arrange
        String name = "Unit1";
        String invalidStatus = "INVALID_STATUS";
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            unitService.getAllUnitPaging(0, 10, name, invalidStatus);
        });
    }

    // Test case: Page or size is invalid (negative)
    @Test
    public void testGetAllUnitPaging_InvalidPageSize() {
        // Arrange
        String name = "Unit1";
        String status = "ACTIVE";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            unitService.getAllUnitPaging(-1, -1, name, status); // Invalid page and size
        });
    }

    // Test case: Status is null
    @Test
    public void testGetAllUnitPaging_StatusIsNull() {
        // Arrange
        UnitDto unitDto = new UnitDto(1L, "Unit1", "Description1", Instant.now(), Instant.now(), "Admin", "Admin", Status.ACTIVE);
        List<UnitDto> unitList = List.of(unitDto);
        Page<UnitDto> expectedPage = new PageImpl<>(unitList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "Unit1";
        String status = null;

        when(unitRepository.findAllByNameContaining(name, null, pageable)).thenReturn(expectedPage);

        // Act
        Page<UnitDto> result = unitService.getAllUnitPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Unit1", result.getContent().get(0).getUnitName());
    }

    // Test case: Null or empty name
    @Test
    public void testGetAllUnitPaging_NameIsNullOrEmpty() {
        // Arrange
        UnitDto unitDto = new UnitDto(1L, "Unit1", "Description1", Instant.now(), Instant.now(), "Admin", "Admin", Status.ACTIVE);
        List<UnitDto> unitList = List.of(unitDto);
        Page<UnitDto> expectedPage = new PageImpl<>(unitList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "";
        String status = "ACTIVE";

        when(unitRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<UnitDto> result = unitService.getAllUnitPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Unit1", result.getContent().get(0).getUnitName());
    }

    // Test case: Exception thrown from repository
    @Test
    public void testGetAllUnitPaging_RepositoryException() {
        // Arrange
        String name = "Unit1";
        String status = "ACTIVE";
        Pageable pageable = PageRequest.of(0, 10);

        when(unitRepository.findAllByNameContaining(name, Status.ACTIVE, pageable))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            unitService.getAllUnitPaging(0, 10, name, status);
        });
    }
}
