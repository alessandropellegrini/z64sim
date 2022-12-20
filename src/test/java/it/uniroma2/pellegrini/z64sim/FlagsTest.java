/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FlagsTest {
    final short OF = 1 << 11;
    final short DF = 1 << 10;
    final short IF = 1 << 9;
    final short SF = 1 << 7;
    final short ZF = 1 << 6;
    final short PF = 1 << 2;
    final short oneF = 1 << 1;
    final short CF = 1;

    @BeforeEach
    public void resetFlags() {
        SimulatorController.getCpuState().setCF(false);
        SimulatorController.getCpuState().setOF(false);
        SimulatorController.getCpuState().setPF(false);
        SimulatorController.getCpuState().setSF(false);
        SimulatorController.getCpuState().setZF(false);
        SimulatorController.getCpuState().setDF(false);
        SimulatorController.getCpuState().setIF(false);
    }

    @Test
    @DisplayName("Carry Flag")
    public void testCF() {
        SimulatorController.updateFlags(255, 7, 262, 1);
        Assertions.assertEquals(oneF | CF, SimulatorController.getCpuState().getFlags());
    }

    @Test
    @DisplayName("Parity flag")
    public void testPF() {
        SimulatorController.updateFlags(0, 1, 1, 1);
        Assertions.assertEquals(PF | oneF, SimulatorController.getCpuState().getFlags());
    }

    @Test
    @DisplayName("Zero Flag")
    public void testZF() {
        SimulatorController.updateFlags(0, 0, 0, 1);
        Assertions.assertEquals(ZF | oneF, SimulatorController.getCpuState().getFlags());
    }

    @Test
    @DisplayName("Sign Flag")
    public void testSF() {
        SimulatorController.updateFlags(-1, 0, -1, 1);
        Assertions.assertEquals(SF | oneF, SimulatorController.getCpuState().getFlags());
    }

    @Test
    @DisplayName("Overflow Flag")
    public void testOF() {
        SimulatorController.updateFlags(127, 2, 129, 1);
        Assertions.assertEquals(OF | SF | oneF, SimulatorController.getCpuState().getFlags());
        resetFlags();
        SimulatorController.updateFlags(161, 160, 321, 1);
        Assertions.assertEquals(OF | CF | oneF, SimulatorController.getCpuState().getFlags());
        resetFlags();
        SimulatorController.updateFlags(8, 2, 10, 1);
        Assertions.assertEquals(oneF, SimulatorController.getCpuState().getFlags());
        resetFlags();
        SimulatorController.updateFlags(135, 253, 388, 1);
        Assertions.assertEquals(SF | CF | oneF, SimulatorController.getCpuState().getFlags());
    }
}
