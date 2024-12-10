package com.fu.pha.dto.request;

import com.fu.pha.validate.anotation.ValidReturnOrder;
import com.fu.pha.validate.anotation.ValidUnitPrice;
import jakarta.validation.constraints.Min;
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
public class ReturnOrderItemRequestDto {
    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;
    private Integer quantity;
    @ValidUnitPrice
    private Double unitPrice;
    @NotNull(message = "Đơn vị không được để trống")
    private String unit;
    @NotNull(message = "Hệ số quy đổi không được để trống")
    @Min(value = 1, message = "Hệ số quy đổi phải lớn hơn 0")
    private Integer conversionFactor;
    private List<ReturnOrderBatchRequestDto> batchRequestDtos;
}
