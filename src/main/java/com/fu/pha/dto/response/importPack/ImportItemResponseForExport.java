package com.fu.pha.dto.response.importPack;

import com.fu.pha.dto.response.ProductUnitDTOResponse;
import com.fu.pha.entity.ImportItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class ImportItemResponseForExport {
    private Long id;
    private Integer quantity;
    private Double unitPrice;
    private String unit;
    private Double discount;
    private Double tax;
    private Double totalAmount;
    private String batchNumber;
    private String productName; // Thay đổi từ ProductDTOResponse thành String để chỉ lưu productName
    private Long importId;
    private Instant expiryDate;
    private Integer conversionFactor;
    private Integer remainingQuantity;
    private List<ProductUnitDTOResponse> productUnits;


    public ImportItemResponseForExport(Long id, Integer quantity, Double unitPrice, String unit, Double discount,
                                       Double tax, Double totalAmount, String batchNumber, String productName,
                                       Long importId, Instant expiryDate, Integer remainingQuantity,
                                       List<ProductUnitDTOResponse> productUnits) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.batchNumber = batchNumber;
        this.productName = productName;
        this.importId = importId;
        this.expiryDate = expiryDate;
        this.remainingQuantity = remainingQuantity;
        this.productUnits = productUnits;
    }

}
