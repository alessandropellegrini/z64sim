/**
 * SPDX-FileCopyrightText: 2023 Luca Gasperini <luca.gasperini@xsoftware.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.util.sys;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum OS {
    UNKNOWN,
    LINUX,
    UNIX,
    WINDOWS,
    MAC;
    @NotNull
    public static String getConfigFilePath() {
        String configDir = getConfigDir();
        // just return empty string if cant find a config dir
        if (configDir.isEmpty()) {
            return "";
        }

        // else return config file path
        return configDir + getSeparator() + getConfigFileName();
    }
    @NotNull
    public static String getNameOS() {
        return System.getProperty("os.name");
    }
    @NotNull
    public static String getHomeDir() {
        return System.getProperty("user.home");
    }
    @NotNull
    public static String getUserName() {
        return System.getProperty("user.name");
    }
    @NotNull
    public static String getSeparator() {
        return System.getProperty("file.separator");
    }

    public static OS getCurrentOS() {
        String osname = getNameOS().toLowerCase();

        // If OS is windows
        if (osname.contains("win")) {
            return WINDOWS;
        }
        // If OS is mac
        if (osname.contains("mac")) {
            return MAC;
        }
        // If OS is unix
        if (osname.contains("nix")) {
            return UNIX;
        }
        // If OS is linux
        if (osname.contains("nux")) {
            return LINUX;
        }
        // Else if cant determine which OS
        return UNKNOWN;
    }

    @NotNull
    @Contract(pure = true)
    public static String getConfigFileName() {
        return "z64sim.cnf";
    }
    @NotNull
    public static String getConfigDir() {

        OS currentOS = getCurrentOS();

        switch (currentOS) {
            case LINUX:
            case UNIX:
                return getConfigDirUnix();
            case WINDOWS:
                return getConfigDirWindows();
            case MAC:
                return getConfigDirMac();
            default:
                // return an empty string if cant figure out which os
                return "";
        }
    }

    @NotNull
    private static String getConfigDirUnix() {
        // try to use xdg
        String xdg = System.getenv("XDG_CONFIG_HOME");
        if (xdg != null) {
            return xdg + "/.z64sim";
        }

        // if cant use xdg try fallback to home + ".config/"
        return getHomeDir() + "/.config/z64sim";
    }

    @NotNull
    private static String getConfigDirWindows() {
        // try to use APPDATA
        String appdata = System.getenv("APPDATA");
        if (appdata != null) {
            return appdata + "\\z64sim";
        }

        String home = getHomeDir();
        // based on https://learn.microsoft.com/it-it/windows/win32/sysinfo/operating-system-version
        // check if system version is higher than XP
        final boolean IS_XP_HIGHER = (Float.parseFloat(System.getProperty("os.version")) > 5.2f);
        if (IS_XP_HIGHER) {
            return home + "\\AppData\\Roaming\\z64sim";
        } else {
            return "C:\\Documents and Settings\\" + getUserName() + "\\Application Data\\z64sim";
        }
    }

    @NotNull
    private static String getConfigDirMac() {
        String home = getHomeDir();
        return home + "/Library/Application Support/z64sim";
    }
}
