package com.example.spendz.Model;

public enum TimePeriod {
    MONTHLY(1), QUARTERLY(3), HALF_YEARLY(6), YEARLY(12);

    private final int value;

    private TimePeriod(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
