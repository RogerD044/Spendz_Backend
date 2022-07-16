package com.example.spendz.Model.Response.Trends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;

@Data
@AllArgsConstructor
@Builder
public class CumulativeResponse {
    private Date monthDate;
    private String label;
    private double closingAmount;
    private HashMap<Long, Double> income;
    private HashMap<Long, Double> categoryExpense;
    private HashMap<Long, Double> tagExpense;
}
