/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertyBroker {
    private static final Logger log = LoggerFactory.getLogger();
    private static PropertyBroker instance = null;

    private final Properties z64simProperties = new Properties();
    private final ResourceBundle i18nBundle;

    private PropertyBroker() {
        try {
            this.z64simProperties.load(getClass().getClassLoader().getResourceAsStream("z64sim.properties"));
        } catch (IOException e) {
            log.error("Unable to load property file");
        }
        this.i18nBundle = ResourceBundle.getBundle("i18n", SettingsController.getLocale());
    }

    private static PropertyBroker getInstance() {
        if(instance == null)
            instance = new PropertyBroker();
        return instance;
    }

    public static String getPropertyValue(String propertyName) {
        return getInstance().z64simProperties.getProperty(propertyName);
    }

    public static String getMessageFromBundle(String key) {
        try {
            return getInstance().i18nBundle.getString(key);
        } catch (MissingResourceException e) {
            return "[" + key + "]";
        }
    }

    public static String getMessageFromBundle(String key, Object... params) {
        try {
            return MessageFormat.format(getMessageFromBundle(key), params);
        } catch (MissingResourceException e) {
            return "[" + key + "]";
        }
    }
}
