/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import it.uniroma2.pellegrini.z64sim.controller.AppActions;

import javax.swing.*;

public class MainWindowMenu extends JMenuBar {

    public MainWindowMenu() {

        /* FILE */
        JMenu menuFile = new JMenu("File");

        JMenuItem settingsMenu = new JMenuItem(AppActions.SHOW_SETTINGS);
        menuFile.add(settingsMenu);

        JMenuItem quitMenu = new JMenuItem(AppActions.QUIT);
        menuFile.add(quitMenu);

        this.add(menuFile);

        /* EDIT */
        JMenu menu2 = new JMenu("Edit");
        JMenu smenu = new JMenu("Sub menu");
        JMenuItem e2 = new JMenuItem("Settings");
        smenu.add(e2);

        this.add(menu2);
    }
}
