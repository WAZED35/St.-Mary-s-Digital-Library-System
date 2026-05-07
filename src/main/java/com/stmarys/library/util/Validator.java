package com.stmarys.library.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Centralised validation helper for console and graphical user interfaces. */
public final class Validator {
    private Validator() {
    }

    /** Ensures text is not blank. */
    public static void notBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
    }

    /** Ensures an email address has a valid basic format. */
    public static void email(String email) {
        notBlank(email, "Email");
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Email address is invalid.");
        }
    }

    /** Parses and validates an ISO local date in YYYY-MM-DD format. */
    public static LocalDate date(String value, String field) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " must be in YYYY-MM-DD format.");
        }
    }

    /** Parses and validates a positive integer. */
    public static int positiveInt(String value, String field) {
        try {
            int number = Integer.parseInt(value.trim());
            if (number <= 0) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be a positive number.");
        }
    }

    /** Ensures the due date is after the borrow date. */
    public static void dueAfterBorrow(LocalDate borrowDate, LocalDate dueDate) {
        if (!dueDate.isAfter(borrowDate)) {
            throw new IllegalArgumentException("Due date must be later than borrow date.");
        }
    }
}
