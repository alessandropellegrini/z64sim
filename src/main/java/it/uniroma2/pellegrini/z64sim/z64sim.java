/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import it.uniroma2.pellegrini.z64sim.controller.MainController;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;

public class z64sim {

    public static void main(String[] args) {
        SettingsController.init();
        Logger.init(); // Must come after settings initialization
        MainController.init();

        if(SettingsController.getTheme().equals("light"))
            FlatLightLaf.setup();
        else
            FlatDarkLaf.setup();

        MainWindow.getInstance().show();
    }
}
