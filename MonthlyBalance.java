package com.smartfinance.model;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Summary of income, expense, and net balance for one month.
 */
public class MonthlyBalance {

    private final YearMonth month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;

    public MonthlyBalance(YearMonth month) {
        this.month = month;
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpense = BigDecimal.ZERO;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void addIncome(BigDecimal amount) {
        totalIncome = totalIncome.add(amount);
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void addExpense(BigDecimal amount) {
        totalExpense = totalExpense.add(amount);
    }

    public BigDecimal getNetBalance() {
        return totalIncome.subtract(totalExpense);
    }
}
