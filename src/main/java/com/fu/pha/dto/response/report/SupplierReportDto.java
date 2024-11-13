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
    private long oldSuppliers;
    private long totalSuppliers;
    private double totalImportAmount;
    private int totalImportQuantity;
}
