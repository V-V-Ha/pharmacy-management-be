package com.fu.pha.controller;

import com.fu.pha.dto.request.CustomerDTORequest;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.PageResponseModel;
import com.fu.pha.enums.Status;
import com.fu.pha.exception.Message;
import com.fu.pha.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @GetMapping("/get-all-customer-paging")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('STOCK')")
    public ResponseEntity<PageResponseModel<CustomerDTOResponse>> getALlCustomerPaging(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size,
                                                                                       @RequestParam(required = false) String phoneNumber,
                                                                                       @RequestParam(required = false) String status) {
        Page<CustomerDTOResponse> customerDTOResponses = customerService.getAllCustomerByPaging(page, size, phoneNumber, status);
        PageResponseModel<CustomerDTOResponse> response = PageResponseModel.<CustomerDTOResponse>builder()
                .page(page)
                .size(size)
                .total(customerDTOResponses.getTotalElements())
                .listData(customerDTOResponses.getContent())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-customer")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<String> createCustomer(@Valid @RequestBody CustomerDTORequest request) {
        customerService.createCustomer(request);
        return ResponseEntity.ok(Message.CREATE_SUCCESS);
    }

    @PutMapping("/update-customer")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<String> updateCustomer(@Valid @RequestBody CustomerDTORequest request) {
        customerService.updateCustomer(request);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }

    @GetMapping("/get-customer-by-id")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<CustomerDTOResponse> getCustomerById(@RequestParam Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/change-status-customer")
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('SALE')")
    public ResponseEntity<String> updateCustomerStatus(@RequestParam Long id) {
        customerService.updateCustomerStatus(id);
        return ResponseEntity.ok(Message.UPDATE_SUCCESS);
    }
}
