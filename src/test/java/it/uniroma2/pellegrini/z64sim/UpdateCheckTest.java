/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.UpdateController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateCheckTest {

    @Test
    @DisplayName("Testing Disassembler")
    public void testUpdateCheck() {
        UpdateController.init();
        while(!UpdateController.isCheckCompleted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
