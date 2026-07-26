package com.smartfinance.model;

import java.math.BigDecimal;

/**
 * Aggregated totals for one category (used in reports and charts).
 */
public class CategoryReport {

    private final String categoryName;
    private final TransactionType type;
    private BigDecimal total;

    public CategoryReport(String categoryName, TransactionType type) {
        this.categoryName = categoryName;
        this.type = type;
        this.total = BigDecimal.ZERO;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void add(BigDecimal amount) {
        total = total.add(amount);
    }
}
