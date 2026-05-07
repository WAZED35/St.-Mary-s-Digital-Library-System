package com.stmarys.library.dao;

import com.stmarys.library.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Stores and retrieves audit trail events from the SQLite database. */
public class AuditDAO {
    /** Adds an audit event. */
    public void log(String username, String action, String details) {
        String sql = "INSERT INTO audit_logs(username, action, details) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username == null || username.isBlank() ? "unknown" : username);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns all audit events in reverse chronological order. */
    public List<Object[]> findAll() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT id, username, action, details, created_at FROM audit_logs ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(row(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /** Searches audit events by username, action or details. */
    public List<Object[]> search(String term) {
        List<Object[]> rows = new ArrayList<>();
        String sql = """
                SELECT id, username, action, details, created_at FROM audit_logs
                WHERE username LIKE ? OR action LIKE ? OR details LIKE ?
                ORDER BY id DESC
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            String search = "%" + term + "%";
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(row(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private Object[] row(ResultSet rs) throws SQLException {
        return new Object[]{
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("action"),
                rs.getString("details"),
                rs.getString("created_at")
        };
    }
}
