package com.fu.pha.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerReportDto {
    private long newCustomers;
    private long oldCustomers;
    private long totalCustomers;
    private long walkInCustomers;
    private double totalRevenueFromCustomers;
    private int totalQuantitySoldToCustomers;
}
