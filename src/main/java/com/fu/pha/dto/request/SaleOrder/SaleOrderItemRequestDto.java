package com.fu.pha.dto.request.SaleOrder;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SaleOrderItemRequestDto {

    private Long productId;
    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;
    @DecimalMin(value = "0.01", message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;
    private String unit;
    @DecimalMin(value = "0", message = "Chiết khấu phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100", message = "Chiết khấu phải nhỏ hơn hoặc bằng 100")
    private Double discount;
    @DecimalMin(value = "0", message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private Double totalAmount;
    private String batchNumber;
    private String dosage;
    private Integer conversionFactor;
}
