/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.SettingsWindow;

public class MainController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();

    private static MainController instance = null;

    private MainController() {}

    @Override
    public boolean dispatch(Events command) {
        if(command == Events.SHOW_SETTINGS_DIALOG)
            this.showSettingsDialog();

        return false;
    }

    public static void init() {
        log.trace("Initializing Main Controller");
        instance = new MainController();
    }

    private void showSettingsDialog() {
        SettingsWindow dialog = new SettingsWindow();
        dialog.setTitle(PropertyBroker.getMessageFromBundle("menu.file.settings"));
        dialog.pack();
        dialog.setVisible(true);
    }

}
