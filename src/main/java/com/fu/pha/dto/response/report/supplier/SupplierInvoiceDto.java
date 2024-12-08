package com.fu.pha.dto.response.report.supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupplierInvoiceDto {
    private Long supplierId;
    private String supplierName;
    private String phoneNumber;
    private Long invoiceCount; // Tổng số hóa đơn nhập hàng
    private Long totalProductQuantity; // Tổng số sản phẩm nhập
    private Double totalReturnAmount; // Tổng tiền trả hàng
    private Double totalImportAmount; // Tổng tiền nhập hàng
}
