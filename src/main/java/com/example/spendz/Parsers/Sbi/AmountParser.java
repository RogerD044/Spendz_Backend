package com.example.spendz.Parsers.Sbi;

import org.springframework.stereotype.Component;

@Component
public class AmountParser {

    public double parseAmount(String amount) {
        try {
            amount = amount.replace(",", "");
            return Double.parseDouble(amount);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
