package com.fu.pha.service;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.request.CustomerDto;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.enums.Status;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomerService {
    void createCustomer(CustomerDTORequest customerDTORequest);

    void updateCustomer(CustomerDTORequest customerDTORequest);

    void updateCustomerStatus(Long id);

    CustomerDTOResponse getCustomerById(Long id);

    Page<CustomerDTOResponse> getAllCustomerByPaging(int size, int index, String phoneNumber, String status);

    List<CustomerDTOResponse> getCustomerByCustomerName(String customerName);
}
