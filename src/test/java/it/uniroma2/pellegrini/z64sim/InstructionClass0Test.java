/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass0;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 0 — System instructions (hlt, int)")
public class InstructionClass0Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("hlt displaces RIP backward by instruction size")
    public void testHlt() throws ParseException, SimulatorException {
        SimulatorController.getCpuState().setRIP(0x1000L);
        Instruction inst = new InstructionClass0("hlt", null);
        inst.run();
        assertEquals(0x1000L - 8, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("int throws UnsupportedOperationException")
    public void testInt() throws ParseException {
        Instruction inst = new InstructionClass0("int", new OperandImmediate(0x80));
        assertThrows(UnsupportedOperationException.class, inst::run);
    }
}
