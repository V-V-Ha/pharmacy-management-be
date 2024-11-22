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
    private Double amountNewCustomers;
    private long oldCustomers;
    private Double amountOldCustomers;
    private long walkInCustomers;
    private Double amountWalkInCustomers;
    private long totalCustomers;
    private double totalRevenueFromCustomers;
    private int totalQuantitySoldToCustomers;
}
