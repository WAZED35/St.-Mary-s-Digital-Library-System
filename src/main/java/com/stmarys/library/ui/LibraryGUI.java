package com.stmarys.library.ui;

import com.stmarys.library.dao.AuditDAO;
import com.stmarys.library.dao.BookDAO;
import com.stmarys.library.dao.BorrowRecordDAO;
import com.stmarys.library.dao.MemberDAO;
import com.stmarys.library.model.Book;
import com.stmarys.library.model.BorrowRecord;
import com.stmarys.library.model.Member;
import com.stmarys.library.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Main Swing dashboard for managing books, members, borrowing records and audit trail. */
public class LibraryGUI extends JFrame {
    private final BookDAO bookDAO = new BookDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final BorrowRecordDAO borrowRecordDAO = new BorrowRecordDAO();
    private final AuditDAO auditDAO = new AuditDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JPanel dateFilterPanel;
    private JLabel titleLabel;
    private JLabel userLabel;
    private JLabel totalBooksValue;
    private JLabel totalMembersValue;
    private JLabel borrowRecordsValue;

    private String currentModule = "Books";
    private final String currentUser;
    private final String currentRole;

    private final Color darkBlue = new Color(0, 58, 92);
    private final Color blue = new Color(0, 126, 168);
    private final Color orange = new Color(245, 145, 0);
    private final Color lightBackground = new Color(245, 247, 250);
    private final Color red = new Color(220, 53, 69);
    private final Color green = new Color(40, 167, 69);

    /** Opens the dashboard as the default administrator user. */
    public LibraryGUI() {
        this("admin", "Staff");
    }

    /** Opens the dashboard for a specific authenticated user. */
    public LibraryGUI(String username, String role) {
        this.currentUser = username;
        this.currentRole = role;

        setTitle("St Mary's Digital Library System");
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(lightBackground);
        add(main);
        main.add(createSidebar(), BorderLayout.WEST);
        main.add(createContent(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                auditDAO.log(currentUser, "WINDOW_CLOSE", "Dashboard window closed");
            }
        });

