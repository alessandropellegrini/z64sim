/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {
    private static final Logger log = LoggerFactory.getLogger(InstructionClass5.class);

    private static MainController instance = null;

    private MainController() {}

    public enum command {
      QUIT,
    };

    public void dispatch(command command) {
        switch(command) {
            case QUIT:
                break;
            default:
                log.error("Ignoring unexpected event: {}", command);
        }
    }

    public static MainController getInstance() {
        if(instance != null)
            instance = new MainController();

        return instance;
    }
}
