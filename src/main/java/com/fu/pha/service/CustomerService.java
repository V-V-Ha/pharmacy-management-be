package com.fu.pha.service;

import com.fu.pha.dto.request.CustomerDto;
import org.springframework.data.domain.Page;

public interface CustomerService {
    void createCustomer(CustomerDto customerDto);

    void updateCustomer(CustomerDto customerDto);

    void deleteCustomer(Long id);

    CustomerDto getCustomerById(Long id);

    Page<CustomerDto> getAllCustomerByPaging(int size, int index, String customerName, String phoneNumber);
}
