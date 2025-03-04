package com.fu.pha.dto.request.exportSlip;

import com.fu.pha.entity.ExportSlipItem;
import com.fu.pha.validate.anotation.ValidDiscount;
import com.fu.pha.validate.anotation.ValidQuantity;
import com.fu.pha.validate.anotation.ValidTotalAmount;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
public class ExportSlipItemRequestDto {
    private Long id;
    @ValidQuantity
    private Integer quantity;
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0", message = "Đơn giá phải lớn hơn hoặc bằng 0")
    private Double unitPrice;
    @NotNull(message = "Đơn vị không được để trống")
    private String unit;
    @ValidDiscount
    private Double discount;
    @NotNull(message = "Số lô không được để trống")
    private String batchNumber;
    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;
    private Long exportSlipId;
    @ValidTotalAmount
    private Double totalAmount;
    private Long importItemId;
    private String invoiceNumber;
    @NotNull(message = "Hệ số quy đổi không được để trống")
    @Min(value = 1, message = "Hệ số quy đổi phải lớn hơn 0")
    private Integer conversionFactor;
}
