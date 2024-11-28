package com.fu.pha.Service.Customer;
import com.fu.pha.dto.response.CustomerDTOResponse;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerViewListTest {


    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    public void testGetAllCustomerByPaging_FoundCustomers() {
    }

    @Test
    public void testGetAllCustomerByPaging_NoCustomersFound() {

    }

    @Test
    public void testGetAllCustomerByPaging_InvalidStatus() {

    }

    @Test
    public void testGetAllCustomerByPaging_StatusNull() {

    }

    @Test
    public void testGetAllCustomerByPaging_PhoneNumberNull() {

    }

    @Test
    public void testGetAllCustomerByPaging_PhoneNumberEmpty() {

    }

    @Test
    public void testGetAllCustomerByPaging_StatusEmpty() {

    }

    @Test
    public void testGetAllCustomerByPaging_PhoneNumberAndStatusNull() {

    }

    @Test
    void testGetAllCustomerByPaging_ValidPage() {

    }
}
