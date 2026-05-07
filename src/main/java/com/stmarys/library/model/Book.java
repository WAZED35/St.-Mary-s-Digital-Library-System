package com.stmarys.library.model;

/** Represents a book stored in St Mary's Digital Library. */
public class Book {
    private int bookId;
    private String title;
    private String author;
    private String category;
    private String availabilityStatus;

    /** Creates a book object. */
    public Book(int bookId, String title, String author, String category, String availabilityStatus) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.availabilityStatus = availabilityStatus;
    }

    /** Returns the book identifier. */
    public int getBookId() { return bookId; }
    /** Returns the book title. */
    public String getTitle() { return title; }
    /** Returns the author name. */
    public String getAuthor() { return author; }
    /** Returns the category. */
    public String getCategory() { return category; }
    /** Returns availability status. */
    public String getAvailabilityStatus() { return availabilityStatus; }

    /** Updates the title. */
    public void setTitle(String title) { this.title = title; }
    /** Updates the author. */
    public void setAuthor(String author) { this.author = author; }
    /** Updates the category. */
    public void setCategory(String category) { this.category = category; }
    /** Updates availability status. */
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    /** Returns a readable book description for console output. */
    @Override
    public String toString() {
        return bookId + " | " + title + " | " + author + " | " + category + " | " + availabilityStatus;
    }
}
