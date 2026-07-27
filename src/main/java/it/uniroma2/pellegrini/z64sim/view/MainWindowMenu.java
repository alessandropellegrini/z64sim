/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.AppActions;

import javax.swing.*;

public class MainWindowMenu extends JMenuBar {

    public MainWindowMenu() {

        /* FILE */
        JMenu menuFile = new JMenu(PropertyBroker.getMessageFromBundle("menu.file"));

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

        menuEdit.addSeparator();

        JMenuItem findMenu = new JMenuItem(AppActions.FIND);
        menuEdit.add(findMenu);

        JMenuItem findReplaceMenu = new JMenuItem(AppActions.FIND_REPLACE);
        menuEdit.add(findReplaceMenu);

        this.add(menuEdit);
    }
}
