/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);
    private static final Dispatcher instance = null;

    private static final List<EventDispatchable> destinations = new ArrayList<>();

    private Dispatcher() {}

    public static void dispatch(Events command) {
        if (command == Events.QUIT) {
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
