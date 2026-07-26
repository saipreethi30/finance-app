package com.smartfinance.util;

import com.smartfinance.model.CategoryReport;
import com.smartfinance.model.MonthlyBalance;
import com.smartfinance.model.Transaction;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Exports reports to CSV or plain text files.
 */
public final class ExportUtil {

    private ExportUtil() {
    }

    public static void exportTransactionsCsv(List<Transaction> transactions, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.write("ID,Date,Type,Category,Amount,Description\n");
            for (Transaction t : transactions) {
                writer.write(String.format("%d,%s,%s,%s,%.2f,\"%s\"\n",
                        t.getId(),
                        t.getTxnDate(),
                        t.getType(),
                        escapeCsv(t.getCategoryName()),
                        t.getAmount(),
                        escapeCsv(t.getDescription())));
            }
        }
    }

    public static void exportCategoryReportCsv(Map<String, CategoryReport> reports, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.write("Category,Type,Total\n");
            for (CategoryReport r : reports.values()) {
                writer.write(String.format("%s,%s,%.2f\n",
                        escapeCsv(r.getCategoryName()), r.getType(), r.getTotal()));
            }
        }
    }

    public static void exportMonthlyBalanceText(Map<YearMonth, MonthlyBalance> balances, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.write("=== Monthly Balance Report ===\n\n");
            for (Map.Entry<YearMonth, MonthlyBalance> entry : balances.entrySet()) {
                MonthlyBalance b = entry.getValue();
                writer.write(String.format("Month: %s\n", entry.getKey()));
                writer.write(String.format("  Income:  %.2f\n", b.getTotalIncome()));
                writer.write(String.format("  Expense: %.2f\n", b.getTotalExpense()));
                writer.write(String.format("  Balance: %.2f\n\n", b.getNetBalance()));
            }
        }
    }

    public static void exportInsightsText(List<String> insights, Path file) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file)) {
            writer.write("=== Smart Finance Insights ===\n\n");
            for (String insight : insights) {
                writer.write("- " + insight + "\n");
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}
