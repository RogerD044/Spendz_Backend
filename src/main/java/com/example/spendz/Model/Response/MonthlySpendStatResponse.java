package com.example.spendz.Model.Response;


import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySpendStatResponse {
    private double monthlySpend;
    private double spendLimit = 26000;
    private double lastWeekSpend;
    private TreeMap<Date, Double> lastSevenDaySpends;
    private List<AllTransactionResponse> topSixTransactions;
    private List<CategoryWiseResponseData> topSixCategorySpends;
}
