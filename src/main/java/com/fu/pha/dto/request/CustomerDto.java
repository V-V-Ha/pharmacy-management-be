package com.fu.pha.dto.request;

import com.fu.pha.entity.Customer;
import com.fu.pha.enums.Gender;
import com.fu.pha.exception.Message;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;
    @NotNull(message = Message.NULL_FILED)
    private String customerName;

    private String address;

    private Gender gender;

    private Integer yob;

    public CustomerDto(Customer customer) {
        this.id = customer.getId();
        this.customerName = customer.getCustomerName();
        this.address = customer.getAddress();
        this.gender = customer.getGender();
        this.yob = customer.getYob();
    }
}
