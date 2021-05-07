package de.linzn.simplyLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class Logger {
    private LogSystem logSystem;
    private int maxCacheLog;

    LinkedList<String> logEntries = new LinkedList<>();


    Logger(LogSystem logSystem, int maxCacheLog) {
        this.logSystem = logSystem;
        this.maxCacheLog = maxCacheLog;
    }

    /**
     * CONFIG Logging for config settings.
     *
     * @param msg Log entry
     */
    public synchronized void CONFIG(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(formatted, Level.CONFIG, formatted);
    }

    /**
     * Live Logging for commands. Only shown on console
     *
     * @param msg Log entry
     */
    public synchronized void LIVE(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.WHITE + formatted + Color.RESET, Level.INFO, formatted);
    }

    /**
     * Debug Logging. Show debug log entries
     *
     * @param msg Log entry
     */
    public synchronized void DEBUG(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.PURPLE + formatted + Color.RESET, CustomLevel.DEBUG, formatted);
    }

    /**
     * Info Logging. Show all default log entries
     *
     * @param msg Log entry
     */
    public synchronized void INFO(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.WHITE + formatted + Color.RESET, Level.INFO, formatted);
    }

    /**
     * Warning Logging. Show only errors and warning entries
     *
     * @param msg Log entry
     */
    public synchronized void WARNING(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.YELLOW + formatted + Color.RESET, Level.WARNING, formatted);
    }

    /**
     * Warning Logging. Show only error log entries
     *
     * @param msg Log entry
     */
    public synchronized void ERROR(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.RED + formatted + Color.RESET, Level.SEVERE, formatted);
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

    private static String getStackTrace(Exception ex) {
        StringBuffer sb = new StringBuffer(500);
        StackTraceElement[] st = ex.getStackTrace();
        sb.append(ex.getClass().getName() + ": " + ex.getMessage() + "\n");
        for (int i = 0; i < st.length; i++) {
            sb.append("\t at " + st[i].toString() + "\n");
        }
        return sb.toString();
    }

    private void log(String colored, Level level, String raw) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String rawPrefix = "[" + dateFormat.format(new Date().getTime()) + "] [" + Thread.currentThread().getName() + "] ";
        String coloredPrefix = "[" + Thread.currentThread().getName() + "] ";
        this.logSystem.logToSysLogger(level, coloredPrefix + colored);
        this.addToLogList(level, rawPrefix + raw);
    }

    private void addToLogList(Level level, String data) {
        if (this.logSystem.sysLogger.isLoggable(level)) {
            if (logEntries.size() >= maxCacheLog) {
                logEntries.removeFirst();
            }
            logEntries.addLast(replaceChars(data));
        }
    }

    private String replaceChars(String data) {
        return data.replace("\u001B[37m", "")
                .replace("\u001B[0m", "")
                .replace("\u001B[32m", "")
                .replace("\u001B[35m", "");
    }

    public List<String> getLastEntries(int max) {
        if (logEntries.size() <= max) {
            return logEntries;
        } else {
            return logEntries.subList(logEntries.size() - max, logEntries.size());
        }
    }

    private static class CustomLevel extends Level {
        public static final Level DEBUG = new CustomLevel("DEBUG", 350);

        public CustomLevel(String name, int value) {
            super(name, value);
        }
    }

}
