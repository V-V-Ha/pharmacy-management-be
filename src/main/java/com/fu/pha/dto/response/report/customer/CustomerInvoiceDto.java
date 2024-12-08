package com.fu.pha.dto.response.report.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInvoiceDto {
    private Long customerId;
    private String customerName;
    private String phoneNumber;
    private Long invoiceCount; // Số lượng hóa đơn
    private Long totalProductQuantity; // Tổng số lượng sản phẩm
    private Double totalAmount; // Tổng tiền

}
