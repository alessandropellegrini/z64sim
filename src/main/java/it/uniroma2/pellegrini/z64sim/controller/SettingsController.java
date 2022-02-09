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
import it.uniroma2.pellegrini.z64sim.queue.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsController extends Controller {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);
    private static SettingsController instance = null;

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
            try {
                instance.settings = Settings.getDefaultConfiguration();
                instance.settings.persist();
            } catch (SettingsException ex) {
                log.error(ex.getMessage());
            }
        }
    }

    @Override
    public boolean dispatch(Events command) {
        return false;
    }

    public static Locale getLocale() {
        return instance.locale;
    }



    private static class Settings {
        static final String configurationDirectoryPath = System.getProperty("user.home") + System.getProperty("file.separator") + ".z64sim" ;
        static final String configurationFilePath = configurationDirectoryPath + System.getProperty("file.separator") + "z64sim.cnf" ;
        static final ObjectMapper objectMapper = new ObjectMapper();

        // Configuration options
        private String uiLang;
        private String theme;
        private List<String> openFiles;

        private Settings() {
            // Configuration defaults
            this.uiLang = "en";
            this.theme = "light";
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
                settings = objectMapper.readValue(configurationFilePath, Settings.class);
            } catch (IOException e) {
                throw new SettingsException("Unable to load configuration file: " + configurationFilePath);
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
    }
}
