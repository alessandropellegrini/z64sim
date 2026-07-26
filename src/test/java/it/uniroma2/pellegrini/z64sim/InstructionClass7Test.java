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

    @Test
    @DisplayName("getType and encode for Class 7 instructions")
    public void testGetTypeAndEncode() throws ParseException {
        OperandImmediate ioport = new OperandImmediate(0x3F8);
        InstructionClass7 inInst = new InstructionClass7("in", 4, ioport);
        assertEquals(0, inInst.getType());
        byte[] buf = inInst.getValue();
        assertEquals(8, buf.length);
        assertEquals((byte) 0x70, buf[0]);
        // SS=2, DS=2, DI=1, Mem=0 -> 10 10 01 00 = 0xA4 = (byte) -92
        assertEquals((byte) 0xA4, buf[1]);
        assertEquals((byte) 0xF8, buf[4]);
        assertEquals((byte) 0x03, buf[5]);

        InstructionClass7 outInst = new InstructionClass7("out", 4, ioport);
        assertEquals(1, outInst.getType());

        InstructionClass7 insInst = new InstructionClass7("ins", 1, null);
        assertEquals(2, insInst.getType());
        buf = insInst.getValue();
        assertEquals((byte) 0x72, buf[0]);
        assertEquals((byte) 0x00, buf[1]); // SS=0, DS=0, DI=0, Mem=0

        InstructionClass7 outsInst = new InstructionClass7("outs", 1, null);
        assertEquals(3, outsInst.getType());
    }
}
