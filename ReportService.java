package com.smartfinance.service;

import com.smartfinance.model.CategoryReport;
import com.smartfinance.model.MonthlyBalance;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Builds category-wise and monthly balance reports using Java Collections.
 */
public class ReportService {

    private final TransactionService transactionService;

    public ReportService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Groups transactions by category name using a HashMap.
     */
    public Map<String, CategoryReport> categoryWiseReport(long userId, YearMonth month) {
        List<Transaction> txns = filterByMonth(transactionService.getAll(userId), month);
        Map<String, CategoryReport> report = new LinkedHashMap<>();

        for (Transaction t : txns) {
            String key = t.getCategoryName() + "_" + t.getType();
            report.computeIfAbsent(key, k -> new CategoryReport(t.getCategoryName(), t.getType()))
                  .add(t.getAmount());
        }
        return report;
    }

    /**
     * Monthly balance using TreeMap for sorted chronological order.
     */
    public Map<YearMonth, MonthlyBalance> monthlyBalanceReport(long userId) {
        List<Transaction> txns = transactionService.getAll(userId);
        Map<YearMonth, MonthlyBalance> balances = new TreeMap<>();

        for (Transaction t : txns) {
            YearMonth ym = YearMonth.from(t.getTxnDate());
            MonthlyBalance balance = balances.computeIfAbsent(ym, MonthlyBalance::new);
            if (t.getType() == TransactionType.INCOME) {
                balance.addIncome(t.getAmount());
            } else {
                balance.addExpense(t.getAmount());
            }
        }
        return balances;
    }

    public MonthlyBalance getMonthSummary(long userId, YearMonth month) {
        List<Transaction> txns = filterByMonth(transactionService.getAll(userId), month);
        MonthlyBalance balance = new MonthlyBalance(month);
        for (Transaction t : txns) {
            if (t.getType() == TransactionType.INCOME) {
                balance.addIncome(t.getAmount());
            } else {
                balance.addExpense(t.getAmount());
            }
        }
        return balance;
    }

    public BigDecimal getTotalBalance(long userId) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : transactionService.getAll(userId)) {
            if (t.getType() == TransactionType.INCOME) {
                income = income.add(t.getAmount());
            } else {
                expense = expense.add(t.getAmount());
            }
        }
        return income.subtract(expense);
    }

    private List<Transaction> filterByMonth(List<Transaction> txns, YearMonth month) {
        if (month == null) {
            return txns;
        }
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : txns) {
            if (YearMonth.from(t.getTxnDate()).equals(month)) {
                filtered.add(t);
            }
        }
        return filtered;
    }
}
