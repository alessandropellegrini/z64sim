/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import it.uniroma2.pellegrini.z64sim.controller.MainController;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.UpdateController;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import it.uniroma2.pellegrini.z64sim.view.Splash;

import javax.swing.*;

public class z64sim {

    public static void main(String[] args) {
        // Capture the application classloader from the main thread.
        // On JBR 11, the EDT may have a different context classloader which
        // prevents UIDefaults from finding FlatLaf's UI delegate classes in fat JARs.
        final ClassLoader appClassLoader = z64sim.class.getClassLoader();

        Splash splashScreen = new Splash(4);

        splashScreen.step("Loading settings");
        SettingsController.init();
        splashScreen.step("Initializing logger");
        Logger.init(); // *Must* come after settings initialization

        splashScreen.step("Initializing controllers");
        MainController.init();
        SimulatorController.init();

        splashScreen.step("Initializing UI");
        splashScreen.close();

        SwingUtilities.invokeLater(() -> {
            // Ensure the EDT uses the same classloader as the main thread
            Thread.currentThread().setContextClassLoader(appClassLoader);

            if(SettingsController.getTheme().equals("light"))
                FlatLightLaf.setup();
            else
                FlatDarkLaf.setup();

            MainWindow.showMainWindow(args.length > 0 ? args[0] : null);

            UpdateController.init();
        });
    }
}
