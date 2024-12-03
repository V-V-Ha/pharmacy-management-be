package com.fu.pha.dto.response;

import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaleOrderForReturnDto {
    private Long id;
    private String invoiceNumber;
    private Instant saleDate;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private Double discount;
    private Double totalAmount;
    private CustomerDTOResponse customer;
    private DoctorDTOResponse doctor;
    private Long userId;
    private List<SaleOrderItemForReturnDto> saleOrderItems;

}
