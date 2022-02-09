/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass5;
import it.uniroma2.pellegrini.z64sim.queue.EventDispatchable;
import it.uniroma2.pellegrini.z64sim.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.SettingsWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController extends Controller {
    private static final Logger log = LoggerFactory.getLogger(InstructionClass5.class);

    private static MainController instance = null;

    private MainController() {}

    @Override
    public boolean dispatch(Events command) {
        return false;
    }

    private static MainController getInstance() {
        if(instance != null)
            instance = new MainController();

        return instance;
    }

    private void foo() {
        SettingsWindow dialog = new SettingsWindow();
        dialog.pack();
        dialog.setVisible(true);
    }

}
