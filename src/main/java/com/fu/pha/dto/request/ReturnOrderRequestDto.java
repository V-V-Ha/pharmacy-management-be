package com.fu.pha.dto.request;

import com.fu.pha.exception.Message;
import com.fu.pha.validate.anotation.ValidReturnOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ValidReturnOrder
public class ReturnOrderRequestDto {
    @NotNull(message = Message.SALE_ORDER_NOT_NULL)
    private Long saleOrderId;
    @Valid
    private List<ReturnOrderItemRequestDto> returnOrderItems;
    private String returnReason;
    private Double totalAmount;
}
