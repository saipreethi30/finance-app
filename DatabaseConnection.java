package com.smartfinance.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Manages JDBC connection to embedded H2 database.
 * Database file is stored in ./data/finance_db
 */
public final class DatabaseConnection {

    private static final String DB_URL = "jdbc:h2:./data/finance_db;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");
            initializeSchema();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("H2 driver not found on classpath", e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void initializeSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = loadResource("schema.sql");
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            seedDefaultCategories(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private static String loadResource(String name) {
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + name);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + name, e);
        }
    }

    /**
     * Creates a demo user on first run so you can log in immediately.
     */
    private static void seedDefaultCategories(Connection conn) throws SQLException {
        try (var check = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
            var rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        String hash = com.smartfinance.util.PasswordUtil.hash("demo123");
        long userId;
        try (var insert = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, "demo");
            insert.setString(2, hash);
            insert.setString(3, "Demo User");
            insert.executeUpdate();
            var keys = insert.getGeneratedKeys();
            keys.next();
            userId = keys.getLong(1);
        }

        String[][] defaults = {
                {"Salary", "INCOME"}, {"Freelance", "INCOME"}, {"Investment", "INCOME"},
                {"Food", "EXPENSE"}, {"Rent", "EXPENSE"}, {"Transport", "EXPENSE"},
                {"Shopping", "EXPENSE"}, {"Utilities", "EXPENSE"}, {"Entertainment", "EXPENSE"}
        };

        try (var catInsert = conn.prepareStatement(
                "INSERT INTO categories (user_id, name, type, budget_limit) VALUES (?, ?, ?, ?)")) {
            for (String[] row : defaults) {
                catInsert.setLong(1, userId);
                catInsert.setString(2, row[0]);
                catInsert.setString(3, row[1]);
                if ("EXPENSE".equals(row[1])) {
                    catInsert.setBigDecimal(4, new java.math.BigDecimal("500.00"));
                } else {
                    catInsert.setBigDecimal(4, java.math.BigDecimal.ZERO);
                }
                catInsert.addBatch();
            }
            catInsert.executeBatch();
        }

        seedSampleTransactions(conn, userId);
    }

    /** Sample data so reports and charts work immediately for learning. */
    private static void seedSampleTransactions(Connection conn, long userId) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, type, description, txn_date) " +
                     "SELECT ?, id, ?, ?, ?, ? FROM categories WHERE user_id=? AND name=? AND type=?";
        Object[][] samples = {
                {"Salary", "INCOME", 3500.00, "Monthly salary", "2026-06-01"},
                {"Freelance", "INCOME", 800.00, "Side project", "2026-06-05"},
                {"Rent", "EXPENSE", 1200.00, "June rent", "2026-06-02"},
                {"Food", "EXPENSE", 320.50, "Groceries", "2026-06-08"},
                {"Transport", "EXPENSE", 85.00, "Gas", "2026-06-07"},
                {"Shopping", "EXPENSE", 150.00, "Clothes", "2026-06-04"},
                {"Salary", "INCOME", 3500.00, "May salary", "2026-05-01"},
                {"Food", "EXPENSE", 410.00, "May groceries", "2026-05-15"},
                {"Rent", "EXPENSE", 1200.00, "May rent", "2026-05-02"},
        };

        try (var ps = conn.prepareStatement(sql)) {
            for (Object[] row : samples) {
                ps.setLong(1, userId);
                ps.setBigDecimal(2, new java.math.BigDecimal(row[2].toString()));
                ps.setString(3, (String) row[1]);
                ps.setString(4, (String) row[3]);
                ps.setDate(5, java.sql.Date.valueOf((String) row[4]));
                ps.setLong(6, userId);
                ps.setString(7, (String) row[0]);
                ps.setString(8, (String) row[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
