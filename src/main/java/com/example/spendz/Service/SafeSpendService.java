package com.example.spendz.Service;

import org.springframework.stereotype.Service;

@Service
public class SafeSpendService {

    private final double SAFE_SPEND_LIMIT = 26000;

    public double getSafeSpendLimit() {
        return SAFE_SPEND_LIMIT;
    }
}
