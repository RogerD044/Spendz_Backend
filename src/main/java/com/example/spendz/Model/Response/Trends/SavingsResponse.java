package com.example.spendz.Model.Response.Trends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class SavingsResponse {
    private Date startDate;
    private String label;
    private double amount;

}
