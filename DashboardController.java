package com.smartfinance.javafx;

import com.smartfinance.model.Category;
import com.smartfinance.model.CategoryReport;
import com.smartfinance.model.MonthlyBalance;
import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;
import com.smartfinance.model.User;
import com.smartfinance.service.FinanceContext;
import com.smartfinance.util.ExportUtil;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Main JavaFX dashboard with charts, CRUD, reports, and export.
 */
public class DashboardController {

    private final FinanceContext context;
    private final Stage stage;
    private TableView<TransactionRow> transactionTable;
    private ComboBox<Category> categoryCombo;
    private ComboBox<String> typeCombo;

    public DashboardController(FinanceContext context, Stage stage) {
        this.context = context;
        this.stage = stage;
    }

    public void show() {
        User user = context.getAuthService().getCurrentUser();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label header = new Label("Welcome, " + user.getFullName() +
                "  |  Balance: " + String.format("%.2f", context.getReportService().getTotalBalance(user.getId())));
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("Transactions", buildTransactionTab()),
                new Tab("Charts", buildChartsTab()),
                new Tab("Reports", buildReportsTab()),
                new Tab("Insights & Alerts", buildInsightsTab())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        root.setCenter(tabs);

        stage.setScene(new javafx.scene.Scene(root, 1000, 650));
        stage.setTitle("Smart Finance Tracker - JavaFX");
        stage.show();
        refreshTransactions();
    }

    private VBox buildTransactionTab() {
        transactionTable = new TableView<>();
        TableColumn<TransactionRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<TransactionRow, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<TransactionRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<TransactionRow, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<TransactionRow, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<TransactionRow, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionTable.getColumns().addAll(idCol, dateCol, typeCol, catCol, amtCol, descCol);

        typeCombo = new ComboBox<>(FXCollections.observableArrayList("INCOME", "EXPENSE"));
        typeCombo.getSelectionModel().select("EXPENSE");
        categoryCombo = new ComboBox<>();
        loadCategories();
        typeCombo.setOnAction(e -> loadCategories());

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        TextField descField = new TextField();
        descField.setPromptText("Description");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        DatePicker filterFrom = new DatePicker();
        DatePicker filterTo = new DatePicker();
        filterFrom.setPromptText("From");
        filterTo.setPromptText("To");

        Button addBtn = new Button("Add");
        addBtn.setOnAction(e -> addTransaction(amountField, descField, datePicker));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> deleteSelected());

        Button filterBtn = new Button("Filter");
        filterBtn.setOnAction(e -> filterTransactions(filterFrom, filterTo));

        Button exportBtn = new Button("Export CSV");
        exportBtn.setOnAction(e -> exportCsv());

        HBox form = new HBox(8, new Label("Type:"), typeCombo, new Label("Category:"), categoryCombo,
                amountField, descField, datePicker, addBtn, deleteBtn);
        HBox filterBar = new HBox(8, filterFrom, filterTo, filterBtn, exportBtn);

        VBox box = new VBox(10, form, transactionTable, filterBar);
        box.setPadding(new Insets(10));
        VBox.setVgrow(transactionTable, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private VBox buildChartsTab() {
        PieChart expensePie = new PieChart();
        expensePie.setTitle("Expense by Category (This Month)");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Monthly Income vs Expense");
        xAxis.setLabel("Month");
        yAxis.setLabel("Amount");

        Button refreshBtn = new Button("Refresh Charts");
        refreshBtn.setOnAction(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            YearMonth month = YearMonth.now();

            expensePie.getData().clear();
            Map<String, CategoryReport> report = context.getReportService().categoryWiseReport(userId, month);
            report.values().stream()
                    .filter(r -> r.getType() == TransactionType.EXPENSE)
                    .forEach(r -> expensePie.getData().add(
                            new PieChart.Data(r.getCategoryName(), r.getTotal().doubleValue())));

            barChart.getData().clear();
            XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
            incomeSeries.setName("Income");
            XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
            expenseSeries.setName("Expense");

            Map<YearMonth, MonthlyBalance> monthly = context.getReportService().monthlyBalanceReport(userId);
            monthly.forEach((m, b) -> {
                incomeSeries.getData().add(new XYChart.Data<>(m.toString(), b.getTotalIncome()));
                expenseSeries.getData().add(new XYChart.Data<>(m.toString(), b.getTotalExpense()));
            });
            barChart.getData().addAll(incomeSeries, expenseSeries);
        });

        VBox box = new VBox(15, refreshBtn, expensePie, barChart);
        box.setPadding(new Insets(10));
        return box;
    }

