/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
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
    @DisplayName("hlt leaves RIP unchanged (halted flag prevents re-fetch)")
    public void testHlt() throws ParseException, SimulatorException {
        SimulatorController.getCpuState().setRIP(0x1000L);
        Instruction inst = new InstructionClass0("hlt", null);
        inst.run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("int throws UnsupportedOperationException")
    public void testInt() throws ParseException {
        Instruction inst = new InstructionClass0("int", new OperandImmediate(0x80));
        assertThrows(UnsupportedOperationException.class, inst::run);
    }

    @Test
    @DisplayName("getType and encode for Class 0 instructions")
    public void testGetTypeAndEncode() throws ParseException {
        InstructionClass0 hlt = new InstructionClass0("hlt", null);
        assertEquals(1, hlt.getType());
        byte[] hltBuf = hlt.getValue();
        assertEquals(8, hltBuf.length);
        assertEquals((byte) 0x01, hltBuf[0]);
        for (int i = 1; i < 8; i++) {
            assertEquals((byte) 0x00, hltBuf[i]);
        }

        InstructionClass0 nop = new InstructionClass0("nop", null);
        assertEquals(2, nop.getType());
        byte[] nopBuf = nop.getValue();
        assertEquals(8, nopBuf.length);
        assertEquals((byte) 0x02, nopBuf[0]);

        InstructionClass0 intInst = new InstructionClass0("int", new OperandImmediate(0x80));
        assertEquals(3, intInst.getType());
        byte[] intBuf = intInst.getValue();
        assertEquals(8, intBuf.length);
        assertEquals((byte) 0x03, intBuf[0]);
        assertEquals((byte) 0x80, intBuf[7]);
        for (int i = 1; i < 7; i++) {
            assertEquals((byte) 0x00, intBuf[i]);
        }
    }
}
