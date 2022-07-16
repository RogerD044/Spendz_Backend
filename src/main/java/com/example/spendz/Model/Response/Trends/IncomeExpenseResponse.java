package com.example.spendz.Model.Response.Trends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class IncomeExpenseResponse {
    private Date startDate;
    private String label;

    // Salary Category
    private double fixedIncome;
    // Others Category
    private double passiveIncome;
    private double miscIncome;

    private double totalSpend;
}
