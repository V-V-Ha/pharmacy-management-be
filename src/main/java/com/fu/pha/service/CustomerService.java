package com.fu.pha.service;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.request.CustomerDto;
import com.fu.pha.dto.response.CustomerDTOResponse;
import org.springframework.data.domain.Page;

public interface CustomerService {
    void createCustomer(CustomerDTORequest customerDTORequest);

    void updateCustomer(CustomerDTORequest customerDTORequest);

    void deleteCustomer(Long id);

    CustomerDto getCustomerById(Long id);

    Page<CustomerDTOResponse> getAllCustomerByPaging(int size, int index, String phoneNumber);
}
