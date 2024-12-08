package com.fu.pha.dto.response.report.sale;

import java.time.Instant;

public interface SalesTransactionDto {
    String getInvoiceNumber();
    Instant getCreationDate();
    String getCustomerName();
    String getVoucherType();
    String getPaymentMethod();
    Double getTotalAmount();
}
