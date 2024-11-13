package com.fu.pha.dto.request.exportSlip;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ExportSlipItem;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    private Integer quantity;
    @DecimalMin(value = "0", message = "Đơn giá phải lớn hơn hoặc bằng 0")
    private Double unitPrice;
    @NotNull(message = "Đơn vị không được để trống")
    private String unit;
    @DecimalMin(value = "0", message = "Chiết khấu phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100", message = "Chiết khấu phải nhỏ hơn hoặc bằng 100")
    private Double discount;
    @NotNull(message = "Số lô không được để trống")
    private String batchNumber;
    private Instant expiryDate;
    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;
    private Long exportSlipId;
    @DecimalMin(value = "0", message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private Double totalAmount;
    private Long importItemId;
    private Integer conversionFactor;

    public ExportSlipItemRequestDto(ExportSlipItem exportSlipItem) {
        this.id = exportSlipItem.getId();
        this.quantity = exportSlipItem.getQuantity();
        this.unitPrice = exportSlipItem.getUnitPrice();
        this.unit = exportSlipItem.getUnit();
        this.discount = exportSlipItem.getDiscount();
        this.batchNumber = exportSlipItem.getBatch_number();
        this.expiryDate = exportSlipItem.getExpiryDate();
        this.productId = exportSlipItem.getProduct().getId();
        this.exportSlipId = exportSlipItem.getExportSlip().getId();
        this.totalAmount = exportSlipItem.getTotalAmount();
        this.importItemId = exportSlipItem.getImportItem().getId();
    }
}
