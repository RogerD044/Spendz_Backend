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
    private double totalIncome;
    private double miscIncome;
    private double investment;
}
