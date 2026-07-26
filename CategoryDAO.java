package com.smartfinance.dao;

import com.smartfinance.model.Category;
import com.smartfinance.model.TransactionType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access for categories table.
 */
public class CategoryDAO {

    public List<Category> findByUserId(long userId) throws SQLException {
        String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY type, name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Category> findByUserAndType(long userId, TransactionType type) throws SQLException {
        String sql = "SELECT * FROM categories WHERE user_id = ? AND type = ? ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<Category> findById(long id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Category insert(Category category) throws SQLException {
        String sql = "INSERT INTO categories (user_id, name, type, budget_limit) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, category.getUserId());
            ps.setString(2, category.getName());
            ps.setString(3, category.getType().name());
            ps.setBigDecimal(4, category.getBudgetLimit());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getLong(1));
                }
            }
        }
        return category;
    }

    public void updateBudgetLimit(long categoryId, BigDecimal limit) throws SQLException {
        String sql = "UPDATE categories SET budget_limit = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, limit);
            ps.setLong(2, categoryId);
            ps.executeUpdate();
        }
    }

    public void createDefaultsForUser(long userId) throws SQLException {
        String[][] defaults = {
                {"Salary", "INCOME"}, {"Freelance", "INCOME"},
                {"Food", "EXPENSE"}, {"Rent", "EXPENSE"}, {"Transport", "EXPENSE"},
                {"Shopping", "EXPENSE"}, {"Utilities", "EXPENSE"}
        };
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories (user_id, name, type, budget_limit) VALUES (?, ?, ?, ?)")) {
            for (String[] row : defaults) {
                ps.setLong(1, userId);
                ps.setString(2, row[0]);
                ps.setString(3, row[1]);
                ps.setBigDecimal(4, "EXPENSE".equals(row[1]) ? new BigDecimal("500") : BigDecimal.ZERO);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setName(rs.getString("name"));
        c.setType(TransactionType.fromString(rs.getString("type")));
        c.setBudgetLimit(rs.getBigDecimal("budget_limit"));
        return c;
    }
}
