package com.fu.pha.dto.response.report.customer;

public interface CustomerInvoiceProjection {
    Long getCustomerId();
    String getCustomerName();
    String getPhoneNumber();
    Long getInvoiceCount();
    Long getTotalProductQuantity();
    Double getTotalAmount();
}
