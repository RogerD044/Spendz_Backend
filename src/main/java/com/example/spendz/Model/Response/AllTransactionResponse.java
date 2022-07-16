package com.example.spendz.Model.Response;


import com.example.spendz.Model.Category;
import com.example.spendz.Model.Spend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllTransactionResponse {
    private Long id;
    private Date txDate;
    private String rawDesc;
    private String info;
    private String additionInfo;
    private String displayInfo;
    private double amount;
    private double balance;
    private Spend.SpendType type;
    private Category category;
    private String bankName;
    private boolean excludeFromExpense;
    private String paymentVia;
    private Set<Integer> spendTagIds;
}