        loadBooksAsync("");
        refreshStatsAsync();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new GridLayout(10, 1, 0, 8));
        sidebar.setPreferredSize(new Dimension(220, 720));
        sidebar.setBackground(darkBlue);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 18, 24, 18));

        JLabel logo = new JLabel("<html><center>St Mary's<br>Library</center></html>", SwingConstants.CENTER);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sidebar.add(logo);

        JButton booksBtn = menuButton("▣  Books");
        JButton membersBtn = menuButton("●  Members");
        JButton borrowBtn = menuButton("▤  Borrowing");
        JButton auditBtn = menuButton("◇  Audit");
        JButton logoutBtn = menuButton("↩  Logout");

        booksBtn.addActionListener(e -> switchModule("Books"));
        membersBtn.addActionListener(e -> switchModule("Members"));
        borrowBtn.addActionListener(e -> switchModule("Borrowing"));
        auditBtn.addActionListener(e -> switchModule("Audit"));
        logoutBtn.addActionListener(e -> logout());

        sidebar.add(booksBtn);
        sidebar.add(membersBtn);
        sidebar.add(borrowBtn);
        sidebar.add(auditBtn);
        sidebar.add(logoutBtn);
        return sidebar;
    }

    private JButton menuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(darkBlue);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(lightBackground);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(lightBackground);

        titleLabel = new JLabel("Books Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(darkBlue);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTop.setBackground(lightBackground);
        searchField = new JTextField(22);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton searchBtn = actionButton("Search", blue);
        JButton clearBtn = actionButton("Clear", new Color(108, 117, 125));
        searchBtn.addActionListener(e -> searchCurrentModule());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            refreshCurrentModule();
        });
        userLabel = new JLabel("  User: " + currentUser + " (" + currentRole + ")");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userLabel.setForeground(darkBlue);
        rightTop.add(searchField);
        rightTop.add(searchBtn);
        rightTop.add(clearBtn);
        rightTop.add(userLabel);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);

        totalBooksValue = statNumber();
        totalMembersValue = statNumber();
        borrowRecordsValue = statNumber();
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        statsPanel.setBackground(lightBackground);
        statsPanel.add(statCard("Total Books", totalBooksValue));
        statsPanel.add(statCard("Total Members", totalMembersValue));
        statsPanel.add(statCard("Borrow Records", borrowRecordsValue));

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(darkBlue);
        table.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(lightBackground);
        JButton addBtn = actionButton("Add", green);
        JButton updateBtn = actionButton("Update", blue);
        JButton deleteBtn = actionButton("Delete", red);
        JButton refreshBtn = actionButton("Refresh", orange);
        addBtn.addActionListener(e -> addCurrentModule());
        updateBtn.addActionListener(e -> updateCurrentModule());
        deleteBtn.addActionListener(e -> deleteCurrentModule());
        refreshBtn.addActionListener(e -> refreshCurrentModule());
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateFilterPanel.setBackground(lightBackground);
        startDateField = new JTextField("2025-01-01", 10);
        endDateField = new JTextField("2026-12-31", 10);
        JButton filterDatesBtn = actionButton("Filter Due Dates", blue);
        JButton overdueBtn = actionButton("Overdue Only", red);
        filterDatesBtn.addActionListener(e -> filterBorrowRecordsByDateRange());
        overdueBtn.addActionListener(e -> loadOverdueRecordsAsync());
        dateFilterPanel.add(new JLabel("Due from:"));
        dateFilterPanel.add(startDateField);
        dateFilterPanel.add(new JLabel("to:"));
        dateFilterPanel.add(endDateField);
        dateFilterPanel.add(filterDatesBtn);
        dateFilterPanel.add(overdueBtn);
        dateFilterPanel.setVisible(false);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(lightBackground);
        south.add(dateFilterPanel, BorderLayout.WEST);
        south.add(buttonPanel, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(15, 15));
        center.setBackground(lightBackground);
        center.add(statsPanel, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(south, BorderLayout.SOUTH);

        content.add(topBar, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        return content;
    }

    private JLabel statNumber() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 26));
        label.setForeground(blue);
        return label;
    }

    private JPanel statCard(String text, JLabel number) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);
        card.add(number);
        card.add(label);
        return card;
    }

    private JButton actionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void switchModule(String module) {
        currentModule = module;
        titleLabel.setText(module.equals("Books") ? "Books Management" : module.equals("Members") ? "Members Management" : module.equals("Borrowing") ? "Borrowing Records" : "Audit Trail");
        setDateFilterVisible(module.equals("Borrowing"));
        refreshCurrentModule();
    }

    private void setDateFilterVisible(boolean visible) {
        if (dateFilterPanel != null) {
            dateFilterPanel.setVisible(visible);
            dateFilterPanel.revalidate();
            dateFilterPanel.repaint();
        }
    }

    private void refreshCurrentModule() {
        if (currentModule.equals("Books")) {
            loadBooksAsync("");
        } else if (currentModule.equals("Members")) {
            loadMembersAsync("");
        } else if (currentModule.equals("Borrowing")) {
            loadBorrowRecordsAsync("");
        } else {
            loadAuditAsync("");
        }
        refreshStatsAsync();
    }

    private void searchCurrentModule() {
        String keyword = searchField.getText().trim();
        if (currentModule.equals("Books")) {
            loadBooksAsync(keyword);
        } else if (currentModule.equals("Members")) {
            loadMembersAsync(keyword);
        } else if (currentModule.equals("Borrowing")) {
            loadBorrowRecordsAsync(keyword);
        } else {
            loadAuditAsync(keyword);
        }
    }

    private void refreshStatsAsync() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                return new int[]{bookDAO.findAll().size(), memberDAO.findAll().size(), borrowRecordDAO.findAll().size()};
            }

            @Override
            protected void done() {
                try {
                    int[] counts = get();
                    totalBooksValue.setText(String.valueOf(counts[0]));
                    totalMembersValue.setText(String.valueOf(counts[1]));
                    borrowRecordsValue.setText(String.valueOf(counts[2]));
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void loadBooksAsync(String keyword) {
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Title", "Author", "Category", "Status"});
        tableModel.setRowCount(0);
        new SwingWorker<List<Book>, Void>() {
            @Override
            protected List<Book> doInBackground() throws Exception {
                return keyword.isBlank() ? bookDAO.findAll() : bookDAO.search(keyword);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Book book : get()) {
                        tableModel.addRow(new Object[]{book.getBookId(), book.getTitle(), book.getAuthor(), book.getCategory(), book.getAvailabilityStatus()});
                    }
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void loadMembersAsync(String keyword) {
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Name", "Email", "Membership Type"});
        tableModel.setRowCount(0);
        new SwingWorker<List<Member>, Void>() {
            @Override
            protected List<Member> doInBackground() throws Exception {
                return keyword.isBlank() ? memberDAO.findAll() : memberDAO.search(keyword);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Member member : get()) {
                        tableModel.addRow(new Object[]{member.getMemberId(), member.getMemberName(), member.getEmail(), member.getMembershipType()});
                    }
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void loadBorrowRecordsAsync(String keyword) {
        tableModel.setColumnIdentifiers(new Object[]{"Record ID", "Book ID", "Member ID", "Borrow Date", "Due Date", "Status"});
        tableModel.setRowCount(0);
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                return keyword.isBlank() ? borrowRecordDAO.findAll() : borrowRecordDAO.search(keyword);
            }

            @Override
            protected void done() {
                loadBorrowRows(this);
            }
        }.execute();
    }

    private void loadOverdueRecordsAsync() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                return borrowRecordDAO.overdue();
            }

            @Override
            protected void done() {
                loadBorrowRows(this);
            }
        }.execute();
    }

    private void filterBorrowRecordsByDateRange() {
        try {
            LocalDate start = Validator.date(startDateField.getText(), "Start date");
            LocalDate end = Validator.date(endDateField.getText(), "End date");
            if (end.isBefore(start)) {
                JOptionPane.showMessageDialog(this, "End date must be after start date.");
                return;
            }
            new SwingWorker<List<BorrowRecord>, Void>() {
                @Override
                protected List<BorrowRecord> doInBackground() throws Exception {
                    return borrowRecordDAO.filterByDateRange(start, end);
                }

                @Override
                protected void done() {
                    loadBorrowRows(this);
                }
            }.execute();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void loadBorrowRows(SwingWorker<List<BorrowRecord>, Void> worker) {
        try {
            tableModel.setColumnIdentifiers(new Object[]{"Record ID", "Book ID", "Member ID", "Borrow Date", "Due Date", "Status"});
            tableModel.setRowCount(0);
            for (BorrowRecord record : worker.get()) {
                tableModel.addRow(new Object[]{record.getRecordId(), record.getBookId(), record.getMemberId(), record.getBorrowDate(), record.getDueDate(), record.getReturnStatus()});
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    private void loadAuditAsync(String keyword) {
        tableModel.setColumnIdentifiers(new Object[]{"ID", "Username", "Action", "Details", "Created At"});
        tableModel.setRowCount(0);
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() {
                return keyword.isBlank() ? auditDAO.findAll() : auditDAO.search(keyword);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Object[] row : get()) {
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    showError(e);
                }
            }
        }.execute();
    }

    private void addCurrentModule() {
        try {
            if (currentModule.equals("Books")) addBook();
            else if (currentModule.equals("Members")) addMember();
            else if (currentModule.equals("Borrowing")) addBorrowRecord();
            else JOptionPane.showMessageDialog(this, "Audit records are read-only.");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void updateCurrentModule() {
        try {
            if (currentModule.equals("Books")) updateBook();
            else if (currentModule.equals("Members")) updateMember();
            else if (currentModule.equals("Borrowing")) updateBorrowRecord();
            else JOptionPane.showMessageDialog(this, "Audit records are read-only.");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void deleteCurrentModule() {
        try {
            if (currentModule.equals("Books")) deleteBook();
            else if (currentModule.equals("Members")) deleteMember();
            else if (currentModule.equals("Borrowing")) deleteBorrowRecord();
            else JOptionPane.showMessageDialog(this, "Audit records are read-only.");
        } catch (Exception e) {
            showError(e);
        }
    }

    private void addBook() throws SQLException {
        JTextField title = new JTextField();
        JTextField author = new JTextField();
        JTextField category = new JTextField();
        JComboBox<String> status = new JComboBox<>(new String[]{"Available", "Borrowed"});
        Object[] form = {"Title:", title, "Author:", author, "Category:", category, "Status:", status};
        if (JOptionPane.showConfirmDialog(this, form, "Add Book", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            validateBook(title, author, category);
            bookDAO.add(new Book(0, title.getText().trim(), author.getText().trim(), category.getText().trim(), status.getSelectedItem().toString()));
            auditDAO.log(currentUser, "ADD_BOOK", "Title: " + title.getText().trim());
            JOptionPane.showMessageDialog(this, "Book added successfully");
            refreshCurrentModule();
        }
    }

    private void updateBook() throws SQLException {
        int row = selectedRow();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        JTextField title = new JTextField(tableModel.getValueAt(row, 1).toString());
        JTextField author = new JTextField(tableModel.getValueAt(row, 2).toString());
        JTextField category = new JTextField(tableModel.getValueAt(row, 3).toString());
        JComboBox<String> status = new JComboBox<>(new String[]{"Available", "Borrowed"});
        status.setSelectedItem(tableModel.getValueAt(row, 4).toString());
        Object[] form = {"Title:", title, "Author:", author, "Category:", category, "Status:", status};
        if (JOptionPane.showConfirmDialog(this, form, "Update Book", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            validateBook(title, author, category);
            bookDAO.update(new Book(id, title.getText().trim(), author.getText().trim(), category.getText().trim(), status.getSelectedItem().toString()));
            auditDAO.log(currentUser, "UPDATE_BOOK", "Book ID: " + id);
            JOptionPane.showMessageDialog(this, "Book updated successfully");
            refreshCurrentModule();
        }
    }

    private void validateBook(JTextField title, JTextField author, JTextField category) {
        Validator.notBlank(title.getText(), "Title");
        Validator.notBlank(author.getText(), "Author");
        Validator.notBlank(category.getText(), "Category");
    }

    private void deleteBook() throws SQLException {
        int row = selectedRow();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (confirm("Delete selected book?")) {
            bookDAO.delete(id);
            auditDAO.log(currentUser, "DELETE_BOOK", "Book ID: " + id);
            refreshCurrentModule();
        }
    }

    private void addMember() throws SQLException {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{"Student", "Staff"});
        Object[] form = {"Name:", name, "Email:", email, "Type:", type};
        if (JOptionPane.showConfirmDialog(this, form, "Add Member", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            validateMember(name, email);
            memberDAO.add(new Member(0, name.getText().trim(), email.getText().trim(), type.getSelectedItem().toString()));
            auditDAO.log(currentUser, "ADD_MEMBER", "Name: " + name.getText().trim());
            JOptionPane.showMessageDialog(this, "Member added successfully");
            refreshCurrentModule();
        }
    }

    private void updateMember() throws SQLException {
        int row = selectedRow();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        JTextField name = new JTextField(tableModel.getValueAt(row, 1).toString());
        JTextField email = new JTextField(tableModel.getValueAt(row, 2).toString());
        JComboBox<String> type = new JComboBox<>(new String[]{"Student", "Staff"});
        type.setSelectedItem(tableModel.getValueAt(row, 3).toString());
        Object[] form = {"Name:", name, "Email:", email, "Type:", type};
        if (JOptionPane.showConfirmDialog(this, form, "Update Member", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            validateMember(name, email);
            memberDAO.update(new Member(id, name.getText().trim(), email.getText().trim(), type.getSelectedItem().toString()));
            auditDAO.log(currentUser, "UPDATE_MEMBER", "Member ID: " + id);
            JOptionPane.showMessageDialog(this, "Member updated successfully");
            refreshCurrentModule();
        }
    }

    private void validateMember(JTextField name, JTextField email) {
        Validator.notBlank(name.getText(), "Name");
        Validator.email(email.getText());
    }

    private void deleteMember() throws SQLException {
        int row = selectedRow();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (confirm("Delete selected member?")) {
            memberDAO.delete(id);
            auditDAO.log(currentUser, "DELETE_MEMBER", "Member ID: " + id);
            refreshCurrentModule();
        }
    }

    private void addBorrowRecord() throws SQLException {
        BorrowRecord record = buildBorrowRecordForm(0);
        if (record != null) {
            borrowRecordDAO.add(record);
            if ("Borrowed".equals(record.getReturnStatus())) {
                bookDAO.setAvailability(record.getBookId(), "Borrowed");
            }
            auditDAO.log(currentUser, "ADD_BORROW_RECORD", "Book ID: " + record.getBookId() + ", Member ID: " + record.getMemberId());
            JOptionPane.showMessageDialog(this, "Borrow record added successfully");
            refreshCurrentModule();
        }
    }

    private void updateBorrowRecord() throws SQLException {
        int row = selectedRow();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        BorrowRecord record = buildBorrowRecordForm(id);
        if (record != null) {
            borrowRecordDAO.update(record);
            if ("Returned".equals(record.getReturnStatus())) {
                bookDAO.setAvailability(record.getBookId(), "Available");
            } else if ("Borrowed".equals(record.getReturnStatus()) || "Overdue".equals(record.getReturnStatus())) {
                bookDAO.setAvailability(record.getBookId(), "Borrowed");
            }
            auditDAO.log(currentUser, "UPDATE_BORROW_RECORD", "Record ID: " + id);
            JOptionPane.showMessageDialog(this, "Borrow record updated successfully");
            refreshCurrentModule();
        }
    }

    private BorrowRecord buildBorrowRecordForm(int id) {
        JTextField bookId = new JTextField(id == 0 ? "" : tableModel.getValueAt(selectedRow(), 1).toString());
        JTextField memberId = new JTextField(id == 0 ? "" : tableModel.getValueAt(selectedRow(), 2).toString());
        JTextField borrowDate = new JTextField(id == 0 ? LocalDate.now().toString() : tableModel.getValueAt(selectedRow(), 3).toString());
        JTextField dueDate = new JTextField(id == 0 ? LocalDate.now().plusWeeks(2).toString() : tableModel.getValueAt(selectedRow(), 4).toString());
        JComboBox<String> status = new JComboBox<>(new String[]{"Borrowed", "Returned", "Overdue"});
        if (id != 0) {
            status.setSelectedItem(tableModel.getValueAt(selectedRow(), 5).toString());
        }
        Object[] form = {"Book ID:", bookId, "Member ID:", memberId, "Borrow Date:", borrowDate, "Due Date:", dueDate, "Status:", status};
        if (JOptionPane.showConfirmDialog(this, form, id == 0 ? "Add Borrow Record" : "Update Borrow Record", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return null;
        }
        int parsedBookId = Validator.positiveInt(bookId.getText(), "Book ID");
        int parsedMemberId = Validator.positiveInt(memberId.getText(), "Member ID");
        LocalDate parsedBorrowDate = Validator.date(borrowDate.getText(), "Borrow Date");
        LocalDate parsedDueDate = Validator.date(dueDate.getText(), "Due Date");
        Validator.dueAfterBorrow(parsedBorrowDate, parsedDueDate);
        return new BorrowRecord(id, parsedBookId, parsedMemberId, parsedBorrowDate, parsedDueDate, status.getSelectedItem().toString());
    }

    private void deleteBorrowRecord() throws SQLException {
        int row = selectedRow();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (confirm("Delete selected borrowing record?")) {
            borrowRecordDAO.delete(id);
            auditDAO.log(currentUser, "DELETE_BORROW_RECORD", "Record ID: " + id);
            refreshCurrentModule();
        }
    }

    private int selectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            throw new IllegalArgumentException("Select a row first.");
        }
        return table.convertRowIndexToModel(viewRow);
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void logout() {
        auditDAO.log(currentUser, "LOGOUT", "User logged out");
        dispose();
        new com.stmarys.library.app.LoginFrame().setVisible(true);
    }

    private void showError(Exception e) {
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        JOptionPane.showMessageDialog(this, "Error: " + message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
