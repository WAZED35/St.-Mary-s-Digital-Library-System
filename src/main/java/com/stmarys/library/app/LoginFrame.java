package com.stmarys.library.app;

import com.stmarys.library.dao.AuditDAO;
import com.stmarys.library.dao.StaffUserDAO;
import com.stmarys.library.dao.StudentUserDAO;
import com.stmarys.library.ui.LibraryGUI;
import com.stmarys.library.util.LoggerUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;

/** Login window for staff and student users. */
public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<String> loginTypeBox = new JComboBox<>(new String[]{"Staff", "Student"});
    private final JButton loginButton = new JButton("Login");

    private boolean passwordVisible = false;

    private final Color darkBlue = new Color(0, 58, 92);
    private final Color mainBlue = new Color(0, 126, 168);
    private final Color orange = new Color(245, 145, 0);

    /** Builds the login frame. */
    public LoginFrame() {
        setTitle("St Mary's Digital Library Login");
        setSize(950, 580);
        setMinimumSize(new Dimension(850, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        mainPanel.add(createBrandPanel(), BorderLayout.WEST);
        mainPanel.add(createLoginSide(), BorderLayout.CENTER);
    }

    private JPanel createBrandPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(380, 580));
        leftPanel.setBackground(Color.WHITE);

        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon logo = new ImageIcon("src/main/resources/images/stmarys_logo.png");
        Image scaledLogo = logo.getImage().getScaledInstance(230, 150, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaledLogo));

        JLabel systemName = new JLabel("<html><center>St Mary's<br>Digital Library System</center></html>", SwingConstants.CENTER);
        systemName.setFont(new Font("Segoe UI", Font.BOLD, 25));
        systemName.setForeground(darkBlue);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 35, 10);
        leftPanel.add(logoLabel, gbc);

        gbc.gridy = 1;
        leftPanel.add(systemName, gbc);
        return leftPanel;
    }

    private JPanel createLoginSide() {
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, mainBlue, getWidth(), getHeight(), darkBlue);
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        RoundedPanel loginCard = new RoundedPanel(25);
        loginCard.setPreferredSize(new Dimension(420, 455));
        loginCard.setBackground(Color.WHITE);
        loginCard.setLayout(new GridBagLayout());
        loginCard.setBorder(BorderFactory.createEmptyBorder(28, 35, 28, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Library Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(darkBlue);
        gbc.gridy = 0;
        loginCard.add(title, gbc);

        addLabel(loginCard, gbc, "Login Type", 1);
        loginTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loginTypeBox.setPreferredSize(new Dimension(300, 40));
        gbc.gridy = 2;
        loginCard.add(loginTypeBox, gbc);

        addLabel(loginCard, gbc, "Username", 3);
        styleTextField(usernameField);
        gbc.gridy = 4;
        loginCard.add(usernameField, gbc);

        addLabel(loginCard, gbc, "Password", 5);
        gbc.gridy = 6;
        loginCard.add(createPasswordPanel(), gbc);

        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(orange);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder());
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> loginInBackground());

        gbc.gridy = 7;
        loginCard.add(loginButton, gbc);

        rightPanel.add(loginCard);
        return rightPanel;
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, String text, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(darkBlue);
        gbc.gridy = row;
        panel.add(label, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(300, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
    }

    private JPanel createPasswordPanel() {
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setPreferredSize(new Dimension(300, 45));
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordField.setEchoChar('\u2022');
        passwordField.setBackground(Color.WHITE);
        passwordField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JButton showBtn = new JButton("Show");
        showBtn.setPreferredSize(new Dimension(65, 45));
        showBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        showBtn.setForeground(new Color(0, 120, 255));
        showBtn.setBorder(null);
        showBtn.setFocusPainted(false);
        showBtn.setContentAreaFilled(false);
        showBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showBtn.addActionListener(e -> togglePassword(showBtn));

        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(showBtn, BorderLayout.EAST);
        return passwordPanel;
    }

    private void togglePassword(JButton showBtn) {
        passwordVisible = !passwordVisible;
        passwordField.setEchoChar(passwordVisible ? (char) 0 : '\u2022');
        showBtn.setText(passwordVisible ? "Hide" : "Show");
    }

    private void loginInBackground() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String loginType = loginTypeBox.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Checking...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                if ("Staff".equals(loginType)) {
                    return new StaffUserDAO().validateLogin(username, password);
                }
                return new StudentUserDAO().validateLogin(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean valid = get();
                    if (valid) {
                        LoggerUtil.getLogger().info("Successful " + loginType + " login: " + username);
                        new AuditDAO().log(username, "LOGIN", "role=" + loginType);
                        loginButton.setText("Loading...");
                        dispose();
                        new LibraryGUI(username, loginType).setVisible(true);
                    } else {
                        LoggerUtil.getLogger().warning("Failed " + loginType + " login attempt: " + username);
                        new AuditDAO().log(username, "FAILED_LOGIN", "role=" + loginType);
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Invalid " + loginType.toLowerCase() + " username or password",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                } catch (Exception e) {
                    LoggerUtil.getLogger().severe("Login error: " + e.getMessage());
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        worker.execute();
    }

    /** Rounded white login card. */
    static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
