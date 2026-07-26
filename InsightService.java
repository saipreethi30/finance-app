package com.smartfinance.service;

import com.smartfinance.model.CategoryReport;
import com.smartfinance.model.MonthlyBalance;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates smart financial insights from transaction data.
 */
public class InsightService {

    private final ReportService reportService;
    private final TransactionService transactionService;

    public InsightService(ReportService reportService, TransactionService transactionService) {
        this.reportService = reportService;
        this.transactionService = transactionService;
    }

    public List<String> generateInsights(long userId) {
        List<String> insights = new ArrayList<>();
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        MonthlyBalance thisMonth = reportService.getMonthSummary(userId, current);
        MonthlyBalance lastMonth = reportService.getMonthSummary(userId, previous);

        insights.add(String.format("Current month net balance: %.2f", thisMonth.getNetBalance()));

        if (lastMonth.getTotalExpense().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = thisMonth.getTotalExpense().subtract(lastMonth.getTotalExpense());
            String trend = change.compareTo(BigDecimal.ZERO) > 0 ? "increased" : "decreased";
            insights.add(String.format("Expenses %s by %.2f compared to last month", trend, change.abs()));
        }

        findTopExpenseCategory(userId, current).ifPresent(top ->
                insights.add(String.format("Top expense category this month: %s (%.2f)",
                        top.getCategoryName(), top.getTotal())));

        BigDecimal savingsRate = calculateSavingsRate(thisMonth);
        if (savingsRate.compareTo(BigDecimal.ZERO) > 0) {
            insights.add(String.format("Savings rate this month: %d%%", savingsRate.intValue()));
        }

        if (thisMonth.getNetBalance().compareTo(BigDecimal.ZERO) < 0) {
            insights.add("Tip: You are spending more than you earn this month. Review expense categories.");
        } else if (thisMonth.getNetBalance().compareTo(BigDecimal.valueOf(500)) > 0) {
            insights.add("Great job! You have a healthy surplus this month.");
        }

        long txnCount = transactionService.getAll(userId).size();
        insights.add(String.format("Total transactions recorded: %d", txnCount));

        return insights;
    }

    private Optional<CategoryReport> findTopExpenseCategory(long userId, YearMonth month) {
        Map<String, CategoryReport> reports = reportService.categoryWiseReport(userId, month);
        return reports.values().stream()
                .filter(r -> r.getType() == TransactionType.EXPENSE)
                .max(Comparator.comparing(CategoryReport::getTotal));
    }

    private BigDecimal calculateSavingsRate(MonthlyBalance balance) {
        if (balance.getTotalIncome().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return balance.getNetBalance()
                .multiply(BigDecimal.valueOf(100))
                .divide(balance.getTotalIncome(), 0, RoundingMode.HALF_UP);
    }
}
