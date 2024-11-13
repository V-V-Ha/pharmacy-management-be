package com.fu.pha.dto.request.SaleOrder;

import com.fu.pha.enums.OrderType;
import com.fu.pha.enums.PaymentMethod;
import com.fu.pha.enums.PaymentStatus;
import com.fu.pha.exception.Message;
import com.fu.pha.validate.anotation.ValidTotalAmount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class SaleOrderRequestDto {
    private String invoiceNumber;

    @NotNull(message = Message.DATE_NOT_NULL)
    private Instant saleDate;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private Double discount;

    @ValidTotalAmount
    private Double totalAmount;

    @NotNull(message = Message.CUSTOMER_NOT_NULL)
    private Long customerId;
    private Long doctorId;

    @NotNull(message = Message.USER_NOT_NULL)
    private Long userId;
    private String diagnosis;

    @NotNull(message = Message.LIST_ITEM_NOT_NULL)
    @Valid
    private List<SaleOrderItemRequestDto> saleOrderItems;
}
