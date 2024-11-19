package com.fu.pha.dto.response.importPack;

import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ImportItem;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class ImportItemResponseDto {
    private Long id;

    private Integer quantity;

    private Double unitPrice;

    private String unit;

    private Double discount;
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

    private Integer remainingQuantity;

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
        this.conversionFactor = importItem.getConversionFactor();
        if (importItem.getProduct() != null) {
            this.product = new ProductDTOResponse(importItem.getProduct());
        }
        this.importId = importItem.getImportReceipt().getId();
        this.createDate = importItem.getCreateDate();
        this.lastModifiedDate = importItem.getLastModifiedDate();
        this.createBy = importItem.getCreateBy();
        this.lastModifiedBy = importItem.getLastModifiedBy();
        this.remainingQuantity = importItem.getRemainingQuantity();
    }

}

