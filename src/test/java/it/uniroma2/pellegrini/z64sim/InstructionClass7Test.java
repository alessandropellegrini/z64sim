/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass7;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 7 — I/O instructions (in, out)")
public class InstructionClass7Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("in reads 0 from unmapped port")
    public void testIn() throws ParseException, SimulatorException {
        OperandImmediate ioport = new OperandImmediate(0x123);
        Instruction in = new InstructionClass7("in", 4, ioport);
        in.run();
        // No device mapped to port 0x123 — RAX should be 0
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("out to unmapped port is a no-op")
    public void testOut() throws ParseException, SimulatorException {
        OperandImmediate ioport = new OperandImmediate(0x123);
        Instruction out = new InstructionClass7("out", 4, ioport);
        // Should not throw — just a no-op when no device is mapped
        assertDoesNotThrow(out::run);
    }
}
