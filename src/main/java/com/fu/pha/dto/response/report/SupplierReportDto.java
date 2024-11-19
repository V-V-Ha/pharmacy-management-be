package com.fu.pha.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupplierReportDto {
    private long newSuppliers;
    private Double newSuppliersAmount;
    private long oldSuppliers;
    private Double oldSuppliersAmount;
    private long totalSuppliers;
    private Double totalImportAmount;
    private int totalImportQuantity;
}
