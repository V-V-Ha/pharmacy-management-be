package com.fu.pha.dto.response;

import com.fu.pha.entity.ImportItem;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class ImportItemResponseDto {
    private Long id;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.01", message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;

    private String unit;

    @DecimalMin(value = "0", message = "Chiết khấu phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100", message = "Chiết khấu phải nhỏ hơn hoặc bằng 100")
    private Double discount;

    @DecimalMin(value = "0", message = "Thuế phải lớn hơn hoặc bằng 0")
    @DecimalMax(value = "100", message = "Thuế phải nhỏ hơn hoặc bằng 100")
    private Double tax;

    private Double totalAmount;
    private String batchNumber;
    private ProductDTOResponse product;
    private Long importId;
    private Instant createDate;
    private Instant lastModifiedDate;
    private Instant expiryDate;
    private String createBy;
    private String lastModifiedBy;

    private Integer conversionFactor;

    public ImportItemResponseDto(ImportItem importItem) {
        this.id = importItem.getId();
        this.quantity = importItem.getQuantity();
        this.unitPrice = importItem.getUnitPrice();
        this.unit = importItem.getUnit();
        this.discount = importItem.getDiscount();
        this.tax = importItem.getTax();
        this.batchNumber = importItem.getBatchNumber();
        this.expiryDate = importItem.getExpiryDate();
        this.totalAmount = importItem.getTotalAmount();
        if (importItem.getProduct() != null) {
            this.product = new ProductDTOResponse(importItem.getProduct());
        }
        this.importId = importItem.getImportReceipt().getId();
        this.createDate = importItem.getCreateDate();
        this.lastModifiedDate = importItem.getLastModifiedDate();
        this.createBy = importItem.getCreateBy();
        this.lastModifiedBy = importItem.getLastModifiedBy();
    }
}

