package com.fu.pha.dto.request;

import com.fu.pha.exception.Message;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnOrderItemRequestDto {
    @NotNull
    private Long productId;
    @NotNull
    private Integer quantity;
    @NotNull
    private Double unitPrice;
    @NotNull
    private String unit;
    @NotNull
    private Integer conversionFactor;
}
