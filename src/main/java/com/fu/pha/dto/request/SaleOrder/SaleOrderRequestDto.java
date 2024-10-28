package com.fu.pha.dto.request.SaleOrder;

import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class SaleOrderRequestDto {
    private String invoiceNumber;
    private Instant saleDate;
    private OrderType orderType;
    private PaymentStatus paymentMethod;
    private Double discount;
    private Double totalAmount;
    private Long customerId;
    private Long doctorId;
    private Long userId;
    private List<SaleOrderItemRequestDto> saleOrderItems;
}
