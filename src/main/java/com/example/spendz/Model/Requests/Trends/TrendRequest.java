package com.example.spendz.Model.Requests.Trends;

import com.example.spendz.Model.Category;
import com.example.spendz.Model.TimePeriod;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TrendRequest {
    private Date startDate;
    private Date endDate;
    private TimePeriod timePeriod;
    private TrendType trendType;
    private Set<String> categories;

    public enum TrendType {
        INCOME_EXPENSE, SAVING, CATEGORY_EXPENSE
    }
}
