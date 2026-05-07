package com.stmarys.library.model;

import java.time.LocalDate;

/** Represents a book borrowing transaction. */
public class BorrowRecord {
    private int recordId;
    private int bookId;
    private int memberId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String returnStatus;

    /** Creates a borrowing record. */
    public BorrowRecord(int recordId, int bookId, int memberId, LocalDate borrowDate, LocalDate dueDate, String returnStatus) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnStatus = returnStatus;
    }

    /** Returns the record identifier. */
    public int getRecordId() { return recordId; }
    /** Returns the borrowed book identifier. */
    public int getBookId() { return bookId; }
    /** Returns the borrowing member identifier. */
    public int getMemberId() { return memberId; }
    /** Returns the borrow date. */
    public LocalDate getBorrowDate() { return borrowDate; }
    /** Returns the due date. */
    public LocalDate getDueDate() { return dueDate; }
    /** Returns the return status. */
    public String getReturnStatus() { return returnStatus; }

    /** Updates the book identifier. */
    public void setBookId(int bookId) { this.bookId = bookId; }
    /** Updates the member identifier. */
    public void setMemberId(int memberId) { this.memberId = memberId; }
    /** Updates the borrow date. */
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }
    /** Updates the due date. */
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    /** Updates the return status. */
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    /** Returns a readable record description for console output. */
    @Override
    public String toString() {
        return recordId + " | book=" + bookId + " | member=" + memberId + " | " + borrowDate + " | " + dueDate + " | " + returnStatus;
    }
}
