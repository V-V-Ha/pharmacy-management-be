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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SupplierCreateTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private SupplierDto supplierDto;
    private Supplier supplier;
    private Validator validator;

    @BeforeEach
    void setUpCreate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        supplierDto = new SupplierDto();
        supplierDto.setSupplierName("Traphaco");
        supplierDto.setTax("1234567890");
        supplierDto.setPhoneNumber("0987654321");

        supplier = new Supplier();
        supplier.setSupplierName("Traphaco");
        supplier.setTax("1234567890");
        supplier.setPhoneNumber("0987654321");
    }

    //Test trường hợp tạo mới nhà cung cấp thành công
    @Test
    void UTCSC01() {
        when(supplierRepository.findByTax(supplierDto.getTax())).thenReturn(Optional.empty());
        when(supplierRepository.findByPhoneNumber(supplierDto.getPhoneNumber())).thenReturn(Optional.empty());

        supplierService.createSupplier(supplierDto);

        verify(supplierRepository).save(any(Supplier.class));
    }

    //Test trường hợp tên nhà cung cấp null
    @Test
    void UTCSC02() {
        supplierDto.setSupplierName(null);  // Supplier name is missing
        supplierDto.setTax("");  // Tax number is missing

        // Xác thực SupplierDto
        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        // Kiểm tra xem có vi phạm nào không
        assertFalse(violations.isEmpty(), "Expected validation errors for missing supplier name or tax number");

    }

    //Test trường hợp số điện thoại không hợp lệ
    @Test
    void UTCSC03() {
        supplierDto.setPhoneNumber("0987654321a"); // Đặt số điện thoại không hợp lệ

        // Xác thực SupplierDto
        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        // Kiểm tra nếu có vi phạm xác thực
        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        // Lấy vi phạm đầu tiên và kiểm tra thông báo lỗi
        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    //Test trường hợp số điện thoại null
    @Test
    void UTCSC04() {
        supplierDto.setPhoneNumber(null);  // Đặt phoneNumber thành chuỗi trống

        // Xác thực SupplierDto
        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        // Kiểm tra nếu có vi phạm xác thực
        assertFalse(violations.isEmpty(), "Expected validation errors for null phone number");

        // Lấy vi phạm đầu tiên và kiểm tra thông báo lỗi
        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }


    //Test trường hợp mã số thuế không hợp lệ
    @Test
    void UTCSC05() {
        supplierDto.setTax("123456789A"); // Đặt mã số thuế không hợp lệ

        // Xác thực SupplierDto
        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        // Kiểm tra nếu có vi phạm xác thực
        assertFalse(violations.isEmpty(), "Expected validation errors for invalid tax number");

        // Lấy vi phạm đầu tiên và kiểm tra thông báo lỗi
        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.INVALID_TAX, violation.getMessage());
    }

    //Test trường hợp thuế null
    @Test
    void UTCSC06() {
        supplierDto.setTax(null);  // Đặt tax trống để kiểm thử

        // Xác thực SupplierDto
        Set<ConstraintViolation<SupplierDto>> violations = validator.validate(supplierDto);

        // Kiểm tra nếu có vi phạm xác thực
        assertFalse(violations.isEmpty(), "Expected validation errors for null tax");

        // Kiểm tra thông báo lỗi của vi phạm
        ConstraintViolation<SupplierDto> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    //Test trường hợp nhà cung cấp đã tồn tại
    @Test
    void UTCSC07() {
        when(supplierRepository.findByTax(supplierDto.getTax())).thenReturn(Optional.of(supplier));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.createSupplier(supplierDto);
        });

        assertEquals(Message.TAX_EXIST, exception.getMessage());
    }

    //Test trường hợp số điện thoại đã tồn tại
    @Test
    void UTCSC08() {
        when(supplierRepository.findByTax(supplierDto.getTax())).thenReturn(Optional.empty());
        when(supplierRepository.findByPhoneNumber(supplierDto.getPhoneNumber())).thenReturn(Optional.of(supplier));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            supplierService.createSupplier(supplierDto);
        });

        assertEquals(Message.EXIST_PHONE, exception.getMessage());
    }

}
