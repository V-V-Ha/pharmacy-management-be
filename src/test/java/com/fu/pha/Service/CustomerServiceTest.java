package com.fu.pha.Service;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

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

        customerDTORequest = new CustomerDTORequest();
        customerDTORequest.setCustomerName("Vũ Văn Hà");
        customerDTORequest.setPhoneNumber("0987654321");

        customer = new Customer();
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");
    }

    // Test trường hợp tạo khách hàng thành công
    @Test
     void testCreateCustomer_Success() {
        when(customerRepository.findByPhoneNumber(customerDTORequest.getPhoneNumber())).thenReturn(Optional.empty());

        customerService.createCustomer(customerDTORequest);

        verify(customerRepository).save(any(Customer.class));
    }

    // Test trường hợp tạo khách hàng không thành công vì trường customerName không hợp lệ
    @Test
     void testCreateCustomer_InvalidCustomerName() {
        customerDTORequest.setCustomerName("Vũ Văn Hà@");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_NAME, violation.getMessage());
    }

    @Test
    void testCreateCustomer_NullCustomerName() {
        customerDTORequest.setCustomerName(null);

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    // Test trường hợp tạo khách hàng không thành công vì trường phoneNumber không hợp lệ
    @Test
    void testCreateCustomer_InvalidPhoneNumber() {
        customerDTORequest.setPhoneNumber("0987654321a");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    // Test trường hợp tạo khách hàng không thành công vì trường phoneNumber bị null
    @Test
    void testCreateCustomer_NullPhoneNumber() {
        customerDTORequest.setPhoneNumber(null);

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null phone number");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    // Test trường hợp tạo khách hàng không thành công vì số điện thoại đã tồn tại
    @Test
    void testCreateCustomer_PhoneNumberExist() {
        when(customerRepository.findByPhoneNumber(customerDTORequest.getPhoneNumber())).thenReturn(Optional.of(customer));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.createCustomer(customerDTORequest);
        });

        assertEquals(Message.EXIST_PHONE, exception.getMessage());
    }


    @BeforeEach
    void setUpUpdate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        customerDTORequest = new CustomerDTORequest();
        customerDTORequest.setId(1L);
        customerDTORequest.setCustomerName("Vũ Văn Hà");
        customerDTORequest.setPhoneNumber("0987654321");

        customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");
    }

    // Test trường hợp cập nhật khách hàng thành công
    @Test
    void testUpdateCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByPhoneNumber("0987654321")).thenReturn(Optional.empty());

        customerService.updateCustomer(customerDTORequest);

        verify(customerRepository).save(customer);
        assertEquals("Vũ Văn Hà", customer.getCustomerName());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì không tìm thấy khách hàng
    @Test
    void testUpdateCustomer_CustomerNotFound() {
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
    void testUpdateCustomer_InvalidCustomerName() {
        customerDTORequest.setCustomerName("Vũ Văn Hà@");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_NAME, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì trường customerName bị null
    @Test
    void testUpdateCustomer_NullCustomerName() {
        customerDTORequest.setCustomerName("");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null customer name");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì trường phoneNumber không hợp lệ
    @Test
    void testUpdateCustomer_InvalidPhoneNumber() {
        customerDTORequest.setPhoneNumber("0987654321a");

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for invalid phone number");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.INVALID_PHONE, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì trường phoneNumber bị null
    @Test
    void testUpdateCustomer_NullPhoneNumber() {
        customerDTORequest.setPhoneNumber(null);

        Set<ConstraintViolation<CustomerDTORequest>> violations = validator.validate(customerDTORequest);

        assertFalse(violations.isEmpty(), "Expected validation errors for null phone number");

        ConstraintViolation<CustomerDTORequest> violation = violations.iterator().next();
        assertEquals(Message.NULL_FILED, violation.getMessage());
    }

    // Test trường hợp cập nhật khách hàng không thành công vì số điện thoại đã tồn tại
    @Test
    void testUpdateCustomer_PhoneNumberExist() {
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

    @BeforeEach
    void setUpGetCustomerById() {
        MockitoAnnotations.openMocks(this);

        customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");

        // Khởi tạo saleOrderList và thêm một số đối tượng SaleOrder
        SaleOrder order1 = new SaleOrder();
        order1.setTotalAmount(100.0);
        SaleOrder order2 = new SaleOrder();
        order2.setTotalAmount(150.0);
        customer.setSaleOrderList(List.of(order1, order2)); // Đặt danh sách với dữ liệu mẫu
    }

    // Test trường hợp lấy thông tin khách hàng thành công
    @Test
    void getCustomerById_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDTOResponse response = customerService.getCustomerById(1L);

        assertNotNull(response);
        assertEquals(customer.getId(), response.getId());
        assertEquals(customer.getCustomerName(), response.getCustomerName());
        assertEquals(customer.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(customer.getSaleOrderList().stream().mapToDouble(saleOrder -> saleOrder.getTotalAmount()).sum(), response.getTotalAmount());
    }

    // Test trường hợp lấy thông tin khách hàng không thành công vì không tìm thấy khách hàng
    @Test
    void getCustomerById_CustomerNotFound() {
        when(customerRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(200L);
        });

        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }

    //Test trường hợp xóa khách hàng thành công
    @Test
    void deleteCustomer_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).save(customer);
        assertTrue(customer.isDeleted());
    }

    // Test trường hợp xóa khách hàng không thành công vì không tìm thấy khách hàng
    @Test
    void deleteCustomer_CustomerNotFound() {
        when(customerRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomer(200L);
        });

        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }
}
