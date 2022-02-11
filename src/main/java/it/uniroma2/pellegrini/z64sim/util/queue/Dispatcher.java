/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.util.queue;

import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger();
    private static final Dispatcher instance = null;

    private static final List<EventDispatchable> destinations = new ArrayList<>();

    private Dispatcher() {}

    public static void dispatch(Events command) {
        if (command == Events.QUIT) {
            // TODO: move to main controller
            SettingsController.persist();
            System.exit(0);
        }

        for(EventDispatchable eventDispatchable : destinations) {
            eventDispatchable.dispatch(command);
        }
    }

    public static void registerDispatchable(EventDispatchable obj) {
        destinations.add(obj);
    }
}
