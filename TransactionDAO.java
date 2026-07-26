package com.smartfinance.dao;

import com.smartfinance.model.Transaction;
import com.smartfinance.model.TransactionType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data access for transactions table.
 */
public class TransactionDAO {

    public List<Transaction> findByUserId(long userId) throws SQLException {
        return search(userId, null, null, null, null);
    }

    /**
     * Search and filter transactions using optional criteria.
     */
    public List<Transaction> search(Long userId, LocalDate from, LocalDate to,
                                    Long categoryId, TransactionType type) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, c.name AS category_name FROM transactions t " +
                "JOIN categories c ON t.category_id = c.id WHERE t.user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (from != null) {
            sql.append(" AND t.txn_date >= ?");
            params.add(Date.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND t.txn_date <= ?");
            params.add(Date.valueOf(to));
        }
        if (categoryId != null) {
            sql.append(" AND t.category_id = ?");
            params.add(categoryId);
        }
        if (type != null) {
            sql.append(" AND t.type = ?");
            params.add(type.name());
        }
        sql.append(" ORDER BY t.txn_date DESC, t.id DESC");

        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Date d) {
                    ps.setDate(i + 1, d);
                } else if (p instanceof Long l) {
                    ps.setLong(i + 1, l);
                } else if (p instanceof String s) {
                    ps.setString(i + 1, s);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<Transaction> findById(long id) throws SQLException {
        String sql = "SELECT t.*, c.name AS category_name FROM transactions t " +
                     "JOIN categories c ON t.category_id = c.id WHERE t.id = ?";
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

    public Transaction insert(Transaction txn) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, category_id, amount, type, description, txn_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, txn.getUserId());
            ps.setLong(2, txn.getCategoryId());
            ps.setBigDecimal(3, txn.getAmount());
            ps.setString(4, txn.getType().name());
            ps.setString(5, txn.getDescription());
            ps.setDate(6, Date.valueOf(txn.getTxnDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    txn.setId(keys.getLong(1));
                }
            }
        }
        return txn;
    }

    public boolean update(Transaction txn) throws SQLException {
        String sql = "UPDATE transactions SET category_id=?, amount=?, type=?, description=?, txn_date=? WHERE id=? AND user_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, txn.getCategoryId());
            ps.setBigDecimal(2, txn.getAmount());
            ps.setString(3, txn.getType().name());
            ps.setString(4, txn.getDescription());
            ps.setDate(5, Date.valueOf(txn.getTxnDate()));
            ps.setLong(6, txn.getId());
            ps.setLong(7, txn.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id, long userId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public BigDecimal sumByCategoryAndMonth(long userId, long categoryId, int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                     "WHERE user_id=? AND category_id=? AND type='EXPENSE' " +
                     "AND YEAR(txn_date)=? AND MONTH(txn_date)=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, categoryId);
            ps.setInt(3, year);
            ps.setInt(4, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setId(rs.getLong("id"));
        t.setUserId(rs.getLong("user_id"));
        t.setCategoryId(rs.getLong("category_id"));
        t.setCategoryName(rs.getString("category_name"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setType(TransactionType.fromString(rs.getString("type")));
        t.setDescription(rs.getString("description"));
        t.setTxnDate(rs.getDate("txn_date").toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            t.setCreatedAt(ts.toLocalDateTime());
        }
        return t;
    }
}
