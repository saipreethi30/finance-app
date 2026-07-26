package com.smartfinance.model;

/**
 * Enum for transaction direction - income adds money, expense removes it.
 */
public enum TransactionType {
    INCOME,
    EXPENSE;

    public static TransactionType fromString(String value) {
        return TransactionType.valueOf(value.toUpperCase());
    }
}
