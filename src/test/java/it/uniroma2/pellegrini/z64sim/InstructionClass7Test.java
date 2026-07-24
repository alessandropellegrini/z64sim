/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass7;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 7 — I/O instructions (in, out) — currently unsupported")
public class InstructionClass7Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("in throws UnsupportedOperationException")
    public void testIn() throws ParseException {
        OperandImmediate ioport = new OperandImmediate(0x123);
        Instruction in = new InstructionClass7("in", 4, ioport);
        assertThrows(UnsupportedOperationException.class, in::run);
    }

    @Test
    @DisplayName("out throws UnsupportedOperationException")
    public void testOut() throws ParseException {
        OperandImmediate ioport = new OperandImmediate(0x123);
        Instruction out = new InstructionClass7("out", 4, ioport);
        assertThrows(UnsupportedOperationException.class, out::run);
    }
}