    private VBox buildReportsTab() {
        TextArea area = new TextArea();
        area.setEditable(false);

        Button btn = new Button("Generate Reports");
        btn.setOnAction(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            StringBuilder sb = new StringBuilder();
            YearMonth month = YearMonth.now();

            sb.append("Category Report — ").append(month).append("\n");
            context.getReportService().categoryWiseReport(userId, month).values()
                    .forEach(r -> sb.append(String.format("  %s: %.2f%n", r.getCategoryName(), r.getTotal())));

            sb.append("\nMonthly Balance\n");
            context.getReportService().monthlyBalanceReport(userId)
                    .forEach((m, b) -> sb.append(String.format("  %s → Net: %.2f%n", m, b.getNetBalance())));

            area.setText(sb.toString());

            try {
                Path dir = Path.of("exports");
                java.nio.file.Files.createDirectories(dir);
                ExportUtil.exportMonthlyBalanceText(
                        context.getReportService().monthlyBalanceReport(userId),
                        dir.resolve("monthly_balance.txt"));
            } catch (Exception ignored) {
            }
        });

        VBox box = new VBox(10, btn, area);
        box.setPadding(new Insets(10));
        VBox.setVgrow(area, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private VBox buildInsightsTab() {
        TextArea area = new TextArea();
        area.setEditable(false);

        Button insightBtn = new Button("Smart Insights");
        insightBtn.setOnAction(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            List<String> insights = context.getInsightService().generateInsights(userId);
            area.setText(String.join("\n", insights));
        });

        Button alertBtn = new Button("Overspending Alerts");
        alertBtn.setOnAction(e -> {
            long userId = context.getAuthService().getCurrentUser().getId();
            List<String> alerts = context.getNotificationService().checkOverspending(userId);
            if (alerts.isEmpty()) {
                area.setText("No overspending alerts.");
            } else {
                area.setText(String.join("\n", alerts));
            }
        });

        Button backupBtn = new Button("Serialize Backup");
        backupBtn.setOnAction(e -> {
            try {
                User user = context.getAuthService().getCurrentUser();
                Path file = Path.of("backups", user.getUsername() + "_backup.ser");
                java.nio.file.Files.createDirectories(file.getParent());
                context.getBackupService().exportBackup(user, file);
                new Alert(Alert.AlertType.INFORMATION, "Backup: " + file.toAbsolutePath()).showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
            }
        });

        HBox buttons = new HBox(10, insightBtn, alertBtn, backupBtn);
        VBox box = new VBox(10, buttons, area);
        box.setPadding(new Insets(10));
        VBox.setVgrow(area, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    private void addTransaction(TextField amountField, TextField descField, DatePicker datePicker) {
        try {
            Category cat = categoryCombo.getValue();
            TransactionType type = TransactionType.fromString(typeCombo.getValue());
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            long userId = context.getAuthService().getCurrentUser().getId();
            if (type == TransactionType.INCOME) {
                context.getTransactionService().addIncome(userId, cat.getId(), amount, descField.getText(), datePicker.getValue());
            } else {
                context.getTransactionService().addExpense(userId, cat.getId(), amount, descField.getText(), datePicker.getValue());
            }
            refreshTransactions();
            amountField.clear();
            descField.clear();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private void deleteSelected() {
        TransactionRow row = transactionTable.getSelectionModel().getSelectedItem();
        if (row == null) return;
        long userId = context.getAuthService().getCurrentUser().getId();
        context.getTransactionService().delete(row.getId(), userId);
        refreshTransactions();
    }

    private void filterTransactions(DatePicker from, DatePicker to) {
        long userId = context.getAuthService().getCurrentUser().getId();
        List<Transaction> results = context.getTransactionService().search(
                userId, from.getValue(), to.getValue(), null, null);
        fillTable(results);
    }

    private void loadCategories() {
        try {
            long userId = context.getAuthService().getCurrentUser().getId();
            TransactionType type = TransactionType.fromString(typeCombo.getValue());
            categoryCombo.setItems(FXCollections.observableArrayList(
                    context.getCategoryDAO().findByUserAndType(userId, type)));
            if (!categoryCombo.getItems().isEmpty()) {
                categoryCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    private void refreshTransactions() {
        long userId = context.getAuthService().getCurrentUser().getId();
        fillTable(context.getTransactionService().getAll(userId));
    }

    private void fillTable(List<Transaction> txns) {
        transactionTable.setItems(FXCollections.observableArrayList(
                txns.stream().map(TransactionRow::from).toList()));
    }

    private void exportCsv() {
        try {
            long userId = context.getAuthService().getCurrentUser().getId();
            Path file = Path.of("exports", "javafx_transactions.csv");
            java.nio.file.Files.createDirectories(file.getParent());
            ExportUtil.exportTransactionsCsv(context.getTransactionService().getAll(userId), file);
            new Alert(Alert.AlertType.INFORMATION, "Exported: " + file.toAbsolutePath()).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    /** Simple row model for JavaFX TableView binding */
    public static class TransactionRow {
        private Long id;
        private String date;
        private String type;
        private String category;
        private String amount;
        private String description;

        public static TransactionRow from(Transaction t) {
            TransactionRow row = new TransactionRow();
            row.id = t.getId();
            row.date = t.getTxnDate().toString();
            row.type = t.getType().name();
            row.category = t.getCategoryName();
            row.amount = String.format("%.2f", t.getAmount());
            row.description = t.getDescription();
            return row;
        }

        public Long getId() { return id; }
        public String getDate() { return date; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public String getAmount() { return amount; }
        public String getDescription() { return description; }
    }
}
