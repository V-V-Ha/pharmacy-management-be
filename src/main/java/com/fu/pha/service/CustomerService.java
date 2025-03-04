package com.fu.pha.service;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.response.CustomerDTOResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CustomerService {
    CustomerDTOResponse createCustomer(CustomerDTORequest customerDTORequest);

    void updateCustomer(CustomerDTORequest customerDTORequest);

    void updateCustomerStatus(Long id);

    CustomerDTOResponse getCustomerById(Long id);

    Page<CustomerDTOResponse> getAllCustomerByPaging(int size, int index, String keyword, String status);

    List<CustomerDTOResponse> getCustomerByCustomerName(String customerName);

    List<CustomerDTOResponse> findByPhoneNumber(String phoneNumber);
}
