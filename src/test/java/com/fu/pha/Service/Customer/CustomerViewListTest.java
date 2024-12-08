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
import java.util.Collections;
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

    // Test case: Found customers
    @Test
    public void testGetAllCustomerByPaging_FoundCustomers() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(anyString(), any(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, "0987654321", "ACTIVE");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllCustomerByPaging_NoCustomersFound() {
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(customerRepository.getListCustomerPaging(anyString(), any(), any(Pageable.class)))
                .thenReturn(customerPage);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getAllCustomerByPaging(0, 10, "keyword", "ACTIVE");
        });

        assertEquals(Message.CUSTOMER_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllCustomerByPaging_InvalidStatus() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getAllCustomerByPaging(0, 10, "keyword", "INVALID_STATUS");
        });

        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllCustomerByPaging_StatusNull() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(anyString(), isNull(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, "keyword", null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllCustomerByPaging_PhoneNumberNull() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(isNull(), any(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, null, "ACTIVE");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllCustomerByPaging_PhoneNumberEmpty() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(eq(""), any(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, "", "ACTIVE");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllCustomerByPaging_StatusEmpty() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getAllCustomerByPaging(0, 10, "keyword", "");
        });

        assertEquals(Message.STATUS_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void testGetAllCustomerByPaging_PhoneNumberAndStatusNull() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllCustomerByPaging_ValidPage() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(anyString(), any(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, "keyword", "ACTIVE");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }
}
