/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SettingsException;
import it.uniroma2.pellegrini.z64sim.util.log.LogLevel;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SettingsController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();;
    private static SettingsController instance = null;

    // To validate configuration file
    private static final String[] locales = {"en", "es", "fr", "it"};
    private static final String[] themes = {"dark", "light"};
    private static final String[] logLevels = {"trace", "debug", "info", "warn", "error"};

    // Actual members of the singleton
    private Settings settings = null;
    private Locale locale = Locale.getDefault();

    private SettingsController() {}

    public static void init() {
        instance = new SettingsController();

        try {
            instance.settings = Settings.loadConfiguration();
        } catch (SettingsException e) {
            log.warn(e.getMessage());
            // Try to create the default configuration
            instance.settings = Settings.getDefaultConfiguration();
        }

        validateConfig();

        // Immediately rewrite the configuration to file. This allows to "upgrade" the configuration
        // file, for example when moving from a previous version to a newer version, storing the defaults.
        try {
            instance.settings.persist();
        } catch (SettingsException e) {
            log.warn(e.getMessage());
        }

        // Update locale
        instance.locale = new Locale(instance.settings.getUiLang());
    }

    private static void validateConfig() {
        if(!Arrays.asList(locales).contains(getInstance().settings.getUiLang())) {
            log.warn(PropertyBroker.getMessageFromBundle("unexpected.configuration.value.for.uilang.0.using.default"), getInstance().settings.getUiLang());
            getInstance().settings.setUiLang(PropertyBroker.getPropertyValue("z64sim.ui.defaultLanguage"));
        }
        if(!Arrays.asList(themes).contains(getInstance().settings.getTheme())) {
            log.warn(PropertyBroker.getMessageFromBundle("unexpected.configuration.value.for.theme.0.using.default"), getInstance().settings.getTheme());
            getInstance().settings.setTheme(PropertyBroker.getPropertyValue("z64sim.ui.defaultTheme"));
        }
        if(!Arrays.asList(logLevels).contains(getInstance().settings.getLogLevel())) {
            log.warn(PropertyBroker.getMessageFromBundle("unexpected.configuration.value.for.loglevel.0.using.default"), getInstance().settings.getLogLevel());
            getInstance().settings.setLogLevel(PropertyBroker.getPropertyValue("z64sim.log.level"));
        }
    }

    @Override
    public boolean dispatch(Events command) {
        return false;
    }

    private static SettingsController getInstance() {
        if(instance == null)
            init();
        return instance;
    }

    public static Locale getLocale() {
        return getInstance().locale;
    }

    public static LogLevel getLogLevel() {
        return LogLevel.stringToLevel(getInstance().settings.getLogLevel());
    }

    public static boolean getLogShowDateTime() {
        return getInstance().settings.getLogShowDateTime();
    }

    public static String getLogFile() {
        return getInstance().settings.getLogOutFile();
    }

    public static String getUiLang() {
        return getInstance().settings.getUiLang();
    }

    public static String getTheme() {
        return getInstance().settings.getTheme();
    }

    private static class Settings {
        static final String configurationDirectoryPath = System.getProperty("user.home") + System.getProperty("file.separator") + ".z64sim" ;
        static final String configurationFilePath = configurationDirectoryPath + System.getProperty("file.separator") + "z64sim.cnf" ;
        static final ObjectMapper objectMapper = new ObjectMapper();

        // Configuration options
        private String uiLang;
        private String theme;
        private String logLevel;
        private boolean logShowDateTime;
        private String logOutFile;
        private List<String> openFiles;

        private Settings() {
            // Configuration defaults
            this.uiLang = PropertyBroker.getPropertyValue("z64sim.ui.defaultLanguage");
            this.theme = PropertyBroker.getPropertyValue("z64sim.ui.defaultTheme");
            this.logLevel = PropertyBroker.getPropertyValue("z64sim.log.level");
            this.logShowDateTime = Boolean.parseBoolean(PropertyBroker.getPropertyValue("z64sim.log.showDateTime"));
            this.logOutFile = null;
            this.openFiles = new ArrayList<>();
        }

        protected static Settings loadConfiguration() throws SettingsException {
            Settings settings;
            
            try {
                Files.createDirectories(Paths.get(configurationDirectoryPath));
            } catch (IOException e) {
                throw new SettingsException("Unable to create configuration file path: " + configurationDirectoryPath);
            }
            
            try {
                File configFile = new File(configurationFilePath);
                settings = objectMapper.readValue(configFile, Settings.class);
            } catch (IOException e) {
                throw new SettingsException("Unable to load configuration file " + configurationFilePath + ": " + e.getMessage());
            }

            return settings;
        }

        protected static Settings getDefaultConfiguration() {
            return new Settings();
        }

        protected void persist() throws SettingsException {
            try {
                PrintStream out = new PrintStream(configurationFilePath);
                String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
                out.println(json);
                out.close();
            } catch (FileNotFoundException | JsonProcessingException e) {
                throw new SettingsException("Unable to store configuration in: " + configurationFilePath + ": " + e.getMessage());
            }
        }

        public String getUiLang() {
            return uiLang;
        }

        public void setUiLang(String uiLang) {
            this.uiLang = uiLang;
        }

        public String getTheme() {
            return theme;
        }

        public void setTheme(String theme) {
            this.theme = theme;
        }

        public List<String> getOpenFiles() {
            return openFiles;
        }

        public void setOpenFiles(List<String> openFiles) {
            this.openFiles = openFiles;
        }

        public String getLogLevel() {
            return logLevel;
        }

        public void setLogLevel(String logLevel) {
            this.logLevel = logLevel;
        }

        public boolean getLogShowDateTime() {
            return logShowDateTime;
        }

        public void setLogShowDateTime(boolean logShowDateTime) {
            this.logShowDateTime = logShowDateTime;
        }

        public boolean isLogShowDateTime() {
            return logShowDateTime;
        }

        public String getLogOutFile() {
            return logOutFile;
        }

        public void setLogOutFile(String logOutFile) {
            this.logOutFile = logOutFile;
        }
    }
}
