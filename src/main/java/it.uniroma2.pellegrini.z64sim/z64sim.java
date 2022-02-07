/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.MainController;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;

public class z64sim {
    public static void main(String[] args) {
        MainWindow.getInstance(MainController.getInstance()).show();
    }
}
