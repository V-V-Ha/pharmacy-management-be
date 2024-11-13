package com.fu.pha.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FinancialReportDto {
    private double totalIncome;
    private double totalExpense;
    private double profit;
    private Map<String, Double> incomeBySource;
    private Map<String, Double> expenseBySource;
    private Map<String, Integer> quantityBySource;
}
