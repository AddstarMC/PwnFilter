/*
 * PwnFilter -- Regex-based User Filter Plugin for Bukkit-based Minecraft servers.
 * Copyright (c) 2013 Pwn9.com. Tremor77 <admin@pwn9.com> & Sage905 <patrick@toal.ca>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.pwn9.PwnFilter.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Unified Logging interface for PwnFilter-related messages
 * User: ptoal
 * Date: 13-10-02
 * Time: 4:50 PM
 */
public class LogManager {
    public static Level ruleLogLevel;
    // Logging variables
    public static DebugModes debugMode = DebugModes.off;
    public static Logger logger;
    private static File logFolder;

    private FileHandler logfileHandler;

    private static LogManager _instance;

    private LogManager() {
    }

    public static Level getRuleLogLevel() {
        return ruleLogLevel;
    }

    public static void setDebugMode(String mode) {
        try {
            debugMode = LogManager.DebugModes.valueOf(mode);
        } catch (IllegalArgumentException e) {
            debugMode = LogManager.DebugModes.off;
        }
    }

    public static void setRuleLogLevel(String level) {
        try {
            LogManager.ruleLogLevel = Level.parse(level.toUpperCase());
        } catch (IllegalArgumentException e ) {
            LogManager.ruleLogLevel = Level.INFO;
        }
    }

    public void debugLow(String message) {
        if (debugMode.compareTo(DebugModes.low) >= 0) {
            logger.finer(message);
        }
    }

    public void debugMedium(String message) {
        if (debugMode.compareTo(DebugModes.medium) >= 0) {
            logger.finer(message);
        }
    }

    public void debugHigh(String message) {
        if (debugMode.compareTo(DebugModes.high) >= 0) {
            logger.finer(message);
        }
    }

    public static LogManager getInstance(Logger l, File f) {
        if (_instance == null) {
            _instance = new LogManager();
            logger = l;
            logFolder = f;
        }
        return _instance;
    }

    public static LogManager getInstance() {
        if (_instance == null) {
            throw new IllegalStateException("LogManager not yet initialized!");
        } else {
            return _instance;
        }
    }

    public void stop() {
        if (logfileHandler != null) {
            logfileHandler.close();
            LogManager.logger.removeHandler(logfileHandler);
            logfileHandler = null;
        }
    }

    public enum DebugModes {
        off, // Off
        low, // Some debugging
        medium, // More debugging
        high, // You're crazy. :)
    }

    public void start() {
        if (logfileHandler == null) {
            try {
                // For now, one logfile, like the old way.
                String fileName =  new File(logFolder, "pwnfilter.log").toString();
                logfileHandler = new FileHandler(fileName, true);
                SimpleFormatter f = new PwnFormatter();
                logfileHandler.setFormatter(f);
                logfileHandler.setLevel(Level.FINEST); // Catch all log messages
                LogManager.logger.addHandler(logfileHandler);
                LogManager.logger.info("Now logging to " + fileName );

            } catch (IOException e) {
                LogManager.logger.warning("Unable to open logfile.");
            } catch (SecurityException e) {
                LogManager.logger.warning("Security Exception while trying to add file Handler");
            }
        }

    }

}
