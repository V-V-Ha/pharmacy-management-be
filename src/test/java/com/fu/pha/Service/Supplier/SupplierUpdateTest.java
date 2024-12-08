package com.fu.pha.Service.Supplier;

import com.fu.pha.dto.request.SupplierDto;
import com.fu.pha.entity.Supplier;
import com.fu.pha.exception.BadRequestException;
import com.fu.pha.exception.Message;
import com.fu.pha.repository.SupplierRepository;
import com.fu.pha.service.impl.SupplierServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierUpdateTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private SupplierDto supplierDto;
    private Supplier supplier;
    private Validator validator;

    @BeforeEach
    void setUpUpdate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        supplierDto = new SupplierDto();
        supplierDto.setId(1L);
        supplierDto.setSupplierName("Traphaco");
        supplierDto.setTax("1234567890");
        supplierDto.setPhoneNumber("0987654321");

        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Traphaco");
        supplier.setTax("1234567890");
        supplier.setPhoneNumber("0987654321");
    }

    //Test trường hợp cập nhật nhà cung cấp thành công
    @Test
    void UTCSU01() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.findByTax("1234567890")).thenReturn(Optional.empty());

        supplierService.updateSupplier(supplierDto);

        verify(supplierRepository).save(supplier);
        assertEquals("Traphaco", supplier.getSupplierName());
    }

    //Test trường hợp nhà cung cấp không tồn tại
    @Test
    void UTCSU02() {
        supplierDto.setId(123L);  // Đảm bảo ID khớp với ID đã stub

        when(supplierRepository.findById(123L)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.updateSupplier(supplierDto);
        });

        assertEquals(Message.SUPPLIER_NOT_FOUND, exception.getMessage());
    }

    //Test trường hợp nhà cung cấp với null tên nhà cung cấp
    @Test
    void UTCSU03() {
        supplierDto.setSupplierName("");

        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        assertFalse(violations.isEmpty(), "Expected validation errors for null supplier name");

        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.NULL_NAME, violation.getMessage());
    }

    //Test trường hợp nhà cung cấp với số điện thoại không hợp lệ
    @Test
    void UTCSU04() {
        supplierDto.setPhoneNumber("0987654321a");

        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    //Test trường hợp nhà cung cấp với null số điện thoại
    @Test
    void UTCSU05() {
        supplierDto.setPhoneNumber(""); // Đặt phoneNumber thành chuỗi trống

        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        // Kiểm tra rằng có ít nhất một lỗi xác thực
        assertFalse(violations.isEmpty(), "Expected validation errors for null phone number");

        // Kiểm tra xem có lỗi "Thông tin không được trống" trong danh sách các vi phạm
        boolean hasNullFieldMessage = violations.stream()
                .anyMatch(violation -> Message.NULL_FILED.equals(violation.getMessage()));

        assertTrue(hasNullFieldMessage, "Expected validation error message for null phone number");
    }

    //Test trường hợp nhà cung cấp với mã số thuế không hợp lệ
    @Test
    void UTCSU06() {
        supplierDto.setTax("123456789A");

        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid tax number");

        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.INVALID_TAX, violation.getMessage());
    }

    //Test trường hợp nhà cung cấp với null mã số thuế
    @Test
    void UTCSU07() {
        supplierDto.setTax("");

        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        assertFalse(violations.isEmpty(), "Expected validation errors for null tax");

        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    //Test trường hợp nhà cung cấp vơi mã số thuế đã tồn tại
    @Test
    void UTCSU08() {
        Supplier anotherSupplier = new Supplier();
        anotherSupplier.setId(2L);
        anotherSupplier.setTax("1234567890");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(supplierRepository.findByTax("1234567890")).thenReturn(Optional.of(anotherSupplier));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.updateSupplier(supplierDto);
        });

        assertEquals(Message.SUPPLIER_EXIST, exception.getMessage());
    }

    //Test trường hợp nhà cung cấp với số điện thoại đã tồn tại
    @Test
    void UTCSU09() {
        // Tạo một nhà cung cấp khác với số điện thoại trùng lặp
        Supplier anotherSupplier = new Supplier();
        anotherSupplier.setId(2L); // ID khác với supplierDto
        anotherSupplier.setPhoneNumber("0987654321");

        // Thiết lập supplierDto và supplier
        supplierDto.setId(1L); // ID của nhà cung cấp cần cập nhật
        supplierDto.setPhoneNumber("0987654321"); // Số điện thoại trùng với anotherSupplier

        // Stub repository để trả về nhà cung cấp với ID khác nhưng có cùng số điện thoại
        when(supplierRepository.findById(supplierDto.getId())).thenReturn(Optional.of(new Supplier()));
        when(supplierRepository.findByPhoneNumber(supplierDto.getPhoneNumber())).thenReturn(Optional.of(anotherSupplier));

        // Kiểm tra xem có ném ra ngoại lệ với thông báo mong đợi hay không
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.updateSupplier(supplierDto);
        });

        assertEquals(Message.EXIST_PHONE, exception.getMessage());
    }

}
