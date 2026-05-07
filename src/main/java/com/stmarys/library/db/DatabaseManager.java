package com.stmarys.library.db;

import com.stmarys.library.util.PasswordUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/** Manages SQLite connectivity, schema creation, seed data and performance indexes. */
public final class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/library.db";

    private DatabaseManager() {
    }

    /** Opens a connection to the SQLite database. */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver was not found.", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    /** Creates all required database tables, seed users, sample data and indexes. */
    public static void initializeDatabase() {
        new File("database").mkdirs();
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");

            st.execute("""
                    CREATE TABLE IF NOT EXISTS books (
                        book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        category TEXT NOT NULL,
                        availability_status TEXT NOT NULL
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS members (
                        member_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        member_name TEXT NOT NULL,
                        email TEXT NOT NULL UNIQUE,
                        membership_type TEXT NOT NULL
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS borrow_records (
                        record_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        book_id INTEGER NOT NULL,
                        member_id INTEGER NOT NULL,
                        borrow_date DATE NOT NULL,
                        due_date DATE NOT NULL,
                        return_status TEXT NOT NULL,
                        FOREIGN KEY(book_id) REFERENCES books(book_id),
                        FOREIGN KEY(member_id) REFERENCES members(member_id)
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS staff_users (
                        staff_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'Admin'
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS student_users (
                        student_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        course TEXT
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL,
                        action TEXT NOT NULL,
                        details TEXT,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            createIndexes(st);
            seedUsers(conn);
            seedSampleData(conn);
            updateOverdueRecords(conn);
        } catch (SQLException ex) {
            System.err.println("Database initialisation failed: " + ex.getMessage());
        }
    }

    private static void createIndexes(Statement st) throws SQLException {
        st.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_books_author ON books(author)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_books_category ON books(category)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_members_name ON members(member_name)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_borrow_book ON borrow_records(book_id)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_borrow_member ON borrow_records(member_id)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_borrow_due ON borrow_records(due_date)");
        st.execute("CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_logs(created_at)");
    }

    private static void seedUsers(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT OR IGNORE INTO staff_users(username, password, full_name, role)
                VALUES (?, ?, ?, ?)
                """)) {
            ps.setString(1, "admin");
            ps.setString(2, PasswordUtil.hashPassword("admin123"));
            ps.setString(3, "System Administrator");
            ps.setString(4, "Admin");
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT OR IGNORE INTO student_users(username, password, full_name, course)
                VALUES (?, ?, ?, ?)
                """)) {
            ps.setString(1, "student");
            ps.setString(2, PasswordUtil.hashPassword("student123"));
            ps.setString(3, "Demo Student");
            ps.setString(4, "Computer Science");
            ps.executeUpdate();
        }
    }

    private static void seedSampleData(Connection conn) throws SQLException {
        if (hasRows(conn, "books")) {
            return;
        }

        try (PreparedStatement b = conn.prepareStatement("INSERT INTO books(title, author, category, availability_status) VALUES(?, ?, ?, ?)");
             PreparedStatement m = conn.prepareStatement("INSERT INTO members(member_name, email, membership_type) VALUES(?, ?, ?)");
             PreparedStatement r = conn.prepareStatement("INSERT INTO borrow_records(book_id, member_id, borrow_date, due_date, return_status) VALUES(?, ?, ?, ?, ?)")) {

            Object[][] books = {
                    {"Introduction to Java", "John Smith", "Programming", "Available"},
                    {"Database Systems", "Maria Garcia", "Computer Science", "Borrowed"},
                    {"Software Engineering Principles", "Alan Brown", "Engineering", "Available"},
                    {"Clean Code", "Robert C. Martin", "Programming", "Available"},
                    {"Computer Networks", "Andrew S. Tanenbaum", "Networking", "Available"}
            };
            for (Object[] row : books) {
                b.setString(1, row[0].toString());
                b.setString(2, row[1].toString());
                b.setString(3, row[2].toString());
                b.setString(4, row[3].toString());
                b.addBatch();
            }
            b.executeBatch();

            Object[][] members = {
                    {"Alice Johnson", "alice.johnson@stmarys.ac.uk", "Student"},
                    {"Michael Lee", "michael.lee@stmarys.ac.uk", "Staff"},
                    {"Sara Ahmed", "sara.ahmed@stmarys.ac.uk", "Student"},
                    {"James Wilson", "james.wilson@stmarys.ac.uk", "Student"},
                    {"Emily Davis", "emily.davis@stmarys.ac.uk", "Student"}
            };
            for (Object[] row : members) {
                m.setString(1, row[0].toString());
                m.setString(2, row[1].toString());
                m.setString(3, row[2].toString());
                m.addBatch();
            }
            m.executeBatch();

            Object[][] records = {
                    {2, 1, "2025-03-01", "2025-03-15", "Borrowed"},
                    {1, 2, "2025-03-02", "2025-03-16", "Returned"},
                    {3, 3, "2025-03-05", "2025-03-19", "Borrowed"}
            };
            for (Object[] row : records) {
                r.setInt(1, (Integer) row[0]);
                r.setInt(2, (Integer) row[1]);
                r.setString(3, row[2].toString());
                r.setString(4, row[3].toString());
                r.setString(5, row[4].toString());
                r.addBatch();
            }
            r.executeBatch();
        }
    }

    private static boolean hasRows(Connection conn, String tableName) throws SQLException {
        try (Statement st = conn.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static void updateOverdueRecords(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE borrow_records
                SET return_status = 'Overdue'
                WHERE due_date < date('now')
                AND return_status = 'Borrowed'
                """)) {
            ps.executeUpdate();
        }
    }
}
