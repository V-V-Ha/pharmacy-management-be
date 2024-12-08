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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerCreateTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;
    private CustomerDTORequest customerDTORequest;
    private Customer customer;
    private Validator validator;

    @BeforeEach
    void setUpCreate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Khởi tạo đối tượng customer với dữ liệu giả định
        customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");

        customerDTORequest = new CustomerDTORequest();
        customerDTORequest.setCustomerName("Vũ Văn Hà");
        customerDTORequest.setPhoneNumber("0987654321");
        customerDTORequest.setYob(1990);

    }

    // Test trường hợp tạo khách hàng thành công
    @Test
    void UTCCC01() {
        when(customerRepository.findByPhoneNumber(customerDTORequest.getPhoneNumber())).thenReturn(Optional.empty());

        customerService.createCustomer(customerDTORequest);

        verify(customerRepository).save(any(Customer.class));
    }

    // Test trường hợp tạo khách hàng không thành công vì trường customerName không hợp lệ
    @Test
    void UTCCC02() {
        customerDTORequest.setCustomerName("Vũ Văn Hà@");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_NAME, violation.getMessage());
    }

    @Test
    void UTCCC03() {
        customerDTORequest.setCustomerName(null);

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_NAME, violation.getMessage());
    }

    // Test trường hợp tạo khách hàng không thành công vì trường phoneNumber không hợp lệ
    @Test
    void UTCCC04() {
        customerDTORequest.setPhoneNumber("0987654321a");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    // Test trường hợp tạo khách hàng không thành công vì số điện thoại đã tồn tại
    @Test
    void UTCCC05() {
        when(customerRepository.findByPhoneNumber(customerDTORequest.getPhoneNumber())).thenReturn(Optional.of(customer));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.createCustomer(customerDTORequest);
        });

        assertEquals(Message.EXIST_PHONE, exception.getMessage());
    }

}
