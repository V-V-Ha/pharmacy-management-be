package com.fu.pha.dto.response.report;

import java.time.Instant;

public interface FinancialTransactionDto {
    String getInvoiceNumber();
    String getReceiptType(); // 'Phiếu thu' or 'Phiếu chi'
    Instant getCreationDate();
    String getCategory(); // 'Bán hàng', 'Nhập hàng', etc.
    String getPaymentMethod();
    Double getTotalAmount();

}
