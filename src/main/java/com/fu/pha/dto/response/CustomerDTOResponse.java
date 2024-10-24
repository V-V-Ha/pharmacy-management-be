package com.fu.pha.dto.response;


import com.fu.pha.entity.Customer;
import com.fu.pha.entity.SaleOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String gender;

    public CustomerDTOResponse(Customer customer){
        this.id = customer.getId();
        this.customerName = customer.getCustomerName();
        this.address = customer.getAddress();
        this.phoneNumber = customer.getPhoneNumber();
        this.yob = customer.getYob();
        this.totalAmount = customer.getSaleOrderList().stream()
                .mapToDouble(SaleOrder::getTotalAmount).sum();
    }
}
