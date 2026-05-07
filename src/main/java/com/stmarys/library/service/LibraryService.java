package com.stmarys.library.service;

import com.stmarys.library.dao.BookDAO;
import com.stmarys.library.dao.BorrowRecordDAO;
import com.stmarys.library.model.BorrowRecord;

import java.sql.SQLException;

/** Coordinates library operations that involve multiple DAO classes. */
public class LibraryService {
    private final BookDAO bookDAO = new BookDAO();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();

    /** Marks a book as borrowed after a borrow record is created. */
    public void borrowBook(BorrowRecord record) throws SQLException {
        borrowRecordDAO.add(record);
        bookDAO.setAvailability(record.getBookId(), "Borrowed");
    }

    /**
     * Marks a borrowing transaction as returned while preserving the original
     * member ID, book ID, borrow date and due date.
     */
    public void returnBook(int recordId) throws SQLException {
        BorrowRecord existing = borrowRecordDAO.findById(recordId);
        if (existing == null) {
            throw new SQLException("Borrowing record was not found.");
        }
        borrowRecordDAO.updateStatus(recordId, "Returned");
        bookDAO.setAvailability(existing.getBookId(), "Available");
    }
}
