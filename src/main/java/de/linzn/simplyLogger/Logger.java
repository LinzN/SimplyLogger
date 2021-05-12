package de.linzn.simplyLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Logger {
    private final LogSystem logSystem;
    private final int maxCacheLog;
    private final LinkedList<LogRecord> logEntries = new LinkedList<>();


    Logger(LogSystem logSystem, int maxCacheLog) {
        this.logSystem = logSystem;
        this.maxCacheLog = maxCacheLog;
    }

    private static String getStackTrace(Exception ex) {
        StringBuffer sb = new StringBuffer(500);
        StackTraceElement[] st = ex.getStackTrace();
        sb.append(ex.getClass().getName() + ": " + ex.getMessage() + "\n");
        for (int i = 0; i < st.length; i++) {
            sb.append("\t at " + st[i].toString() + "\n");
        }
        return sb.toString();
    }

    /**
     * CONFIG Logging for config settings.
     *
     * @param msg Log entry
     */
    public synchronized void CONFIG(Object msg) {
        this.log(formattingLogInput(msg), Level.CONFIG);
    }

    /**
     * Live Logging for commands. Log in live level
     *
     * @param msg Log entry
     */
    public synchronized void LIVE(Object msg) {
        this.log(formattingLogInput(msg), Level.INFO);
    }

    /**
     * Debug Logging. Log in debug level
     *
     * @param msg Log entry
     */
    public synchronized void DEBUG(Object msg) {
        this.log(formattingLogInput(msg), CustomLevel.DEBUG);
    }

    /**
     * Info Logging. Log in info level
     *
     * @param msg Log entry
     */
    public synchronized void INFO(Object msg) {
        this.log(formattingLogInput(msg), Level.INFO);
    }

    /**
     * Warning Logging. Log in warning level
     *
     * @param msg Log entry
     */
    public synchronized void WARNING(Object msg) {
        this.log(formattingLogInput(msg), Level.WARNING);
    }

    /**
     * Error Logging. Log in error level
     *
     * @param msg Log entry
     */
    public synchronized void ERROR(Object msg) {
        this.log(formattingLogInput(msg), Level.SEVERE);
    }

    /**
     * Core Logging. Log in core level
     *
     * @param msg Log entry
     */
    public synchronized void CORE(Object msg) {
        this.log(formattingLogInput(msg), CustomLevel.CORE);
    }

    private String formattingLogInput(Object input) {
        String output;

        if (input instanceof Exception) {
            Exception e = (Exception) input;
            output = "\n###########[ERROR IN STEM SYSTEM START]##############\n" + "Stacktrace:::" + getStackTrace(e) + "###########[ERROR IN STEM SYSTEM END]##############";
        } else {
            output = input.toString();
        }

        return output;
    }

    private void log(String msg, Level level) {
        this.logSystem.logToSysLogger(level, msg);
    }

    void addToLogList(LogRecord logRecord) {
        if (logEntries.size() >= maxCacheLog) {
            logEntries.removeFirst();
        }
        logEntries.addLast(logRecord);
    }

    /**
     * Get a String list with the last x log entries
     *
     * @param max Amount of logentries to collect
     * @return String list with log entries
     */
    public List<String> getLastEntries(int max) {
        List<LogRecord> clonedRecords = splitLogList(max);

        List<String> stringList = new LinkedList<>();

        for (LogRecord logRecord : clonedRecords) {
            stringList.add(this.logSystem.defaultFormatter.format(logRecord));
        }
        return stringList;
    }

    /**
     * Split the current log entry list to a smaller one
     *
     * @param max max size of requested logentries
     * @return LinkedList of requested logentries
     */
    private List<LogRecord> splitLogList(int max) {
        List<LogRecord> clonedRecords;
        if (logEntries.size() <= max) {
            clonedRecords = new LinkedList<>(logEntries);
        } else {
            clonedRecords = logEntries.subList(logEntries.size() - max, logEntries.size());
        }
        return clonedRecords;
    }

    /**
     * Own custom log level for debugging
     */
    static class CustomLevel extends Level {
        public static final Level CORE = new CustomLevel("CORE", 1100);
        public static final Level DEBUG = new CustomLevel("DEBUG", 350);

        public CustomLevel(String name, int value) {
            super(name, value);
        }
    }
}
