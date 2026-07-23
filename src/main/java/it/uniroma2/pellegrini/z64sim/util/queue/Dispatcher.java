/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.util.queue;

import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger();

    private static final List<EventDispatchable> destinations = new CopyOnWriteArrayList<>();

    private Dispatcher() {}

    /**
     * Dispatch an event to all registered handlers.
     * If called from outside the EDT, the dispatch is marshalled onto the EDT
     * via SwingUtilities.invokeLater to ensure Swing thread safety.
     */
    public static void dispatch(Events command) {
        if (command == Events.QUIT) {
            // TODO: move to main controller
            SettingsController.persist();
            System.exit(0);
        }

        if (SwingUtilities.isEventDispatchThread()) {
            doDispatch(command);
        } else {
            SwingUtilities.invokeLater(() -> doDispatch(command));
        }
    }

    private static void doDispatch(Events command) {
        for (EventDispatchable eventDispatchable : destinations) {
            eventDispatchable.dispatch(command);
        }
    }

    public static void registerDispatchable(EventDispatchable obj) {
        destinations.add(obj);
    }
}
