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