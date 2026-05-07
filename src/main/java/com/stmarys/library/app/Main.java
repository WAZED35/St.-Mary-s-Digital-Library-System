package com.stmarys.library.app;

import com.stmarys.library.db.DatabaseManager;
import com.stmarys.library.ui.ConsoleUI;

import javax.swing.SwingUtilities;

/** Application entry point for console and graphical modes. */
public class Main {
    /** Starts the application; pass 'console' as an argument to use console mode. */
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            new ConsoleUI().start();
        } else {
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
