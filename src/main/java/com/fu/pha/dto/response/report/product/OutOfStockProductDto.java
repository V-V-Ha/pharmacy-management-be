package com.fu.pha.dto.response.report.product;

public interface OutOfStockProductDto {
    Long getProductId();
    String getProductCode();
    String getProductName();
    String getCategoryName();
    String getUnitName();
    Integer getNumberWarning();
    Integer getTotalQuantity();
}
