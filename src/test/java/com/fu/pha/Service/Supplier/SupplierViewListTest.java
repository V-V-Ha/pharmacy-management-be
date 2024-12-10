package com.fu.pha.Service.Supplier;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.service.impl.SupplierServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierViewListTest {
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    // Test case: Found suppliers with valid search criteria
    @Test
    public void UTCSL01() {
        // Arrange
        SupplierDto supplierDto = new SupplierDto(1L, "Nam Hà", "Hà Nội", "0987654321", "namha@gmail.com", "0789654321", Status.ACTIVE);
        List<SupplierDto> supplierList = List.of(supplierDto);
        Page<SupplierDto> expectedPage = new PageImpl<>(supplierList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "Nam Hà";
        String status = "ACTIVE";

        when(supplierRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<SupplierDto> result = supplierService.getAllSupplierAndPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Nam Hà", result.getContent().get(0).getSupplierName());
    }

    // Test case: No suppliers found
    @Test
    public void UTCSL02() {
        // Arrange
        String name = "Sơn Tùng";
        String status = "ACTIVE";
        Pageable pageable = PageRequest.of(0, 10);

        when(supplierRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(Page.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.getAllSupplierAndPaging(0, 10, name, status);
        });
        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }

    // Test case: Invalid status value (not found)
    @Test
    public void UTCSL03() {
        // Arrange
        String name = "Nam Hà";
        String invalidStatus = "INVALID_STATUS";
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.getAllSupplierAndPaging(0, 10, name, invalidStatus);
        });
        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    // Test case: Status is null
    @Test
    public void UTCSL04() {
        // Arrange
        SupplierDto supplierDto = new SupplierDto(1L, "Nam Hà", "Hà Nội", "0987654321", "namha@gmail.com", "0789123456", Status.ACTIVE);
        List<SupplierDto> supplierList = List.of(supplierDto);
        Page<SupplierDto> expectedPage = new PageImpl<>(supplierList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "Nam Hà";
        String status = null;

        when(supplierRepository.findAllByNameContaining(name, null, pageable)).thenReturn(expectedPage);

        // Act
        Page<SupplierDto> result = supplierService.getAllSupplierAndPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Nam Hà", result.getContent().get(0).getSupplierName());
    }

    // Test case: Null or empty name
    @Test
    public void UTCSL05() {
        // Arrange
        SupplierDto supplierDto = new SupplierDto(1L, "Nam Hà", "Hà Nội", "0987654321", "namha@gmail.com", "0789123456", Status.ACTIVE);
        List<SupplierDto> supplierList = List.of(supplierDto);
        Page<SupplierDto> expectedPage = new PageImpl<>(supplierList);
        Pageable pageable = PageRequest.of(0, 10);
        String name = "";
        String status = "ACTIVE";

        when(supplierRepository.findAllByNameContaining(name, Status.ACTIVE, pageable)).thenReturn(expectedPage);

        // Act
        Page<SupplierDto> result = supplierService.getAllSupplierAndPaging(0, 10, name, status);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("Nam Hà", result.getContent().get(0).getSupplierName());
    }

}
