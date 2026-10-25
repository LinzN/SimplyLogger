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

/**
 * Logger facade of the log system.
 * <p>
 * All caches are bounded: the log entry cache keeps the last {@code maxCacheLog} records and the
 * exception history keeps the last {@link #MAX_EXCEPTIONS} exceptions. Messages below the active log
 * level are dropped before any formatting work is done.
 */
public class Logger {
    /* Maximum number of exceptions kept in the exception history */
    public static final int MAX_EXCEPTIONS = 100;
    /* Maximum depth of "Caused by" chains rendered for one exception */
    private static final int MAX_CAUSE_DEPTH = 10;

    private final LogSystem logSystem;
    private final int maxCacheLog;
    /* Guarded by logEntries */
    private final ArrayDeque<LogRecord> logEntries = new ArrayDeque<>();
    /* Guarded by exceptionList */
    private final LinkedHashMap<Long, Exception> exceptionList = new LinkedHashMap<>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Exception> eldest) {
            return size() > MAX_EXCEPTIONS;
        }
    };
    /* Guarded by exceptionList; makes exception keys unique even within the same millisecond */
    private long lastExceptionKey = 0;
    /* Keeps the lines of one log call (e.g. a stack trace) together */
    private final Object outputLock = new Object();

    Logger(LogSystem logSystem, int maxCacheLog) {
        this.logSystem = logSystem;
        this.maxCacheLog = Math.max(1, maxCacheLog);
    }

    private static List<String> getStackTrace(Throwable ex) {
        List<String> exceptionList = new ArrayList<>();
        exceptionList.add("Stacktrace:::" + ex.getClass().getName() + ": " + ex.getMessage());
        appendFrames(exceptionList, ex);
        Throwable cause = ex.getCause();
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        seen.add(ex);
        int depth = 0;
        while (cause != null && depth < MAX_CAUSE_DEPTH && seen.add(cause)) {
            exceptionList.add("Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
            appendFrames(exceptionList, cause);
            cause = cause.getCause();
            depth++;
        }
        return exceptionList;
    }

    private static void appendFrames(List<String> lines, Throwable throwable) {
        for (StackTraceElement element : throwable.getStackTrace()) {
            lines.add("\t at " + element);
        }
    }

    /**
     * CONFIG Logging for config settings.
     *
     * @param msg Log entry
     */
    public void CONFIG(Object msg) {
        this.log(msg, Level.CONFIG);
    }

    /**
     * Live Logging for commands. Log in live level
     *
     * @param msg Log entry
     */
    public void LIVE(Object msg) {
        this.log(msg, Level.INFO);
    }

    /**
     * Debug Logging. Log in debug level
     *
     * @param msg Log entry
     */
    public void DEBUG(Object msg) {
        this.log(msg, CustomLevel.DEBUG);
    }

    /**
     * Info Logging. Log in info level
     *
     * @param msg Log entry
     */
    public void INFO(Object msg) {
        this.log(msg, Level.INFO);
    }

    /**
     * Warning Logging. Log in warning level
     *
     * @param msg Log entry
     */
    public void WARNING(Object msg) {
        this.log(msg, Level.WARNING);
    }

    /**
     * Error Logging. Log in error level
     *
     * @param msg Log entry
     */
    public void ERROR(Object msg) {
        this.log(msg, Level.SEVERE);
    }

    /**
     * Core Logging. Log in core level
     *
     * @param msg Log entry
     */
    public void CORE(Object msg) {
        this.log(msg, CustomLevel.CORE);
    }

    /**
     * Super Logging. Log in super level
     *
     * @param msg Log entry
     */
    public void SUPER(Object msg) {
        this.log(msg, CustomLevel.SUPER);
    }

    /**
     * custom Logging for given log level
     *
     * @param msg   Log entry
     * @param level Log level to log
     */
    public void LOG(Object msg, Level level) {
        this.log(msg, level == null ? Level.INFO : level);
    }

    /**
     * Get a list with the last x log entries
     *
     * @param max Amount of logentries to collect
     * @return copy of the requested log entries, oldest first
     */
    public List<LogRecord> getLastEntries(int max) {
        synchronized (this.logEntries) {
            List<LogRecord> all = new ArrayList<>(this.logEntries);
            if (max >= all.size()) {
                return all;
            }
            if (max <= 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(all.subList(all.size() - max, all.size()));
        }
    }

    /**
     * Get the most recent exceptions (at most {@link #MAX_EXCEPTIONS})
     *
     * @return copy of the exception history, keyed by timestamp in milliseconds
     */
    public LinkedHashMap<Long, Exception> getExceptionList() {
        synchronized (this.exceptionList) {
            return new LinkedHashMap<>(this.exceptionList);
        }
    }

    private void rememberException(Exception e) {
        synchronized (this.exceptionList) {
            long key = Math.max(System.currentTimeMillis(), this.lastExceptionKey + 1);
            this.lastExceptionKey = key;
            this.exceptionList.put(key, e);
        }
    }

    private void log(Object msg, Level level) {
        if (msg instanceof Exception exception) {
            this.rememberException(exception);
        }
        if (!this.logSystem.isLoggable(level)) {
            return;
        }
        if (msg instanceof Throwable throwable) {
            List<String> lines = new ArrayList<>();
            lines.add("");
            lines.add("###########[ERROR IN STEM SYSTEM START]##############");
            lines.addAll(getStackTrace(throwable));
            lines.add("###########[ERROR IN STEM SYSTEM END]##############");
            synchronized (this.outputLock) {
                for (String entry : lines) {
                    this.logSystem.logToSysLogger(level, entry);
                }
            }
        } else if (msg instanceof List<?> list) {
            synchronized (this.outputLock) {
                for (Object entry : list) {
                    this.logSystem.logToSysLogger(level, String.valueOf(entry));
                }
            }
        } else {
            this.logSystem.logToSysLogger(level, String.valueOf(msg));
        }
    }

    void addToLogList(LogRecord logRecord) {
        synchronized (this.logEntries) {
            if (this.logEntries.size() >= this.maxCacheLog) {
                this.logEntries.removeFirst();
            }
            this.logEntries.addLast(logRecord);
        }
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
