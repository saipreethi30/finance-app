package com.smartfinance.service;

import com.smartfinance.dao.CategoryDAO;
import com.smartfinance.dao.UserDAO;
import com.smartfinance.model.BackupData;
import com.smartfinance.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Serializes user data to a .backup file for offline backup/restore.
 */
public class BackupService {

    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final TransactionService transactionService;

    public BackupService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void exportBackup(User user, Path file) throws IOException {
        BackupData data = new BackupData();
        data.setBackupTime(LocalDateTime.now());
        data.setUser(user);
        try {
            data.setCategories(categoryDAO.findByUserId(user.getId()));
        } catch (SQLException e) {
            throw new IOException("Failed to load categories for backup", e);
        }
        data.setTransactions(transactionService.getAll(user.getId()));

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(data);
        }
    }

    public BackupData importBackup(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            return (BackupData) in.readObject();
        }
    }

    public String describeBackup(BackupData data) {
        return String.format("Backup from %s — User: %s, %d categories, %d transactions",
                data.getBackupTime(),
                data.getUser().getUsername(),
                data.getCategories().size(),
                data.getTransactions().size());
    }
}
