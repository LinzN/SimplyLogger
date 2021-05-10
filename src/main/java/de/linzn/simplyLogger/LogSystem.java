package de.linzn.simplyLogger;


import de.linzn.simplyLogger.input.InputHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.*;

public class LogSystem {

    final DefaultFormatter defaultFormatter;
    final ColorFormatter colorFormatter;
    final HtmlFormatter htmlFormatter;
    private final Logger logger;
    java.util.logging.Logger sysLogger;
    File logDirectory;
    private InputManager inputManager;
    private FileHandler fileHandler;
    private ConsoleHandler consoleHandler;

    /**
     * Constructor for creating the Log system
     * Create a Logger for system logging
     *
     * @param appName Logger name
     */
    public LogSystem(String appName) {
        this(10000, appName);
    }

    /**
     * Constructor for creating the Log system
     * Create a Logger for system logging
     *
     * @param appName     Logger name
     * @param maxCacheLog max size of cached log entries
     */
    public LogSystem(int maxCacheLog, String appName) {
        this.defaultFormatter = new DefaultFormatter();
        this.colorFormatter = new ColorFormatter();
        this.htmlFormatter = new HtmlFormatter();
        this.logDirectory = null;
        this.logger = new Logger(this, maxCacheLog);
        this.setupSysLogger(appName);
    }

    /**
     * Get the current logger instance
     *
     * @return Current logger instance
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the current log level
     *
     * @return Current log level
     */
    public Level getLogLevel() {
        return sysLogger.getLevel();
    }

    /**
     * Set the log level for logging
     *
     * @param level Log level to set
     */
    public void setLogLevel(Level level) {
        this.sysLogger.setLevel(level);
        this.consoleHandler.setLevel(level);
    }

    /**
     * Enable file logging
     *
     * @param logDirectory directory to create logfiles
     */
    public void setFileLogger(File logDirectory) {
        this.logDirectory = logDirectory;
        this.setupSysFileLogger();
    }

    /**
     * Register a console input handler for console input
     *
     * @param inputHandler InputHandler implementation for console input
     */
    public void registerInputHandler(InputHandler inputHandler) {
        if (this.inputManager == null) {
            this.logger.INFO("Register a new input manager!");
            this.inputManager = new InputManager(inputHandler, this);
        } else {
            this.logger.ERROR("There is already an input manager registered!");
        }
    }

    /**
     * Unregister and disable console inputs
     */
    public void unregisterInputHandler() {
        if (this.inputManager != null) {
            this.logger.INFO("Unregister input manager!");
            this.inputManager.stopThread();
            this.inputManager = null;
        } else {
            this.logger.ERROR("There is no input manager to unregister!");
        }
    }

    /**
     * Log something to sys logger
     *
     * @param level Level of logging
     * @param msg   Content of logging
     */
    void logToSysLogger(Level level, String msg) {
        if (this.sysLogger != null) {
            this.sysLogger.log(level, msg);
        }
    }

    private void setupSysLogger(String appName) {
        sysLogger = java.util.logging.Logger.getLogger(appName);
        sysLogger.setUseParentHandlers(false);
        consoleHandler = new CustomConsoleHandler(this);
        consoleHandler.setFormatter(colorFormatter);
        sysLogger.addHandler(consoleHandler);
    }

    private void setupSysFileLogger() {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH.mm.ss");
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            fileHandler = new FileHandler(logDirectory.getName() + "/" + dateFormat.format(new Date().getTime()) + ".log");
            fileHandler.setFormatter(defaultFormatter);
            sysLogger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            this.logger.ERROR(e);
        }
    }


    static class DefaultFormatter extends SimpleFormatter {
        private static final String format = "[%1$tF %1$tT] [%2$-7s] [%3$s] %4$s %n";

        @Override
        public synchronized String format(LogRecord lr) {
            return String.format(format,
                    new Date(lr.getMillis()),
                    lr.getLevel().getLocalizedName(),
                    getThreadName(lr.getThreadID()),
                    lr.getMessage()
            );
        }

        String getThreadName(long threadId) {
            Optional<Thread> thread = Thread.getAllStackTraces().keySet().stream()
                    .filter(t -> t.getId() == threadId)
                    .findFirst();
            if (thread.isPresent()) {
                return thread.get().getName();
            } else {
                return "UNKNOWN";
            }
        }
    }

    static class ColorFormatter extends DefaultFormatter {

        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";


        @Override
        public synchronized String format(LogRecord lr) {
            String uncolored = super.format(lr);
            String color;

            if (lr.getLevel() == Level.SEVERE) {
                color = ANSI_RED;
            } else if (lr.getLevel() == Level.WARNING) {
                color = ANSI_YELLOW;
            } else if (lr.getLevel() == Level.INFO) {
                color = ANSI_WHITE;
            } else if (lr.getLevel() == Level.CONFIG) {
                color = ANSI_CYAN;
            } else if (lr.getLevel() == Logger.CustomLevel.DEBUG) {
                color = ANSI_PURPLE;
            } else {
                color = ANSI_GREEN;
            }
            return color + uncolored + ANSI_RESET;
        }
    }

    static class HtmlFormatter extends DefaultFormatter {

        @Override
        public synchronized String format(LogRecord lr) {
            String uncolored = super.format(lr);
            String htmlColor;

            if (lr.getLevel() == Level.SEVERE) {
                htmlColor = "style=\"color:red\">";
            } else if (lr.getLevel() == Level.WARNING) {
                htmlColor = "style=\"color:yellow\">";
            } else if (lr.getLevel() == Level.INFO) {
                htmlColor = ">";
            } else if (lr.getLevel() == Level.CONFIG) {
                htmlColor = "style=\"color:cyan\">";
            } else if (lr.getLevel() == Logger.CustomLevel.DEBUG) {
                htmlColor = "style=\"color:purple\">";
            } else {
                htmlColor = "style=\"color:green\">";
            }
            return "<td " + htmlColor + uncolored + "</td>\n";
        }
    }

}
