/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.util.log;

public enum LogLevel {
    ERROR(0, "ERROR"),
    WARN(1, "WARN"),
    INFO(2, "INFO"),
    DEBUG(3, "DEBUG"),
    TRACE(4, "TRACE");

    private final int levelInt;
    private final String levelStr;

    LogLevel(int i, String s) {
        this.levelInt = i;
        this.levelStr = s;
    }

    public int toInt() {
        return this.levelInt;
    }

    public static LogLevel intToLevel(int levelInt) {
        switch(levelInt) {
            case 0:
                return ERROR;
            case 1:
                return WARN;
            case 2:
                return INFO;
            case 3:
                return DEBUG;
            case 4:
                return TRACE;
            default:
                throw new IllegalArgumentException(levelInt + " is not a valid log level.");
        }
    }

    public static LogLevel stringToLevel(String levelString) {
        switch(levelString.toUpperCase()) {
            case "ERROR":
                return ERROR;
            case "WARN":
                return WARN;
            case "INFO":
                return INFO;
            case "DEBUG":
                return DEBUG;
            case "TRACE":
                return TRACE;
            default:
                throw new IllegalArgumentException(levelString + " is not a valid log level.");
        }
    }

    public String toString() {
        return this.levelStr;
    }
}

