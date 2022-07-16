package com.example.spendz.Model;

import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class Spend {
    @Id
    @GeneratedValue
    private Long id;
    private Date txDate;
    private String rawDesc;
    private String info;
    private String additionInfo;
    private String displayInfo;

    private double amount;
    private double balance;

    private SpendType type;

    private long categoryId;

    private String bankName;
    private boolean excludeFromExpense;
    private String paymentVia;

    public enum SpendType {
        D, C
    }

    @ElementCollection
    private Set<Integer> spendTags = new HashSet<>();
}
