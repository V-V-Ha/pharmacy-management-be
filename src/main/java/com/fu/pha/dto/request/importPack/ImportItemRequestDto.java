package com.fu.pha.dto.request.importPack;

import com.fu.pha.dto.request.ProductUnitDTORequest;
import com.fu.pha.dto.response.ProductDTOResponse;
import com.fu.pha.entity.ImportItem;
import com.fu.pha.exception.Message;
import com.fu.pha.validate.anotation.ValidDiscount;
import com.fu.pha.validate.anotation.ValidQuantity;
import com.fu.pha.validate.anotation.ValidTax;
import com.fu.pha.validate.anotation.ValidUnitPrice;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class ImportItemRequestDto {
    private Long id;

    @ValidQuantity
    private Integer quantity;

    @ValidUnitPrice
    private Double unitPrice;

    private String unit;

    @ValidDiscount
    private Double discount;

    @ValidTax
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

    private Integer conversionFactor;

    private Integer remainingQuantity;

    public ImportItemRequestDto(ImportItem importItem) {
        this.id = importItem.getId();
        this.quantity = importItem.getQuantity();
        this.unitPrice = importItem.getUnitPrice();
        this.unit = importItem.getUnit();
        this.discount = importItem.getDiscount();
        this.tax = importItem.getTax();
        this.batchNumber = importItem.getBatchNumber();
        this.expiryDate = importItem.getExpiryDate();
        this.totalAmount = importItem.getTotalAmount();
        this.productId = importItem.getProduct().getId();
        this.importId = importItem.getImportReceipt().getId();
        this.createDate = importItem.getCreateDate();
        this.lastModifiedDate = importItem.getLastModifiedDate();
        this.createBy = importItem.getCreateBy();
        this.lastModifiedBy = importItem.getLastModifiedBy();
        this.remainingQuantity = importItem.getRemainingQuantity();
    }
}
