package de.linzn.simplyLogger;

import de.linzn.simplyLogger.input.InputHandler;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
 class InputManager implements Runnable {

    private final InputHandler inputHandler;
    private final AtomicBoolean enabled;
    private final LogSystem logSystem;
    private Thread runThread;

     /**
      * Create input manager instance
      * @param inputHandler input handler to register
      * @param logSystem instance of parent log system
      */
    InputManager(InputHandler inputHandler, LogSystem logSystem) {
        this.inputHandler = inputHandler;
        this.logSystem = logSystem;
        this.enabled = new AtomicBoolean(true);
        this.startThread();
    }

     /**
      * Start thread for input
      */
    private void startThread() {
        Executors.newSingleThreadExecutor().submit(this);
    }

     /**
      * Stop the input thread
      */
    void stopThread() {
        this.enabled.set(false);
        if (this.runThread != null && !this.runThread.isInterrupted()) {
            this.runThread.interrupt();
        }
    }

    @Override
    public void run() {
        this.runThread = Thread.currentThread();
        while (enabled.get()) {
            try {
                String input = System.console().readLine();

                String[] inputArray = input.split(" ");
                String command = inputArray[0];

                String[] args = Arrays.copyOfRange(inputArray, 1, inputArray.length);
                inputHandler.onConsoleInput(command, args);
            } catch (Exception e) {
                this.logSystem.getLogger().ERROR(e);
            }
        }
    }
}
