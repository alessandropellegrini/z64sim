/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass4;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 4 — Flag manipulation (clc/stc, clz/stz, cls/sts, clp/stp, clo/sto, cli/sti, cld/std)")
public class InstructionClass4Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("clc/stc: clear and set carry flag")
    public void testClcStc() throws SimulatorException {
        SimulatorController.setCF(true);
        new InstructionClass4("clc").run();
        assertFalse(SimulatorController.getCF());

        new InstructionClass4("stc").run();
        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("clz/stz: clear and set zero flag")
    public void testClzStz() throws SimulatorException {
        SimulatorController.setZF(true);
        new InstructionClass4("clz").run();
        assertFalse(SimulatorController.getZF());

        new InstructionClass4("stz").run();
        assertTrue(SimulatorController.getZF());
    }

    @Test
    @DisplayName("cls/sts: clear and set sign flag")
    public void testClsSts() throws SimulatorException {
        SimulatorController.setSF(true);
        new InstructionClass4("cls").run();
        assertFalse(SimulatorController.getSF());

        new InstructionClass4("sts").run();
        assertTrue(SimulatorController.getSF());
    }

    @Test
    @DisplayName("clp/stp: clear and set parity flag")
    public void testClpStp() throws SimulatorException {
        SimulatorController.setPF(true);
        new InstructionClass4("clp").run();
        assertFalse(SimulatorController.getPF());

        new InstructionClass4("stp").run();
        assertTrue(SimulatorController.getPF());
    }

    @Test
    @DisplayName("clo/sto: clear and set overflow flag")
    public void testCloSto() throws SimulatorException {
        SimulatorController.setOF(true);
        new InstructionClass4("clo").run();
        assertFalse(SimulatorController.getOF());

        new InstructionClass4("sto").run();
        assertTrue(SimulatorController.getOF());
    }

    @Test
    @DisplayName("cli/sti: clear and set interrupt flag")
    public void testCliSti() throws SimulatorException {
        SimulatorController.setIF(true);
        new InstructionClass4("cli").run();
        assertFalse(SimulatorController.getIF());

        new InstructionClass4("sti").run();
        assertTrue(SimulatorController.getIF());
    }

    @Test
    @DisplayName("cld/std: clear and set direction flag")
    public void testCldStd() throws SimulatorException {
        SimulatorController.setDF(true);
        new InstructionClass4("cld").run();
        assertFalse(SimulatorController.getDF());

        new InstructionClass4("std").run();
        assertTrue(SimulatorController.getDF());
    }

    @Test
    @DisplayName("getType and encode for Class 4 instructions")
    public void testGetTypeAndEncode() {
        String[] mnemonics = {"clc", "clp", "clz", "cls", "cli", "cld", "clo", "stc", "stp", "stz", "sts", "sti", "std", "sto"};
        for (int i = 0; i < mnemonics.length; i++) {
            InstructionClass4 inst = new InstructionClass4(mnemonics[i]);
            assertEquals(i, inst.getType());
            byte[] buf = inst.getValue();
            assertEquals(8, buf.length);
            byte expectedOpcode = (byte) (0x40 | i);
            assertEquals(expectedOpcode, buf[0]);
            for (int j = 1; j < 8; j++) {
                assertEquals((byte) 0x00, buf[j]);
            }
        }
    }
}
