package com.fu.pha.dto.response;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;

@Data
public class ImportItemResponseDto {
    private Long id;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.01", message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;

    private Long unitId;

    @DecimalMin(value = "0", message = "Chiết khấu phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100", message = "Chiết khấu phải nhỏ hơn hoặc bằng 100")
    private Double discount;

    @DecimalMin(value = "0", message = "Thuế phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100", message = "Thuế phải nhỏ hơn hoặc bằng 100")
    private Double tax;

    private Double totalAmount;
    private String batchNumber;
    private Long productId;
    private Long importId;
    private Instant createDate;
    private Instant lastModifiedDate;
    private Instant expiryDate;
    private String createBy;
    private String lastModifiedBy;





}

