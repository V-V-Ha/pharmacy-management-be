package com.fu.pha.service.impl;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.request.CustomerDto;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.Category;
import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.BadRequestException;
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

import java.time.Year;
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

        int yob = customerDTORequest.getYob();

        // Validate Year of Birth (should be greater than 1900 and not in the future)
        if (yob <= 1900 || yob > Year.now().getValue()) {
            throw new BadRequestException(Message.INVALID_YOB);
        }

        Customer customer = new Customer();
        customer.setPhoneNumber(customerDTORequest.getPhoneNumber());
        customer.setCustomerName(customerDTORequest.getCustomerName());
        customer.setAddress(customerDTORequest.getAddress());
        customer.setYob(yob);
        customer.setGender(customerDTORequest.getGender());
        customer.setStatus(Status.ACTIVE);
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

        int yob = customerDTORequest.getYob();

        // Validate Year of Birth (should be greater than 1900 and not in the future)
        if (yob <= 1900 || yob > Year.now().getValue()) {
            throw new BadRequestException(Message.INVALID_YOB);
        }

        Customer customer = customerOptional.get();
        customer.setPhoneNumber(customerDTORequest.getPhoneNumber());
        customer.setCustomerName(customerDTORequest.getCustomerName());
        customer.setAddress(customerDTORequest.getAddress());
        customer.setYob(yob);
        customer.setGender(customerDTORequest.getGender());
        customerRepository.save(customer);
    }

   @Override
    public void updateCustomerStatus(Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }

        // Chuyển đổi trạng thái
        Customer customer = customerOptional.get();
        if (customer.getStatus() == Status.ACTIVE) {
            customer.setStatus(Status.INACTIVE);
        } else {
            customer.setStatus(Status.ACTIVE);
        }
        customerRepository.save(customer);
    }

    @Override
    public CustomerDTOResponse getCustomerById(Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }
        return new CustomerDTOResponse(customerOptional.get());
    }

    @Override
    public List<CustomerDTOResponse> getCustomerByCustomerName(String customerName) {
        Optional<List<CustomerDTOResponse>> customers = customerRepository.findByCustomerName(customerName);
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }

        return customers.get();
    }

    @Override
    public Page<CustomerDTOResponse> getAllCustomerByPaging(int size, int index, String phoneNumber, String status) {
        Pageable pageable = PageRequest.of(size, index);
        Status customerStatus = null;
        if (status != null) {
            try {
                customerStatus = Status.valueOf(status.toUpperCase());
            } catch (Exception e) {
                throw new ResourceNotFoundException(Message.STATUS_NOT_FOUND);
            }
        }
        Page<CustomerDTOResponse> customerDTOResponses = customerRepository.getListCustomerPaging(phoneNumber, customerStatus, pageable);
        if (customerDTOResponses.isEmpty()) {
            throw new ResourceNotFoundException(Message.CUSTOMER_NOT_FOUND);
        }
        return customerDTOResponses;
    }

    private void checkValidateCustomer(CustomerDTORequest customerDTORequest) {

        if(customerDTORequest.getCustomerName().isEmpty() || customerDTORequest.getPhoneNumber().isEmpty()) {
            throw new BadRequestException(Message.NULL_FILED);
        }

       // if ()

        int yob = customerDTORequest.getYob();

        // Validate Year of Birth (should be greater than 1900 and not in the future)
        if (yob <= 1900 || yob > Year.now().getValue()) {
            throw new BadRequestException(Message.INVALID_YOB);
        }
    }

}
