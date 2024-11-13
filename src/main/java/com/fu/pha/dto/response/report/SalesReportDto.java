package com.fu.pha.dto.response.report;

import com.fu.pha.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDto {
    private long totalInvoices;
    private double totalRevenue;
    private int totalQuantitySold;
    private Map<PaymentMethod, Double> revenueByPaymentMethod;
}
