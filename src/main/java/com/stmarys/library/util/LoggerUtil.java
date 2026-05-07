package com.stmarys.library.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Provides application-wide file logging in the audit folder. */
public final class LoggerUtil {
    private static final Logger LOGGER = Logger.getLogger("StMarysLibrary");

    static {
        try {
            new File("audit").mkdirs();
            FileHandler fileHandler = new FileHandler("audit/library.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LoggerUtil() {
    }

    /** Returns the shared application logger. */
    public static Logger getLogger() {
        return LOGGER;
    }
}
