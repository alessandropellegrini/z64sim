/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.util.log;

import it.uniroma2.pellegrini.z64sim.controller.SettingsController;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Date;

public class Logger {
    private static PrintStream logFile = System.out;
    private static LogLevel level = LogLevel.WARN;
    private static boolean showDateTime = false;
    private static MessageFormat messageFormatter = new MessageFormat("[{1}] {0}: {2}");

    String className;

    private Logger() {
    }

    public Logger(String className) {
        // Abbreviate everything except the last part of the package and the class name
        this.className = className.replaceAll("\\B\\w+(\\.[a-z])","$1");
    }

    public static void init() {
        if(SettingsController.getLogFile() != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(SettingsController.getLogFile(), true);
                logFile = new PrintStream(fos);
                // The purpose of logging to a file is to allow prospective students to send
                // bug reports when something will definitely go wrong.
                // Inetrnal logging might be not enough, so it's better to capture
                // everything that's commonly sent to the console.
                System.setOut(logFile);
                System.setErr(logFile);
            } catch(FileNotFoundException e) {
                logFile = System.out;
                logFile.println(MessageFormat.format("{0} Unable to open log file {1}, reverting to stdout.", "[ERROR]", SettingsController.getLogFile()));
                e.printStackTrace();
            }
        }

        level = SettingsController.getLogLevel();
        showDateTime = SettingsController.getLogShowDateTime();
        if(showDateTime)
            messageFormatter = new MessageFormat("{0,date,short} {0,time,short} - [{2}] {1}: {3}", SettingsController.getLocale());
    }

    public static synchronized void doLog(String className, int levelInt, String msg) {
        if(showDateTime) {
            Object[] args = {new Date(), className, LogLevel.intToLevel(levelInt), msg};
            logFile.println(messageFormatter.format(args));
        } else {
            Object[] args = {className, LogLevel.intToLevel(levelInt), msg};
            logFile.println(messageFormatter.format(args));
        }
    }

    public void trace(String msg) {
        if(level.toInt() >= LogLevel.TRACE.toInt())
            doLog(this.className, 4, msg);
    }

    public void trace(String fmt, Object... params) {
        this.trace(MessageFormat.format(fmt, params));
    }

    public void debug(String msg) {
        if(level.toInt() >= LogLevel.DEBUG.toInt())
            doLog(this.className, 3, msg);
    }

    public void debug(String fmt, Object... params) {
        this.debug(MessageFormat.format(fmt, params));
    }

    public void info(String msg) {
        if(level.toInt() >= LogLevel.INFO.toInt())
            doLog(this.className, 2, msg);
    }

    public void info(String fmt, Object... params) {
        this.info(MessageFormat.format(fmt, params));
    }

    public void warn(String msg) {
        if(level.toInt() >= LogLevel.WARN.toInt())
            doLog(this.className, 1, msg);
    }

    public void warn(String fmt, Object... params) {
        this.warn(MessageFormat.format(fmt, params));
    }

    public void error(String msg) {
        if(level.toInt() >= LogLevel.ERROR.toInt())
            doLog(this.className, 0, msg);
    }

    public void error(String fmt, Object... params) {
        this.error(MessageFormat.format(fmt, params));
    }

}
