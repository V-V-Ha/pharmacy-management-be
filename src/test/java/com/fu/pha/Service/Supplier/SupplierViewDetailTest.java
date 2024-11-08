package com.fu.pha.Service.Supplier;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.service.impl.SupplierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierViewDetailTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        // Khởi tạo đối tượng supplier với các giá trị giả định
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Traphaco");
        supplier.setTax("1234567890");
        supplier.setPhoneNumber("0987654321");
    }

    //Test trường hợp lấy nhà cung cấp theo ID thành công
    @Test
    void UTCSVD01() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        SupplierDto result = supplierService.getSupplierById(1L);

        assertEquals(supplier.getId(), result.getId());
        assertEquals(supplier.getSupplierName(), result.getSupplierName());
        assertEquals(supplier.getTax(), result.getTax());
        assertEquals(supplier.getPhoneNumber(), result.getPhoneNumber());
    }

    //Test trường hợp nhà cung cấp không tồn tại
    @Test
    void UTCSVD02() {
        when(supplierRepository.findById(200L)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.getSupplierById(200L);
        });

        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }
}
