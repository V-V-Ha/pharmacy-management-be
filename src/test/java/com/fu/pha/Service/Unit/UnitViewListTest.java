package com.fu.pha.Service.Unit;

import com.fu.pha.dto.request.UnitDto;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
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

    // Test case: No units found
    @Test
    public void UTCUNL01() {
        // Arrange
        String name = "Vỉ";
        String status = "ACTIVE";
        Pageable pageable = PageRequest.of(0, 10);

        when(unitRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(Page.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            unitService.getAllUnitPaging(0, 10, name, status);
        });
        assertEquals(Message.UNIT_NOT_FOUND, exception.getMessage());
    }

    // Test case: Invalid status value (not found)
    @Test
    public void UTCUNL02() {
        // Arrange
        String name = "Hộp";
        String invalidStatus = "INVALID_STATUS";

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            unitService.getAllUnitPaging(0, 10, name, invalidStatus);
        });
    }

    // Test case: Status is null
    @Test
    public void UTCUNL03() {
        // Arrange
        UnitDto unitDto = new UnitDto(1L, "Hộp", "abc", Instant.now(), Instant.now(), "minhhieu", "minhhieu", Status.ACTIVE);
        List<UnitDto> unitList = List.of(unitDto);
        Page<UnitDto> expectedPage = new PageImpl<>(unitList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "Hộp";
        String status = null;

        when(unitRepository.findAllByNameContaining(name, null, pageable)).thenReturn(expectedPage);

        // Act
        Page<UnitDto> result = unitService.getAllUnitPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Hộp", result.getContent().get(0).getUnitName());
    }

    // Test case: Null or empty name
    @Test
    public void UTCUNL04() {
        // Arrange
        UnitDto unitDto = new UnitDto(1L, "Hộp", "Description1", Instant.now(), Instant.now(), "minhhieu", "minhhieu", Status.ACTIVE);
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
        assertEquals("Hộp", result.getContent().get(0).getUnitName());
    }

    // Test case: Found units with valid search criteria
    @Test
    public void UTCUNL05() {
        // Arrange
        UnitDto unitDto = new UnitDto(1L, "Hộp", "Description1", Instant.now(), Instant.now(), "minhhieu", "minhhieu", Status.ACTIVE);
        List<UnitDto> unitList = List.of(unitDto);
        Page<UnitDto> expectedPage = new PageImpl<>(unitList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "Hộp";
        String status = "ACTIVE";

        when(unitRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<UnitDto> result = unitService.getAllUnitPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Hộp", result.getContent().get(0).getUnitName());
    }

}
