package com.smartfinance.service;

import com.smartfinance.dao.CategoryDAO;

/**
 * Central holder for all services — shared across Console, Swing, and JavaFX UIs.
 */
public class FinanceContext {

    private final AuthService authService = new AuthService();
    private final TransactionService transactionService = new TransactionService();
    private final ReportService reportService = new ReportService(transactionService);
    private final NotificationService notificationService = new NotificationService();
    private final InsightService insightService = new InsightService(reportService, transactionService);
    private final BackupService backupService = new BackupService(transactionService);
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public AuthService getAuthService() {
        return authService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public InsightService getInsightService() {
        return insightService;
    }

    public BackupService getBackupService() {
        return backupService;
    }

    public CategoryDAO getCategoryDAO() {
        return categoryDAO;
    }
}
