package com.stmarys.library.dao;

import com.stmarys.library.db.DatabaseManager;
import com.stmarys.library.model.BorrowRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data access object for borrowing transaction records. */
public class BorrowRecordDAO {
    /** Inserts a new borrowing record. */
    public void add(BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records(book_id, member_id, borrow_date, due_date, return_status) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getBookId());
            ps.setInt(2, record.getMemberId());
            ps.setString(3, record.getBorrowDate().toString());
            ps.setString(4, record.getDueDate().toString());
            ps.setString(5, record.getReturnStatus());
            ps.executeUpdate();
        }
    }

    /** Returns all borrowing records. */
    public List<BorrowRecord> findAll() throws SQLException {
        return query("SELECT * FROM borrow_records ORDER BY record_id");
    }

    /** Searches borrowing records by identifiers or return status. */
    public List<BorrowRecord> search(String term) throws SQLException {
        String searchTerm = "%" + term + "%";
        String sql = """
                SELECT * FROM borrow_records
                WHERE CAST(record_id AS TEXT) LIKE ?
                   OR CAST(book_id AS TEXT) LIKE ?
                   OR CAST(member_id AS TEXT) LIKE ?
                   OR return_status LIKE ?
                ORDER BY due_date ASC
                """;
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, searchTerm);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
        }
        return records;
    }

    /** Returns records due between the selected dates inclusive. */
    public List<BorrowRecord> filterByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE due_date BETWEEN ? AND ? ORDER BY due_date ASC";
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(map(rs));
                }
            }
        }
        return records;
    }

    /** Returns records which are overdue and not returned. */
    public List<BorrowRecord> overdue() throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE due_date < date('now') AND return_status <> 'Returned' ORDER BY due_date ASC";
        return query(sql);
    }

    /** Updates an existing borrowing record. */
    public void update(BorrowRecord record) throws SQLException {
        String sql = "UPDATE borrow_records SET book_id=?, member_id=?, borrow_date=?, due_date=?, return_status=? WHERE record_id=?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getBookId());
            ps.setInt(2, record.getMemberId());
            ps.setString(3, record.getBorrowDate().toString());
            ps.setString(4, record.getDueDate().toString());
            ps.setString(5, record.getReturnStatus());
            ps.setInt(6, record.getRecordId());
            ps.executeUpdate();
        }
    }

    /** Updates only the return status of a borrowing record. */
    public void updateStatus(int recordId, String status) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE borrow_records SET return_status=? WHERE record_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, recordId);
            ps.executeUpdate();
        }
    }

    /** Deletes a borrowing record by identifier. */
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM borrow_records WHERE record_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Finds a borrowing record by identifier. */
    public BorrowRecord findById(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM borrow_records WHERE record_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private List<BorrowRecord> query(String sql) throws SQLException {
        List<BorrowRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                records.add(map(rs));
            }
        }
        return records;
    }

    private BorrowRecord map(ResultSet rs) throws SQLException {
        return new BorrowRecord(
                rs.getInt("record_id"),
                rs.getInt("book_id"),
                rs.getInt("member_id"),
                LocalDate.parse(rs.getString("borrow_date")),
                LocalDate.parse(rs.getString("due_date")),
                rs.getString("return_status")
        );
    }
}
