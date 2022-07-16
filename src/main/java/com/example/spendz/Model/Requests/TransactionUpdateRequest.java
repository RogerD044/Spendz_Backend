package com.example.spendz.Model.Requests;

import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TransactionUpdateRequest {
    private Long id;
    private String displayInfo;
    private Long categoryId;
    private boolean excludeFromExpense;
    private boolean allowUpcomingCategoryChanges;
    private Set<Integer> spendTags;
}
