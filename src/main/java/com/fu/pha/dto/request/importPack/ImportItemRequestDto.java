package com.fu.pha.dto.request.importPack;


import com.fu.pha.validate.anotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
public class ImportItemRequestDto {
    private Long id;

    @ValidQuantity
    private Integer quantity;

    @ValidUnitPrice
    private Double unitPrice;

    @NotBlank(message = "Đơn vị không được để trống")
    private String unit;

    @ValidDiscount
    private Double discount;

    @ValidVat
    private Double tax;

    @ValidTotalAmount
    private Double totalAmount;
    @NotBlank(message = "Số lô không được để trống")
    private String batchNumber;
    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;
    @NotNull(message = "Ngày hết hạn không được để trống")
    private Instant expiryDate;
    @NotNull(message = "Hệ số quy đổi không được để trống")
    @Min(value = 1, message = "Hệ số quy đổi phải lớn hơn 0")
    private Integer conversionFactor;

}
