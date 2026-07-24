/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
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
        JMenu menuEdit = new JMenu(PropertyBroker.getMessageFromBundle("menu.edit"));

        JMenuItem undoMenu = new JMenuItem(AppActions.UNDO);
        menuEdit.add(undoMenu);

        JMenuItem redoMenu = new JMenuItem(AppActions.REDO);
        menuEdit.add(redoMenu);

        this.add(menuEdit);
    }
}
