package com.example.spendz.Model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private List<AllTransactionResponse> allTransactions;
    private List<CategoryWiseResponseData> categoryWiseResponseData;

    private double totalSpend;
    private double totalSpendPercentOfSalary;
    private double netSpend;
    private double netSpendPercentOfSalary;
    private double investment;
    private double savingAndInvestment;
    private double totalSavingPercentOfSalary;

    private double salary;
    private double returns;
    private double totalIncome;
    private double miscIncome;
}
