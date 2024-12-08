package com.fu.pha.dto.response.report;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDto {
    private long totalInvoices;
    private double totalRevenue;
    private int totalQuantitySold;
    private Double cashRevenue;
    private Double transferRevenue;
}
