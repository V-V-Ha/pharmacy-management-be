package com.fu.pha.Service.Customer;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.entity.Customer;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CustomerRepository;
import com.fu.pha.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerChangeStatusTest {


    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;
    private CustomerDTORequest customerDTORequest;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Khởi tạo mock
        MockitoAnnotations.initMocks(this);

        // Khởi tạo đối tượng customer giả định
        customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Vũ Văn Hà");
        customer.setPhoneNumber("0987654321");
        customer.setStatus(Status.ACTIVE); // Giả định ban đầu là chưa bị xóa
    }

    //Test trường hợp change status khách hàng thành công
    @Test
    void UTCCCS01() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.updateCustomerStatus(1L);

        verify(customerRepository).save(customer);
        //assertTrue(customer.isDeleted());
    }

    // Test trường hợp change status khách hàng không thành công vì không tìm thấy khách hàng
    @Test
    void UTCCCS02() {
        when(customerRepository.findById(200L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomerStatus(200L);
        });

        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }

}
