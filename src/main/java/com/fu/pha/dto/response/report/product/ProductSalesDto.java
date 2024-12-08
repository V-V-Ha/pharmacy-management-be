package com.fu.pha.dto.response.report.product;

public interface ProductSalesDto {
    String getProductCode();
    String getProductName();
    String getUnit();
    Integer getTransactionCount();
    Integer getQuantitySold();
    Double getTotalAmount();
}
