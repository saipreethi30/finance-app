package com.smartfinance.service;

import com.smartfinance.dao.TransactionDAO;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for income/expense CRUD operations.
 */
public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public Transaction addIncome(long userId, long categoryId, BigDecimal amount,
                                 String description, LocalDate date) {
        return add(userId, categoryId, amount, TransactionType.INCOME, description, date);
    }

    public Transaction addExpense(long userId, long categoryId, BigDecimal amount,
                                  String description, LocalDate date) {
        return add(userId, categoryId, amount, TransactionType.EXPENSE, description, date);
    }

    private Transaction add(long userId, long categoryId, BigDecimal amount,
                            TransactionType type, String description, LocalDate date) {
        validateAmount(amount);
        try {
            Transaction txn = new Transaction(userId, categoryId, amount, type, description, date);
            return transactionDAO.insert(txn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add transaction", e);
        }
    }

    public boolean update(Transaction txn) {
        validateAmount(txn.getAmount());
        try {
            return transactionDAO.update(txn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    public boolean delete(long id, long userId) {
        try {
            return transactionDAO.delete(id, userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    public List<Transaction> getAll(long userId) {
        try {
            return transactionDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions", e);
        }
    }

    public List<Transaction> search(long userId, LocalDate from, LocalDate to,
                                    Long categoryId, TransactionType type) {
        try {
            return transactionDAO.search(userId, from, to, categoryId, type);
        } catch (SQLException e) {
            throw new RuntimeException("Search failed", e);
        }
    }

    public Optional<Transaction> findById(long id) {
        try {
            return transactionDAO.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transaction", e);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
