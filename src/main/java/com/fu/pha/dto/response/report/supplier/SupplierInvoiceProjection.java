package com.fu.pha.dto.response.report.supplier;

public interface SupplierInvoiceProjection {
    Long getSupplierId();
    String getSupplierName();
    String getPhoneNumber();
    Long getInvoiceCount(); // Tổng số hóa đơn nhập hàng
    Long getTotalProductQuantity(); // Tổng số sản phẩm nhập
    Double getTotalReturnAmount(); // Tổng tiền trả hàng
    Double getTotalImportAmount(); // Tổng tiền nhập hàng
}
