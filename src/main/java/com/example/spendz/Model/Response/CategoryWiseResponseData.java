package com.example.spendz.Model.Response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CategoryWiseResponseData {
    private Long id;
    private String color;
    private double amount;
    private double percentile;
    private String categoryName;
    private int noOfSpend;
}
