package com.fu.pha.Service.Customer;
import com.fu.pha.dto.response.CustomerDTOResponse;
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

    //Test case: phone and Status is null
    @Test
    public void UTCCL08() {
        List<CustomerDTOResponse> customers = List.of(new CustomerDTOResponse());
        Page<CustomerDTOResponse> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.getListCustomerPaging(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(customerPage);

        Page<CustomerDTOResponse> result = customerService.getAllCustomerByPaging(0, 10, null, null);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

}
