package com.example.spendz.Model.Response.Trends;

import com.example.spendz.Model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;

@Data
@AllArgsConstructor
@Builder
public class CategoryTrendResponse {
    private Date startDate;
    private String label;
    private HashMap<String, Double> categorySpend;
    private double totalAmount;
}
