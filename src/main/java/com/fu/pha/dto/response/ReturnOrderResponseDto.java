package com.fu.pha.dto.response;

import com.fu.pha.entity.ReturnOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderResponseDto {
    private Long id;
    private Long saleOrderId;
    private CustomerDTOResponse customer;
    private Double refundAmount;
    private Instant returnDate;
    private String returnReason;
    private List<ReturnOrderItemResponseDto> returnOrderItems;

    public ReturnOrderResponseDto(ReturnOrder returnOrder) {
        this.id = returnOrder.getId();
        this.saleOrderId = returnOrder.getSaleOrder().getId();
        this.refundAmount = returnOrder.getRefundAmount();
        this.customer = returnOrder.getCustomer() != null ? new CustomerDTOResponse(returnOrder.getCustomer()) : null;
        this.returnDate = returnOrder.getReturnDate();
        this.returnReason = returnOrder.getReturnReason();
        this.returnOrderItems = returnOrder.getReturnOrderItems().stream()
                .map(ReturnOrderItemResponseDto::new)
                .collect(Collectors.toList());
    }
}
