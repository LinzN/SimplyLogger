/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.simplyLogger;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Logger {
    private final LogSystem logSystem;
    private final int maxCacheLog;
    private final LinkedList<LogRecord> logEntries = new LinkedList<>();
    private final LinkedHashMap<Long, Exception> exceptionList = new LinkedHashMap<>();


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
        this.log(processLogInput(msg), Level.CONFIG);
    }

    /**
     * Live Logging for commands. Log in live level
     *
     * @param msg Log entry
     */
    public synchronized void LIVE(Object msg) {
        this.log(processLogInput(msg), Level.INFO);
    }

    /**
     * Debug Logging. Log in debug level
     *
     * @param msg Log entry
     */
    public synchronized void DEBUG(Object msg) {
        this.log(processLogInput(msg), CustomLevel.DEBUG);
    }

    /**
     * Info Logging. Log in info level
     *
     * @param msg Log entry
     */
    public synchronized void INFO(Object msg) {
        this.log(processLogInput(msg), Level.INFO);
    }

    /**
     * Warning Logging. Log in warning level
     *
     * @param msg Log entry
     */
    public synchronized void WARNING(Object msg) {
        this.log(processLogInput(msg), Level.WARNING);
    }

    /**
     * Error Logging. Log in error level
     *
     * @param msg Log entry
     */
    public synchronized void ERROR(Object msg) {
        this.log(processLogInput(msg), Level.SEVERE);
    }

    /**
     * Core Logging. Log in core level
     *
     * @param msg Log entry
     */
    public synchronized void CORE(Object msg) {
        this.log(processLogInput(msg), CustomLevel.CORE);
    }

    /**
     * Super Logging. Log in super level
     *
     * @param msg Log entry
     */
    public synchronized void SUPER(Object msg) {
        this.log(processLogInput(msg), CustomLevel.SUPER);
    }

    /**
     * custom Logging for given log level
     *
     * @param msg   Log entry
     * @param level Log level to log
     */
    public synchronized void LOG(Object msg, Level level) {
        this.log(processLogInput(msg), level);
    }


    /**
     * Get a String list with the last x log entries
     *
     * @param max Amount of logentries to collect
     * @return String list with log entries
     */
    public List<LogRecord> getLastEntries(int max) {
        return splitLogList(max);
    }


    /**
     * Get all exceptions since startup
     *
     * @return LinkedHashMap with all exceptions
     */
    public LinkedHashMap<Long, Exception> getExceptionList() {
        return this.exceptionList;
    }


    private Object processLogInput(Object input) {
        Object output;

        if (input instanceof Exception) {
            Exception e = (Exception) input;
            this.exceptionList.put(new Date().getTime(), e);
            List<String> exceptionList = new ArrayList<>();
            exceptionList.add("");
            exceptionList.add("###########[ERROR IN STEM SYSTEM START]##############");
            exceptionList.addAll(getStackTrace(e));
            exceptionList.add("###########[ERROR IN STEM SYSTEM END]##############");
            output = exceptionList;
        } else {
            output = input;
        }

        return output;
    }

    private static List<String> getStackTrace(Exception ex) {
        List<String> exceptionList = new ArrayList<>();
        StackTraceElement[] st = ex.getStackTrace();
        exceptionList.add("Stacktrace:::" + ex.getClass().getName() + ": " + ex.getMessage() + "");
        for (int i = 0; i < st.length; i++) {
            exceptionList.add("\t at " + st[i].toString() + "");
        }
        return exceptionList;
    }

    private void log(Object msg, Level level) {
        if (msg instanceof List) {
            List<String> list = (List<String>) msg;
            for (String entry : list) {
                this.logSystem.logToSysLogger(level, entry);
            }
        } else {
            this.logSystem.logToSysLogger(level, msg.toString());
        }
    }

    void addToLogList(LogRecord logRecord) {
        if (logEntries.size() >= maxCacheLog) {
            logEntries.removeFirst();
        }
        logEntries.addLast(logRecord);
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
        public static final Level SUPER = new CustomLevel("SUPER", 1110);
        public static final Level DEBUG = new CustomLevel("DEBUG", 350);

        public CustomLevel(String name, int value) {
            super(name, value);
        }
    }
}
