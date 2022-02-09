/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import com.formdev.flatlaf.FlatDarkLaf;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;

public class z64sim {

    public static void main(String[] args) {
        SettingsController.init();
        FlatDarkLaf.setup();
        MainWindow.getInstance().show();
    }
}