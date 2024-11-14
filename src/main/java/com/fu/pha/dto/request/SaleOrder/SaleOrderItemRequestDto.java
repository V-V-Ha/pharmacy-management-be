package com.fu.pha.dto.request.SaleOrder;

import com.fu.pha.validate.anotation.ValidDiscount;
import com.fu.pha.validate.anotation.ValidTotalAmount;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SaleOrderItemRequestDto {

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;
    @NotNull(message = "Số lượng không được để trống")
    private Integer quantity;
    @DecimalMin(value = "0.01", message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;
    @NotNull(message = "Đơn vị không được để trống")
    private String unit;
    @ValidDiscount
    private Double discount;
    @ValidTotalAmount
    private Double totalAmount;
    private String batchNumber;
    private String dosage;
    @NotNull(message = "Hệ số quy đổi không được để trống")
    @Min(value = 1, message = "Hệ số quy đổi phải lớn hơn 0")
    private Integer conversionFactor;
}
