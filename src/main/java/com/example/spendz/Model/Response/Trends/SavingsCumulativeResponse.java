package com.example.spendz.Model.Response.Trends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SavingsCumulativeResponse implements TrendResponse {
    private List<SavingsResponse> savings;
    private double averageAmount;
}
