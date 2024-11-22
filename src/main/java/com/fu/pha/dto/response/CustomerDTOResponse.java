package com.fu.pha.dto.response;


import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.enums.Gender;
import com.fu.pha.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTOResponse {

    private Long id;
    private String customerName;
    private String address;
    private String phoneNumber;
    private Double totalAmount;
    private Integer yob;
    private Gender gender;
    private Status status;

    public CustomerDTOResponse(Customer customer) {
        this.id = customer.getId();
        this.customerName = customer.getCustomerName();
        this.address = customer.getAddress();
        this.phoneNumber = customer.getPhoneNumber();
        this.yob = customer.getYob();
        if (customer.getSaleOrderList() != null) {
            this.totalAmount = customer.getSaleOrderList().stream()
                    .filter(Objects::nonNull)
                    .mapToDouble(saleOrder -> saleOrder.getTotalAmount() != null ? saleOrder.getTotalAmount() : 0.0)
                    .sum();
        } else {
            this.totalAmount = 0.0;
        }
        this.gender = customer.getGender();
        this.status = customer.getStatus();
    }

    public CustomerDTOResponse(Long id, String customerName) {
        this.id = id;
        this.customerName = customerName;
    }
}
