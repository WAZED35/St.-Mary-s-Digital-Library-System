package com.stmarys.library.dao;

import com.stmarys.library.db.DatabaseManager;
import com.stmarys.library.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access object for book records. */
public class BookDAO {
    /** Inserts a new book. */
    public void add(Book book) throws SQLException {
        String sql = "INSERT INTO books(title, author, category, availability_status) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setString(4, book.getAvailabilityStatus());
            ps.executeUpdate();
        }
    }

    /** Returns all books sorted by identifier. */
    public List<Book> findAll() throws SQLException {
        return query("SELECT * FROM books ORDER BY book_id");
    }

    /** Searches books by ID, title, author, category or status. */
    public List<Book> search(String term) throws SQLException {
        String searchTerm = "%" + term + "%";
        String sql = """
                SELECT * FROM books
                WHERE CAST(book_id AS TEXT) LIKE ?
                   OR title LIKE ?
                   OR author LIKE ?
                   OR category LIKE ?
                   OR availability_status LIKE ?
                ORDER BY title ASC
                """;
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, searchTerm);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(map(rs));
                }
            }
        }
        return books;
    }

    /** Returns books in one selected category. */
    public List<Book> filterByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM books WHERE category LIKE ? ORDER BY title ASC";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + category + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(map(rs));
                }
            }
        }
        return books;
    }

    /** Updates an existing book. */
    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title=?, author=?, category=?, availability_status=? WHERE book_id=?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setString(4, book.getAvailabilityStatus());
            ps.setInt(5, book.getBookId());
            ps.executeUpdate();
        }
    }

    /** Deletes a book by identifier. */
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Updates a book availability status. */
    public void setAvailability(int id, String status) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE books SET availability_status=? WHERE book_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private List<Book> query(String sql) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                books.add(map(rs));
            }
        }
        return books;
    }

    private Book map(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getString("availability_status")
        );
    }
}
