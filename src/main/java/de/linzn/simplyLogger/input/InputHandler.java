package de.linzn.simplyLogger.input;

public interface InputHandler {

    /**
     * Called method to handle console inputs
     *
     * @param inputCommand input command
     * @param args         arguments after command
     */
    void onConsoleInput(String inputCommand, String[] args);
}
