package com.fu.pha.dto.response.SaleOrder;

import com.fu.pha.dto.request.SaleOrder.SaleOrderItemRequestDto;
import com.fu.pha.dto.response.CustomerDTOResponse;
import com.fu.pha.dto.response.DoctorDTOResponse;
import com.fu.pha.entity.SaleOrder;
import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
@Data
@NoArgsConstructor
public class SaleOrderResponseDto {
    private String invoiceNumber;
    private Instant saleDate;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private Double discount;
    private Double totalAmount;
    private CustomerDTOResponse customer;
    private DoctorDTOResponse doctor;
    private Long userId;
    private String diagnosis;
    private List<SaleOrderItemResponseDto> saleOrderItems;

    public SaleOrderResponseDto(String invoiceNumber, Instant saleDate, OrderType orderType, PaymentMethod paymentMethod, Double discount, Double totalAmount, CustomerDTOResponse customer, DoctorDTOResponse doctor, Long userId, String diagnosis, List<SaleOrderItemResponseDto> saleOrderItems) {
        this.invoiceNumber = invoiceNumber;
        this.saleDate = saleDate;
        this.orderType = orderType;
        this.paymentMethod = paymentMethod;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.customer = customer;
        this.doctor = doctor;
        this.userId = userId;
        this.diagnosis = diagnosis;
        this.saleOrderItems = saleOrderItems;
    }



}
