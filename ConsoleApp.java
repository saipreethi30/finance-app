package com.smartfinance.console;

import com.smartfinance.model.Category;
import com.smartfinance.model.CategoryReport;
import com.smartfinance.model.MonthlyBalance;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;
import com.smartfinance.model.User;
import com.smartfinance.service.FinanceContext;
import com.smartfinance.util.DateUtil;
import com.smartfinance.util.ExportUtil;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Text-based console menu for learning core Java I/O and control flow.
 */
public class ConsoleApp {

    private final FinanceContext context = new FinanceContext();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        printBanner();
        if (!loginOrRegister()) {
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addTransaction(TransactionType.INCOME);
                case "2" -> addTransaction(TransactionType.EXPENSE);
                case "3" -> listTransactions();
                case "4" -> updateTransaction();
                case "5" -> deleteTransaction();
                case "6" -> searchTransactions();
                case "7" -> showCategoryReport();
                case "8" -> showMonthlyReport();
                case "9" -> showInsights();
                case "10" -> showNotifications();
                case "11" -> exportData();
                case "12" -> backupData();
                case "0" -> {
                    context.getAuthService().logout();
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye!");
    }

    private void printBanner() {
        System.out.println("========================================");
        System.out.println("   SMART FINANCE TRACKER (Console)");
        System.out.println("========================================");
    }

    private boolean loginOrRegister() {
        System.out.println("\n1. Login  2. Register  0. Exit");
        String choice = scanner.nextLine().trim();
        if ("0".equals(choice)) {
            return false;
        }
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if ("2".equals(choice)) {
            System.out.print("Full Name: ");
            String fullName = scanner.nextLine().trim();
            try {
                User user = context.getAuthService().register(username, password, fullName);
                System.out.println("Registered: " + user);
                return true;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                return false;
            }
        }

        Optional<User> user = context.getAuthService().login(username, password);
        if (user.isPresent()) {
            System.out.println("Welcome, " + user.get().getFullName() + "!");
            return true;
        }
        System.out.println("Invalid credentials.");
        return false;
    }

    private void printMainMenu() {
        User user = context.getAuthService().getCurrentUser();
        System.out.println("\n--- Main Menu (" + user.getUsername() + ") ---");
        System.out.println("1. Add Income     2. Add Expense");
        System.out.println("3. List All       4. Update");
        System.out.println("5. Delete         6. Search/Filter");
        System.out.println("7. Category Report  8. Monthly Balance");
        System.out.println("9. Smart Insights   10. Overspending Alerts");
        System.out.println("11. Export CSV/Text 12. Backup (Serialize)");
        System.out.println("0. Logout");
        System.out.print("Choice: ");
    }

    private void addTransaction(TransactionType type) {
        long userId = context.getAuthService().getCurrentUser().getId();
        try {
            List<Category> categories = context.getCategoryDAO().findByUserAndType(userId, type);
            if (categories.isEmpty()) {
                System.out.println("No categories found.");
                return;
            }
            System.out.println("Categories:");
            for (int i = 0; i < categories.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, categories.get(i).getName());
            }
            System.out.print("Select category #: ");
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            Category cat = categories.get(idx);

            System.out.print("Amount: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Description: ");
            String desc = scanner.nextLine().trim();
            System.out.print("Date (yyyy-MM-dd): ");
            LocalDate date = DateUtil.parseDate(scanner.nextLine());

            Transaction txn = type == TransactionType.INCOME
                    ? context.getTransactionService().addIncome(userId, cat.getId(), amount, desc, date)
                    : context.getTransactionService().addExpense(userId, cat.getId(), amount, desc, date);
            System.out.println("Added: " + txn);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listTransactions() {
        long userId = context.getAuthService().getCurrentUser().getId();
        List<Transaction> txns = context.getTransactionService().getAll(userId);
        if (txns.isEmpty()) {
            System.out.println("No transactions.");
            return;
        }
        txns.forEach(System.out::println);
    }

    private void updateTransaction() {
        long userId = context.getAuthService().getCurrentUser().getId();
        System.out.print("Transaction ID: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Optional<Transaction> opt = context.getTransactionService().findById(id);
        if (opt.isEmpty() || !opt.get().getUserId().equals(userId)) {
            System.out.println("Not found.");
            return;
        }
        Transaction txn = opt.get();
        System.out.print("New amount (" + txn.getAmount() + "): ");
        String amt = scanner.nextLine().trim();
        if (!amt.isEmpty()) {
            txn.setAmount(new BigDecimal(amt));
        }
        System.out.print("New description (" + txn.getDescription() + "): ");
        String desc = scanner.nextLine().trim();
        if (!desc.isEmpty()) {
            txn.setDescription(desc);
        }
        if (context.getTransactionService().update(txn)) {
            System.out.println("Updated.");
        }
    }

    private void deleteTransaction() {
        long userId = context.getAuthService().getCurrentUser().getId();
        System.out.print("Transaction ID to delete: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        if (context.getTransactionService().delete(id, userId)) {
            System.out.println("Deleted.");
        } else {
            System.out.println("Not found.");
        }
    }

    private void searchTransactions() {
        long userId = context.getAuthService().getCurrentUser().getId();
        System.out.print("From date (yyyy-MM-dd, blank=any): ");
        String fromStr = scanner.nextLine().trim();
        System.out.print("To date (yyyy-MM-dd, blank=any): ");
        String toStr = scanner.nextLine().trim();
        System.out.print("Type (INCOME/EXPENSE, blank=any): ");
        String typeStr = scanner.nextLine().trim();

        LocalDate from = fromStr.isEmpty() ? null : DateUtil.parseDate(fromStr);
        LocalDate to = toStr.isEmpty() ? null : DateUtil.parseDate(toStr);
        TransactionType type = typeStr.isEmpty() ? null : TransactionType.fromString(typeStr);

        List<Transaction> results = context.getTransactionService().search(userId, from, to, null, type);
        System.out.println("Found " + results.size() + " transaction(s):");
        results.forEach(System.out::println);
    }

    private void showCategoryReport() {
        long userId = context.getAuthService().getCurrentUser().getId();
        System.out.print("Month (yyyy-MM, blank=current): ");
        String m = scanner.nextLine().trim();
        YearMonth month = m.isEmpty() ? YearMonth.now() : DateUtil.parseMonth(m);

        Map<String, CategoryReport> report = context.getReportService().categoryWiseReport(userId, month);
        System.out.println("\n=== Category Report: " + month + " ===");
        report.values().forEach(r ->
                System.out.printf("  %s [%s]: %.2f%n", r.getCategoryName(), r.getType(), r.getTotal()));
    }

    private void showMonthlyReport() {
        long userId = context.getAuthService().getCurrentUser().getId();
        Map<YearMonth, MonthlyBalance> report = context.getReportService().monthlyBalanceReport(userId);
        System.out.println("\n=== Monthly Balance Report ===");
        report.forEach((month, b) -> {
            System.out.printf("%s | Income: %.2f | Expense: %.2f | Balance: %.2f%n",
                    month, b.getTotalIncome(), b.getTotalExpense(), b.getNetBalance());
        });
        System.out.printf("Overall balance: %.2f%n", context.getReportService().getTotalBalance(userId));
    }

    private void showInsights() {
        long userId = context.getAuthService().getCurrentUser().getId();
        List<String> insights = context.getInsightService().generateInsights(userId);
        System.out.println("\n=== Smart Insights ===");
        insights.forEach(i -> System.out.println("  * " + i));
    }

    private void showNotifications() {
        long userId = context.getAuthService().getCurrentUser().getId();
        List<String> alerts = context.getNotificationService().checkOverspending(userId);
        if (alerts.isEmpty()) {
            System.out.println("No overspending alerts. Budgets look good!");
        } else {
            System.out.println("\n=== Overspending Alerts ===");
            alerts.forEach(a -> System.out.println("  ! " + a));
        }
    }

    private void exportData() {
        long userId = context.getAuthService().getCurrentUser().getId();
        try {
            Path dir = Path.of("exports");
            java.nio.file.Files.createDirectories(dir);
            List<Transaction> txns = context.getTransactionService().getAll(userId);
            Path csv = dir.resolve("transactions.csv");
            ExportUtil.exportTransactionsCsv(txns, csv);
            System.out.println("Exported transactions to: " + csv.toAbsolutePath());

            Map<YearMonth, MonthlyBalance> monthly = context.getReportService().monthlyBalanceReport(userId);
            Path txt = dir.resolve("monthly_balance.txt");
            ExportUtil.exportMonthlyBalanceText(monthly, txt);
            System.out.println("Exported monthly report to: " + txt.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Export failed: " + e.getMessage());
        }
    }

    private void backupData() {
        User user = context.getAuthService().getCurrentUser();
        try {
            Path dir = Path.of("backups");
            java.nio.file.Files.createDirectories(dir);
            Path file = dir.resolve(user.getUsername() + "_backup.ser");
            context.getBackupService().exportBackup(user, file);
            System.out.println("Backup saved to: " + file.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }
}
