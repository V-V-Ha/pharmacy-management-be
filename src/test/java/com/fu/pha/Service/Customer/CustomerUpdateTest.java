package com.fu.pha.Service.Customer;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.entity.Customer;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CustomerRepository;
import com.fu.pha.service.impl.CustomerServiceImpl;
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
public class CustomerUpdateTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;
    private CustomerDTORequest customerDTORequest;
    private Customer customer;
    private Validator validator;

    @BeforeEach
    void setUpUpdate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Khởi tạo đối tượng customer giả định
        customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");

        // Khởi tạo đối tượng customerDTORequest giả định
        customerDTORequest = new CustomerDTORequest();
        customerDTORequest.setId(1L);
        customerDTORequest.setCustomerName("Vũ Văn Hà");
        customerDTORequest.setPhoneNumber("0987654321");
        customerDTORequest.setYob(1990);

    }

    // Test trường hợp cập nhật khách hàng thành công
    @Test
    void UTCCU01() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.empty());

        customerService.updateCustomer(customerDTORequest);

        verify(customerRepository).save(customer);
        assertEquals("Vũ Văn Hà", customer.getCustomerName());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì không tìm thấy khách hàng
    @Test
    void UTCCU02() {
        // Đặt id của customerDTORequest thành 123 để đảm bảo đồng bộ trong quá trình kiểm tra
        customerDTORequest.setId(123L);

        // Khi findById với ID 123, trả về Optional.empty()
        when(customerRepository.findById(123L)).thenReturn(Optional.empty());

        // Kiểm tra rằng ngoại lệ ResourceNotFoundException được ném ra khi không tìm thấy khách hàng
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomer(customerDTORequest);
        });

        // Kiểm tra thông điệp lỗi để đảm bảo đúng ngoại lệ được ném ra
        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì trường customerName không hợp lệ
    @Test
    void UTCCU03() {
        customerDTORequest.setCustomerName("Vũ Văn Hà@");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_NAME, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì trường customerName bị null
    @Test
    void UTCCU04() {
        customerDTORequest.setCustomerName(null);

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_NAME, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì trường phoneNumber không hợp lệ
    @Test
    void UTCCU05() {
        customerDTORequest.setPhoneNumber("0987654321a");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì số điện thoại đã tồn tại
    @Test
    void UTCCU06() {
        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(2L);
        anotherCustomer.setPhoneNumber("0987654321");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.of(anotherCustomer));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomer(customerDTORequest);
        });

        assertEquals(Message.EXIST_PHONE, exception.getMessage());
    }

}
