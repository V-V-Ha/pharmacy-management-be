package com.fu.pha.service.impl;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.request.CustomerDto;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.exception.Message;
import com.fu.pha.exception.ResourceNotFoundException;
import com.fu.pha.repository.CustomerRepository;
import com.fu.pha.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    @Override
    public void createCustomer(CustomerDTORequest customerDTORequest) {

        // Check exist phone number
        Optional<Customer> customerOptional = customerRepository.findByPhoneNumber(customerDTORequest.getPhoneNumber());
        if (customerOptional.isPresent()) {
            throw new ResourceNotFoundException(Message.EXIST_PHONE);
        }

        Customer customer = new Customer();
        customer.setPhoneNumber(customerDTORequest.getPhoneNumber());
        customer.setCustomerName(customerDTORequest.getCustomerName());
        customer.setAddress(customerDTORequest.getAddress());
        customer.setYob(customerDTORequest.getYob());
        customer.setGender(customerDTORequest.getGender());
        customerRepository.save(customer);
    }


    @Transactional
    @Override
    public void updateCustomer(CustomerDTORequest customerDTORequest) {
        Optional<Customer> customerOptional = customerRepository.findById(customerDTORequest.getId());
        if (customerOptional.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }

        // Check exist phone number
        Optional<Customer> customerOptionalByPhone = customerRepository.findByPhoneNumber(customerDTORequest.getPhoneNumber());
        if (customerOptionalByPhone.isPresent() && !customerOptionalByPhone.get().getId().equals(customerDTORequest.getId())) {
            throw new ResourceNotFoundException(Message.EXIST_PHONE);
        }

        Customer customer = customerOptional.get();
        customer.setPhoneNumber(customerDTORequest.getPhoneNumber());
        customer.setCustomerName(customerDTORequest.getCustomerName());
        customer.setAddress(customerDTORequest.getAddress());
        customer.setYob(customerDTORequest.getYob());
        customer.setGender(customerDTORequest.getGender());
        customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }
        customerOptional.get().setDeleted(true);
        customerRepository.save(customerOptional.get());
    }

    @Override
    public CustomerDTOResponse getCustomerById(Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }

        CustomerDTOResponse customerDTOResponse = new CustomerDTOResponse();
        customerDTOResponse.setId(customerOptional.get().getId());
        customerDTOResponse.setCustomerName(customerOptional.get().getCustomerName());
        customerDTOResponse.setAddress(customerOptional.get().getAddress());
        customerDTOResponse.setPhoneNumber(customerOptional.get().getPhoneNumber());
        customerDTOResponse.setYob(customerOptional.get().getYob());
        customerDTOResponse.setGender(customerOptional.get().getGender());
        customerDTOResponse.setTotalAmount(customerOptional.get().getSaleOrderList().stream()
                .mapToDouble(SaleOrder::getTotalAmount).sum());
        return customerDTOResponse;
    }

    @Override
    public List<CustomerDTOResponse> getCustomerByCustomerName(String customerName) {
        List<Customer> customers = customerRepository.findByCustomerName(customerName);
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }

        List<CustomerDTOResponse> customerDTOResponses = customers.stream().map(customer -> {
            CustomerDTOResponse customerDTOResponse = new CustomerDTOResponse();
            customerDTOResponse.setId(customer.getId());
            customerDTOResponse.setCustomerName(customer.getCustomerName());
            customerDTOResponse.setAddress(customer.getAddress());
            customerDTOResponse.setPhoneNumber(customer.getPhoneNumber());
            customerDTOResponse.setYob(customer.getYob());
            customerDTOResponse.setGender(customer.getGender());
            customerDTOResponse.setTotalAmount(customer.getSaleOrderList().stream()
                    .mapToDouble(SaleOrder::getTotalAmount).sum());
            return customerDTOResponse;
        }).collect(Collectors.toList());

        return customerDTOResponses;
    }

    @Override
    public Page<CustomerDTOResponse> getAllCustomerByPaging(int size, int index, String phoneNumber) {
        Pageable pageable = PageRequest.of(size, index);
        Page<CustomerDTOResponse> customerDTOResponses = customerRepository.getListCustomerPaging(phoneNumber, pageable);
        if (customerDTOResponses.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }
        return customerDTOResponses;
    }

}
