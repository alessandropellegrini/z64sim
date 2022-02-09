/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import javax.swing.*;

public class MainWindowMenu extends JMenuBar {
    private JMenu menu, smenu;
    private JMenuItem e1, e2, e3, e4, e5, e6;

    public MainWindowMenu() {

        menu = new JMenu("Menu");
        smenu = new JMenu("Sub menu");

        e1 = new JMenuItem("Item 1");
        e2 = new JMenuItem("Item 2");
        e3 = new JMenuItem("Item 3");
        e4 = new JMenuItem("Item 4");
        e5 = new JMenuItem("Item 5");
        e6 = new JMenuItem("Item 6");

        menu.add(e1);
        menu.add(e2);
        menu.add(e3);
        smenu.add(e4);
        smenu.add(e5);
        smenu.add(e6);
        menu.add(smenu);

        this.add(menu);
    }
}
