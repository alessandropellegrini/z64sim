/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.queue;

public abstract class EventDispatchable {

    public EventDispatchable() {
        Dispatcher.registerDispatchable(this);
    }

    public abstract boolean dispatch(Events command);
}
