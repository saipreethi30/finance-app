package com.smartfinance.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A spending or income category belonging to a user.
 * budgetLimit is used for overspending notifications (expense categories).
 */
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private TransactionType type;
    private BigDecimal budgetLimit;

    public Category() {
        this.budgetLimit = BigDecimal.ZERO;
    }

    public Category(Long userId, String name, TransactionType type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.budgetLimit = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    @Override
    public String toString() {
        return name + " [" + type + "]";
    }
}
