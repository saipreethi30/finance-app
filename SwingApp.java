package com.smartfinance.swing;

import com.smartfinance.model.Category;
import com.smartfinance.model.CategoryReport;
import com.smartfinance.model.MonthlyBalance;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;
import com.smartfinance.model.User;
import com.smartfinance.service.FinanceContext;
import com.smartfinance.util.ExportUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Swing desktop UI — uses standard Java GUI components.
 */
public class SwingApp {

    private final FinanceContext context = new FinanceContext();
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JComboBox<Category> categoryCombo;
    private JComboBox<String> typeCombo;

    public void start() {
        if (!showLoginDialog()) {
            return;
        }
        buildMainWindow();
    }

    private boolean showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField fullName = new JTextField();
        panel.add(new JLabel("Username:"));
        panel.add(username);
        panel.add(new JLabel("Password:"));
        panel.add(password);
        panel.add(new JLabel("Full Name (register only):"));
        panel.add(fullName);

        int login = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (login != JOptionPane.OK_OPTION) {
            return false;
        }

        var auth = context.getAuthService();
        var user = auth.login(username.getText(), new String(password.getPassword()));
        if (user.isPresent()) {
            return true;
        }

        int reg = JOptionPane.showConfirmDialog(null, "Login failed. Register new user?", "Register",
                JOptionPane.YES_NO_OPTION);
        if (reg == JOptionPane.YES_OPTION) {
            try {
                auth.register(username.getText(), new String(password.getPassword()), fullName.getText());
                return true;
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        return false;
    }

    private void buildMainWindow() {
        User user = context.getAuthService().getCurrentUser();
        frame = new JFrame("Smart Finance Tracker - Swing (" + user.getUsername() + ")");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Transactions", buildTransactionPanel());
        tabs.addTab("Reports", buildReportPanel());
        tabs.addTab("Insights", buildInsightPanel());

        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);
        refreshTable();
    }

    private JPanel buildTransactionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "Date", "Type", "Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(3, 4, 5, 5));
        typeCombo = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        categoryCombo = new JComboBox<>();
        JTextField amountField = new JTextField();
        JTextField descField = new JTextField();
        JTextField dateField = new JTextField(LocalDate.now().toString());
        JTextField filterFrom = new JTextField();
        JTextField filterTo = new JTextField();

        typeCombo.addActionListener(e -> loadCategories());
        loadCategories();

        form.add(new JLabel("Type:"));
        form.add(typeCombo);
        form.add(new JLabel("Category:"));
        form.add(categoryCombo);
        form.add(new JLabel("Amount:"));
        form.add(amountField);
        form.add(new JLabel("Date:"));
        form.add(dateField);
        form.add(new JLabel("Description:"));
        form.add(descField);

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> {
            try {
                Category cat = (Category) categoryCombo.getSelectedItem();
                TransactionType type = TransactionType.fromString((String) typeCombo.getSelectedItem());
                BigDecimal amount = new BigDecimal(amountField.getText().trim());
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                long userId = context.getAuthService().getCurrentUser().getId();
                if (type == TransactionType.INCOME) {
                    context.getTransactionService().addIncome(userId, cat.getId(), amount, descField.getText(), date);
                } else {
                    context.getTransactionService().addExpense(userId, cat.getId(), amount, descField.getText(), date);
                }
                refreshTable();
                amountField.setText("");
                descField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            long id = (Long) tableModel.getValueAt(row, 0);
            long userId = context.getAuthService().getCurrentUser().getId();
            context.getTransactionService().delete(id, userId);
            refreshTable();
        });

        JButton filterBtn = new JButton("Filter");
        filterBtn.addActionListener(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            LocalDate from = filterFrom.getText().isBlank() ? null : LocalDate.parse(filterFrom.getText().trim());
            LocalDate to = filterTo.getText().isBlank() ? null : LocalDate.parse(filterTo.getText().trim());
            fillTable(context.getTransactionService().search(userId, from, to, null, null));
        });

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.addActionListener(e -> exportCsv());

        JPanel buttons = new JPanel();
        buttons.add(addBtn);
        buttons.add(deleteBtn);
        buttons.add(new JLabel("From:"));
        buttons.add(filterFrom);
        buttons.add(new JLabel("To:"));
        buttons.add(filterTo);
        buttons.add(filterBtn);
        buttons.add(exportBtn);

        panel.add(form, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea();
        area.setEditable(false);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton btn = new JButton("Generate Reports");
        btn.addActionListener(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            StringBuilder sb = new StringBuilder();
            YearMonth month = YearMonth.now();

            sb.append("=== Category Report (").append(month).append(") ===\n");
            Map<String, CategoryReport> catReport = context.getReportService().categoryWiseReport(userId, month);
            catReport.values().forEach(r ->
                    sb.append(String.format("  %s [%s]: %.2f%n", r.getCategoryName(), r.getType(), r.getTotal())));

            sb.append("\n=== Monthly Balance ===\n");
            Map<YearMonth, MonthlyBalance> monthly = context.getReportService().monthlyBalanceReport(userId);
            monthly.forEach((m, b) -> sb.append(String.format("%s | In: %.2f | Out: %.2f | Net: %.2f%n",
                    m, b.getTotalIncome(), b.getTotalExpense(), b.getNetBalance())));

            sb.append("\n=== Overspending Alerts ===\n");
            context.getNotificationService().checkOverspending(userId)
                    .forEach(a -> sb.append("  ! ").append(a).append("\n"));

            area.setText(sb.toString());
        });
        panel.add(btn, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildInsightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea();
        area.setEditable(false);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton insightBtn = new JButton("Generate Insights");
        insightBtn.addActionListener(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            List<String> insights = context.getInsightService().generateInsights(userId);
            area.setText(String.join("\n", insights));
        });

        JButton backupBtn = new JButton("Create Backup");
        backupBtn.addActionListener(e -> {
            try {
                User user = context.getAuthService().getCurrentUser();
                Path file = Path.of("backups", user.getUsername() + "_backup.ser");
                java.nio.file.Files.createDirectories(file.getParent());
                context.getBackupService().exportBackup(user, file);
                JOptionPane.showMessageDialog(frame, "Saved: " + file.toAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });

        JPanel btns = new JPanel();
        btns.add(insightBtn);
        btns.add(backupBtn);
        panel.add(btns, BorderLayout.NORTH);
        return panel;
    }

    private void loadCategories() {
        categoryCombo.removeAllItems();
        try {
            long userId = context.getAuthService().getCurrentUser().getId();
            TransactionType type = TransactionType.fromString((String) typeCombo.getSelectedItem());
            for (Category c : context.getCategoryDAO().findByUserAndType(userId, type)) {
                categoryCombo.addItem(c);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }
    }

    private void refreshTable() {
        long userId = context.getAuthService().getCurrentUser().getId();
        fillTable(context.getTransactionService().getAll(userId));
    }

    private void fillTable(List<Transaction> txns) {
        tableModel.setRowCount(0);
        for (Transaction t : txns) {
            tableModel.addRow(new Object[]{
                    t.getId(), t.getTxnDate(), t.getType(),
                    t.getCategoryName(), t.getAmount(), t.getDescription()
            });
        }
    }

    private void exportCsv() {
        try {
            long userId = context.getAuthService().getCurrentUser().getId();
            Path file = Path.of("exports", "swing_transactions.csv");
            java.nio.file.Files.createDirectories(file.getParent());
            ExportUtil.exportTransactionsCsv(context.getTransactionService().getAll(userId), file);
            JOptionPane.showMessageDialog(frame, "Exported: " + file.toAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }
    }
}
