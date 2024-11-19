package com.fu.pha.dto.response;

import com.fu.pha.dto.response.SaleOrder.SaleOrderResponseDto;
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
    private String invoiceNumber;
    private SaleOrderResponseDto saleOrder;
    private CustomerDTOResponse customer;
    private Double refundAmount;
    private Instant returnDate;
    private String returnReason;
    private List<ReturnOrderItemResponseDto> returnOrderItems;

    public ReturnOrderResponseDto(ReturnOrder returnOrder) {
        this.id = returnOrder.getId();
        this.invoiceNumber = returnOrder.getInvoiceNumber();
        this.saleOrder = returnOrder.getSaleOrder() != null ? new SaleOrderResponseDto(returnOrder.getSaleOrder()) : null;        this.refundAmount = returnOrder.getRefundAmount();
        this.customer = returnOrder.getCustomer() != null ? new CustomerDTOResponse(returnOrder.getCustomer()) : null;
        this.returnDate = returnOrder.getReturnDate();
        this.returnReason = returnOrder.getReturnReason();
        this.returnOrderItems = returnOrder.getReturnOrderItems().stream()
                .map(ReturnOrderItemResponseDto::new)
                .collect(Collectors.toList());
    }
}
