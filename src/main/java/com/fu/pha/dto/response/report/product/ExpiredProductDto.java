package com.fu.pha.dto.response.report.product;

import java.time.Instant;

public interface ExpiredProductDto {
    Long getProductId();
    String getProductCode();
    String getProductName();
    String getCategoryName();
    String getUnitName();
    String getBatchNumber();
    Instant getExpiryDate();
    Integer getDaysRemaining();
}
