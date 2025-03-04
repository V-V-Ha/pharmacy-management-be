package com.fu.pha.Service.Supplier;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.service.impl.SupplierServiceImpl;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierChangeStatusTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private SupplierDto supplierDto;
    private Supplier supplier;
    private Validator validator;

    @BeforeEach
    void setUp() {
        // Khởi tạo đối tượng supplier với các giá trị giả định
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Traphaco");
        supplier.setTax("1234567890");
        supplier.setPhoneNumber("0987654321");
        supplier.setStatus(Status.ACTIVE); // Đảm bảo giá trị ban đầu là false
    }

    //Test trường hợp change status nhà cung cấp thành công
    @Test
    void UTCSCS01() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        supplierService.updateSupplierStatus(1L);

        assertTrue(supplier.getStatus() == Status.INACTIVE);
        verify(supplierRepository).save(supplier);
    }

    //Test trường hợp change status nhà cung cấp không tồn tại
    @Test
    void UTCSCS02() {
        when(supplierRepository.findById(200L)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.updateSupplierStatus(200L);
        });

        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }
}
