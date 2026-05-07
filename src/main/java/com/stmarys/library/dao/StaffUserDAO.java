package com.stmarys.library.dao;

import com.stmarys.library.db.DatabaseManager;
import com.stmarys.library.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Authenticates staff users against hashed credentials. */
public class StaffUserDAO {
    /** Returns true when a staff username and password are valid. */
    public boolean validateLogin(String username, String password) {
        String sql = "SELECT 1 FROM staff_users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, PasswordUtil.hashPassword(password.trim()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
