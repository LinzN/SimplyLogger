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

import de.linzn.simplyLogger.input.InputHandler;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reads console input on a daemon thread and passes it to the registered input handler.
 * <p>
 * Works with and without an interactive console: when no console is attached (e.g. running as a
 * systemd service) stdin is read directly. On end of input the thread stops instead of spinning.
 */
class InputManager implements Runnable {

    private final InputHandler inputHandler;
    private final AtomicBoolean enabled;
    private final LogSystem logSystem;
    private final Thread runThread;

    /**
     * Create input manager instance
     *
     * @param inputHandler input handler to register
     * @param logSystem    instance of parent log system
     */
    InputManager(InputHandler inputHandler, LogSystem logSystem) {
        this.inputHandler = inputHandler;
        this.logSystem = logSystem;
        this.enabled = new AtomicBoolean(true);
        this.runThread = new Thread(this, "simplyLogger-Input");
        this.runThread.setDaemon(true);
        this.runThread.start();
    }

    /**
     * Stop the input thread
     */
    void stopThread() {
        this.enabled.set(false);
        this.runThread.interrupt();
    }

    @Override
    public void run() {
        Console console = System.console();
        BufferedReader reader = console == null
                ? new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()))
                : null;
        while (enabled.get()) {
            String input;
            try {
                input = console != null ? console.readLine() : reader.readLine();
            } catch (IOException | RuntimeException e) {
                if (enabled.get()) {
                    this.logSystem.getLogger().ERROR("Console input failed, input disabled: " + e);
                }
                return;
            }
            if (input == null) {
                /* End of input (stdin closed or redirected from /dev/null) */
                this.logSystem.getLogger().DEBUG("Console input closed, input disabled");
                return;
            }
            input = input.trim();
            if (input.isEmpty()) {
                continue;
            }
            String[] inputArray = input.split("\\s+");
            String command = inputArray[0];
            String[] args = Arrays.copyOfRange(inputArray, 1, inputArray.length);
            try {
                inputHandler.onConsoleInput(command, args);
            } catch (Exception e) {
                this.logSystem.getLogger().ERROR(e);
            }
        }
    }
}
