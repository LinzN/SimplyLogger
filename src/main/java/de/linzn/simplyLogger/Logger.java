package de.linzn.simplyLogger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Logger {
    private LogSystem logSystem;
    private int maxCacheLog;

    LinkedList<String> logEntries = new LinkedList<>();


    Logger(LogSystem logSystem, int maxCacheLog) {
        this.logSystem = logSystem;
        this.maxCacheLog = maxCacheLog;
    }

    /**
     * Live Logging for commands. Only shown on console
     *
     * @param msg Log entry
     */
    public synchronized void LIVE(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.WHITE + formatted + Color.RESET, LOGLEVEL.LIVE, formatted);
    }

    /**
     * Debug Logging. Show debug log entries
     *
     * @param msg Log entry
     */
    public synchronized void DEBUG(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.PURPLE + formatted + Color.RESET, LOGLEVEL.DEBUG, formatted);
    }

    /**
     * Info Logging. Show all default log entries
     *
     * @param msg Log entry
     */
    public synchronized void INFO(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.WHITE + formatted + Color.RESET, LOGLEVEL.INFO, formatted);
    }

    /**
     * Warning Logging. Show only errors and warning entries
     *
     * @param msg Log entry
     */
    public synchronized void WARNING(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.YELLOW + formatted + Color.RESET, LOGLEVEL.WARNING, formatted);
    }

    /**
     * Warning Logging. Show only error log entries
     *
     * @param msg Log entry
     */
    public synchronized void ERROR(Object msg) {
        String formatted = formattingLogInput(msg);
        this.log(Color.RED + formatted + Color.RESET, LOGLEVEL.ERROR, formatted);
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

    private void log(String msg, LOGLEVEL loglevel, String raw) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String logPrefix = "[" + dateFormat.format(new Date().getTime()) + "] [" + Thread.currentThread().getName() + "] ";
        if (shouldLogged(loglevel)) {
            System.out.println(logPrefix + msg);
            if (loglevel != LOGLEVEL.LIVE) {
                String logPrefixRaw = "[" + Thread.currentThread().getName() + "] ";
                this.logSystem.writeToFile(logPrefixRaw + raw);
                this.addToLogList(logPrefix + raw);
            }
        }
    }

    private void addToLogList(String data) {
        if (logEntries.size() >= maxCacheLog) {
            logEntries.removeFirst();
        }
        logEntries.addLast(replaceChars(data));
    }

    private String replaceChars(String data) {
        return data.replace("\u001B[37m", "")
                .replace("\u001B[0m", "")
                .replace("\u001B[32m", "")
                .replace("\u001B[35m", "");
    }

    private boolean shouldLogged(LOGLEVEL loglevel) {
        if (this.logSystem.logLevel == LOGLEVEL.ALL) {
            return true;
        } else if (this.logSystem.logLevel == LOGLEVEL.DEBUG) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return true;
            } else if (loglevel == LOGLEVEL.INFO) {
                return true;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return true;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        } else if (this.logSystem.logLevel == LOGLEVEL.INFO) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return false;
            } else if (loglevel == LOGLEVEL.INFO) {
                return true;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return true;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        } else if (this.logSystem.logLevel == LOGLEVEL.WARNING) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return false;
            } else if (loglevel == LOGLEVEL.INFO) {
                return false;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return true;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        } else if (this.logSystem.logLevel == LOGLEVEL.ERROR) {
            if (loglevel == LOGLEVEL.LIVE) {
                return true;
            } else if (loglevel == LOGLEVEL.ALL) {
                return false;
            } else if (loglevel == LOGLEVEL.DEBUG) {
                return false;
            } else if (loglevel == LOGLEVEL.INFO) {
                return false;
            } else if (loglevel == LOGLEVEL.WARNING) {
                return false;
            } else if (loglevel == LOGLEVEL.ERROR) {
                return true;
            }
        }
        return false;
    }

    public List<String> getLastEntries(int max) {
        if (logEntries.size() <= max) {
            return logEntries;
        } else {
            return logEntries.subList(logEntries.size() - max, logEntries.size());
        }
    }

}
