package de.linzn.simplyLogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.*;

public class LogSystem {

    java.util.logging.Logger sysLogger;
    File logDirectory;
    private final Logger logger;
    private FileHandler fileHandler;
    private ConsoleHandler consoleHandler;

    public LogSystem(String appName) {
        this(10000, appName);
    }

    public LogSystem(int maxCacheLog, String appName) {
        this.logDirectory = null;
        this.logger = new Logger(this, maxCacheLog);
        this.setupSysLogger(appName);
    }

    public Logger getLogger() {
        return logger;
    }

    public Level getLogLevel() {
        return sysLogger.getLevel();
    }

    public void setLogLevel(Level level) {
        this.sysLogger.setLevel(level);
        this.consoleHandler.setLevel(level);
    }

    public void setFileLogger(File logDirectory) {
        this.logDirectory = logDirectory;
        this.setupSysFileLogger();
    }

    void logToSysLogger(Level level, String msg) {
        if (this.sysLogger != null) {
            this.sysLogger.log(level, msg);
        }
    }

    private void setupSysLogger(String appName) {
        sysLogger = java.util.logging.Logger.getLogger(appName);
        sysLogger.setUseParentHandlers(false);
        consoleHandler = new CustomConsoleHandler(this);
        consoleHandler.setFormatter(formatter);
        sysLogger.addHandler(consoleHandler);
    }

    private void setupSysFileLogger() {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH.mm.ss");
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            fileHandler = new FileHandler(logDirectory.getName() + "/" + dateFormat.format(new Date().getTime()) + ".log");
            fileHandler.setFormatter(formatter);
            sysLogger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            this.logger.ERROR(e);
        }
    }

    public SimpleFormatter formatter = new SimpleFormatter() {
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
    };
}
