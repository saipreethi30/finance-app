package com.smartfinance.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for serialization backup - holds all user data in memory.
 */
public class BackupData implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime backupTime;
    private User user;
    private List<Category> categories = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    public LocalDateTime getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(LocalDateTime backupTime) {
        this.backupTime = backupTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
