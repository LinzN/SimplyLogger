package de.linzn.simplyLogger;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

class CustomConsoleHandler extends ConsoleHandler {

    private LogSystem logSystem;

    public CustomConsoleHandler(LogSystem logSystem) {
        this.logSystem = logSystem;
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        logSystem.getLogger().addToLogList(record);
    }


    @Override
    public void close() {
        super.close();
    }
}