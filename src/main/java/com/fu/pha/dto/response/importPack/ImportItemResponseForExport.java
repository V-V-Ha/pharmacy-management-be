package com.fu.pha.dto.response.importPack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private Instant importDate;
    private Integer conversionFactor;
    private Integer remainingQuantity;
    private String invoiceNumber;
    private Long supplierId;


    public ImportItemResponseForExport(Long id ,String invoiceNumber,Integer quantity, Double unitPrice, String unit, Double discount,
                                       Double tax, Double totalAmount, String batchNumber,
                                       Long importId, Instant expiryDate,Instant importDate, Integer remainingQuantity,Long supplierId) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.batchNumber = batchNumber;
        this.importId = importId;
        this.expiryDate = expiryDate;
        this.importDate = importDate;
        this.remainingQuantity = remainingQuantity;
        this.supplierId = supplierId;
    }


    public ImportItemResponseForExport(Long id, String invoiceNumber,Integer quantity, Double unitPrice, String unit, Double discount, Double tax, Double totalAmount, String batchNumber, Instant expiryDate, Instant importDate,Integer remainingQuantity) {
        this.id = id;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.importDate = importDate;
        this.invoiceNumber = invoiceNumber;
        this.remainingQuantity = remainingQuantity;
    }
}
