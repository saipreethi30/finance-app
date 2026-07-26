package com.smartfinance.service;

import com.smartfinance.dao.CategoryDAO;
import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Category;
import com.smartfinance.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks expense categories against budget limits and warns about overspending.
 */
public class NotificationService {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<String> checkOverspending(long userId) {
        List<String> alerts = new ArrayList<>();
        YearMonth current = YearMonth.now();

        try {
            List<Category> expenseCategories = categoryDAO.findByUserAndType(userId, TransactionType.EXPENSE);
            for (Category cat : expenseCategories) {
                if (cat.getBudgetLimit().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal spent = transactionDAO.sumByCategoryAndMonth(
                        userId, cat.getId(), current.getYear(), current.getMonthValue());

                if (spent.compareTo(cat.getBudgetLimit()) > 0) {
                    BigDecimal over = spent.subtract(cat.getBudgetLimit());
                    alerts.add(String.format("OVERSPENT: %s — spent %.2f, budget %.2f (over by %.2f)",
                            cat.getName(), spent, cat.getBudgetLimit(), over));
                } else {
                    BigDecimal pct = spent.multiply(BigDecimal.valueOf(100))
                            .divide(cat.getBudgetLimit(), 0, RoundingMode.HALF_UP);
                    if (pct.compareTo(BigDecimal.valueOf(80)) >= 0) {
                        alerts.add(String.format("WARNING: %s — %d%% of budget used (%.2f / %.2f)",
                                cat.getName(), pct.intValue(), spent, cat.getBudgetLimit()));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check budgets", e);
        }
        return alerts;
    }
}
