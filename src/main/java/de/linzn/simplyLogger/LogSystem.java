package de.linzn.simplyLogger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogSystem {

    private Logger logger;

    java.util.logging.Logger fileLogger;
    LOGLEVEL logLevel;
    File logDirectory;

    public LogSystem() {
        this.logLevel = LOGLEVEL.INFO;
        this.logDirectory = null;
        this.logger = new Logger(this);
    }

    public Logger getLogger() {
        return logger;
    }

    public LOGLEVEL getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LOGLEVEL logLevel) {
        this.logLevel = logLevel;
    }

    public void setFileLogger(String appName, File logDirectory) {
        this.logDirectory = logDirectory;
        this.setupFileLogger(appName);
    }

    void writeToFile(String msg){
        if(this.fileLogger != null){
            this.fileLogger.info( msg);
        }
    }

    private void setupFileLogger(String appName) {
        fileLogger = java.util.logging.Logger.getLogger(appName);
        fileLogger.setUseParentHandlers(false);
        FileHandler fh;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH.mm.ss");

        try {
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            fh = new FileHandler(logDirectory.getName() + "/" + dateFormat.format(new Date().getTime()) + ".log");
            fileLogger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                            new Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            };

            fh.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
    }
}
