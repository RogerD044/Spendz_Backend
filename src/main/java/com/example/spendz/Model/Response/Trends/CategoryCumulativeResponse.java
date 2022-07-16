package com.example.spendz.Model.Response.Trends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class CategoryCumulativeResponse implements TrendResponse {
    private List<CategoryTrendResponse> categoryTrendResponse;
    private HashMap<String, Double> categoryAvgExpense;
}
