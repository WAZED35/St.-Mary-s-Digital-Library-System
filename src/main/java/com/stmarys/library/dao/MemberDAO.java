package com.stmarys.library.dao;

import com.stmarys.library.db.DatabaseManager;
import com.stmarys.library.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access object for library member records. */
public class MemberDAO {
    /** Inserts a new member. */
    public void add(Member member) throws SQLException {
        String sql = "INSERT INTO members(member_name, email, membership_type) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getMemberName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getMembershipType());
            ps.executeUpdate();
        }
    }

    /** Returns all members. */
    public List<Member> findAll() throws SQLException {
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM members ORDER BY member_id")) {
            while (rs.next()) {
                members.add(map(rs));
            }
        }
        return members;
    }

    /** Searches members by identifier, name, email or membership type. */
    public List<Member> search(String term) throws SQLException {
        String searchTerm = "%" + term + "%";
        String sql = """
                SELECT * FROM members
                WHERE CAST(member_id AS TEXT) LIKE ?
                   OR member_name LIKE ?
                   OR email LIKE ?
                   OR membership_type LIKE ?
                ORDER BY member_name ASC
                """;
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, searchTerm);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(map(rs));
                }
            }
        }
        return members;
    }

    /** Updates an existing member. */
    public void update(Member member) throws SQLException {
        String sql = "UPDATE members SET member_name=?, email=?, membership_type=? WHERE member_id=?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getMemberName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getMembershipType());
            ps.setInt(4, member.getMemberId());
            ps.executeUpdate();
        }
    }

    /** Deletes a member by identifier. */
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM members WHERE member_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Member map(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("member_id"),
                rs.getString("member_name"),
                rs.getString("email"),
                rs.getString("membership_type")
        );
    }
}
