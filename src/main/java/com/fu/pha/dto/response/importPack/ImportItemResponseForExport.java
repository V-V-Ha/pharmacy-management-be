package com.fu.pha.dto.response.importPack;

import com.fu.pha.dto.response.ProductUnitDTOResponse;
import com.fu.pha.dto.response.exportSlip.BatchInfo;
import com.fu.pha.entity.ImportItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportItemResponseForExport {
    private Long id;
    private Integer quantity;
    private Double unitPrice;
    private String unit;
    private Double discount;
    private Double tax;
    private Double totalAmount;
    private String batchNumber;
    private String productName;
    private Long importId;
    private Instant expiryDate;
    private Integer conversionFactor;
    private Integer remainingQuantity;


    public ImportItemResponseForExport(Long id, Integer quantity, Double unitPrice, String unit, Double discount,
                                       Double tax, Double totalAmount, String batchNumber,
                                       Long importId, Instant expiryDate, Integer remainingQuantity) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.batchNumber = batchNumber;
        this.importId = importId;
        this.expiryDate = expiryDate;
        this.remainingQuantity = remainingQuantity;
    }


    public ImportItemResponseForExport(Long id, Integer quantity, Double unitPrice, String unit, Double discount, Double tax, Double totalAmount, String batchNumber, Instant expiryDate, Integer remainingQuantity) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.remainingQuantity = remainingQuantity;
    }
}
