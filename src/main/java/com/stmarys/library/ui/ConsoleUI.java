package com.stmarys.library.ui;

import com.stmarys.library.dao.BookDAO;
import com.stmarys.library.dao.BorrowRecordDAO;
import com.stmarys.library.dao.MemberDAO;
import com.stmarys.library.model.Book;
import com.stmarys.library.model.BorrowRecord;
import com.stmarys.library.model.Member;
import com.stmarys.library.util.Validator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

/** Text-based console interface for the library system. */
public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();

    /** Starts the menu loop for console-based interaction. */
    public void start() {
        while (true) {
            System.out.println("\nST MARY'S DIGITAL LIBRARY");
            System.out.println("1. Manage Books");
            System.out.println("2. Manage Members");
            System.out.println("3. Manage Borrowing Records");
            System.out.println("4. Search Records");
            System.out.println("5. Exit");
            String choice = ask("Choose option");
            try {
                switch (choice) {
                    case "1" -> manageBooks();
                    case "2" -> manageMembers();
                    case "3" -> manageBorrowingRecords();
                    case "4" -> searchRecords();
                    case "5" -> { return; }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void manageBooks() throws SQLException {
        System.out.println("1 Add  2 View  3 Update  4 Delete");
        switch (ask("Choose option")) {
            case "1" -> {
                String title = ask("Title");
                String author = ask("Author");
                String category = ask("Category");
                String status = ask("Status");
                Validator.notBlank(title, "Title");
                Validator.notBlank(author, "Author");
                Validator.notBlank(category, "Category");
                bookDAO.add(new Book(0, title, author, category, status));
            }
            case "2" -> bookDAO.findAll().forEach(System.out::println);
            case "3" -> {
                int id = Validator.positiveInt(ask("Book ID"), "Book ID");
                bookDAO.update(new Book(id, ask("Title"), ask("Author"), ask("Category"), ask("Status")));
            }
            case "4" -> bookDAO.delete(Validator.positiveInt(ask("Book ID"), "Book ID"));
            default -> System.out.println("Invalid option.");
        }
    }

    private void manageMembers() throws SQLException {
        System.out.println("1 Add  2 View  3 Update  4 Delete");
        switch (ask("Choose option")) {
            case "1" -> {
                String name = ask("Name");
                String email = ask("Email");
                String type = ask("Membership Type");
                Validator.notBlank(name, "Name");
                Validator.email(email);
                memberDAO.add(new Member(0, name, email, type));
            }
            case "2" -> memberDAO.findAll().forEach(System.out::println);
            case "3" -> {
                int id = Validator.positiveInt(ask("Member ID"), "Member ID");
                String email = ask("Email");
                Validator.email(email);
                memberDAO.update(new Member(id, ask("Name"), email, ask("Membership Type")));
            }
            case "4" -> memberDAO.delete(Validator.positiveInt(ask("Member ID"), "Member ID"));
            default -> System.out.println("Invalid option.");
        }
    }

    private void manageBorrowingRecords() throws SQLException {
        System.out.println("1 Add  2 View  3 Update  4 Delete  5 Overdue  6 Date Range");
        switch (ask("Choose option")) {
            case "1" -> borrowRecordDAO.add(buildRecord(0));
            case "2" -> borrowRecordDAO.findAll().forEach(System.out::println);
            case "3" -> borrowRecordDAO.update(buildRecord(Validator.positiveInt(ask("Record ID"), "Record ID")));
            case "4" -> borrowRecordDAO.delete(Validator.positiveInt(ask("Record ID"), "Record ID"));
            case "5" -> borrowRecordDAO.overdue().forEach(System.out::println);
            case "6" -> {
                LocalDate start = Validator.date(ask("Start due date YYYY-MM-DD"), "Start date");
                LocalDate end = Validator.date(ask("End due date YYYY-MM-DD"), "End date");
                borrowRecordDAO.filterByDateRange(start, end).forEach(System.out::println);
            }
            default -> System.out.println("Invalid option.");
        }
    }

    private BorrowRecord buildRecord(int id) {
        int bookId = Validator.positiveInt(ask("Book ID"), "Book ID");
        int memberId = Validator.positiveInt(ask("Member ID"), "Member ID");
        LocalDate borrowDate = Validator.date(ask("Borrow date YYYY-MM-DD"), "Borrow date");
        LocalDate dueDate = Validator.date(ask("Due date YYYY-MM-DD"), "Due date");
        Validator.dueAfterBorrow(borrowDate, dueDate);
        return new BorrowRecord(id, bookId, memberId, borrowDate, dueDate, ask("Status"));
    }

    private void searchRecords() throws SQLException {
        String term = ask("Search term");
        System.out.println("Books:");
        bookDAO.search(term).forEach(System.out::println);
        System.out.println("Members:");
        memberDAO.search(term).forEach(System.out::println);
        System.out.println("Borrowing Records:");
        borrowRecordDAO.search(term).forEach(System.out::println);
    }

    private String ask(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }
}
