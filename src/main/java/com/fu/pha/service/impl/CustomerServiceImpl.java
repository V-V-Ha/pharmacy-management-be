package com.fu.pha.service.impl;

import com.fu.pha.dto.request.CustomerDto;
import com.fu.pha.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Override
    public void createCustomer(CustomerDto customerDto) {

    }

    @Override
    public void updateCustomer(CustomerDto customerDto) {

    }

    @Override
    public void deleteCustomer(Long id) {

    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        return null;
    }

    @Override
    public Page<CustomerDto> getAllCustomerByPaging(int size, int index, String customerName, String phoneNumber) {
        return null;
    }
}
