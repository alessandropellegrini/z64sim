/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryAccessTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("Memory.getValueAt retrieves bytes inside multi-byte instructions without NPE")
    public void testReadInsideInstruction() throws ParseException {
        String code = ".org 0x800\n.text\nmain:\n  hlt\n";
        Assembler a = new Assembler(new StringReader(code));
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty());
        Program p = a.getProgram();
        SimulatorController.loadProgram(p);

        // hlt at 0x800 has size 8. Bytes 0x800..0x807 should be readable.
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 8; i++) {
                byte b = Memory.getValueAt(0x800 + i);
            }
        });
    }

    @Test
    @DisplayName("Memory access violation error message formats address in hexadecimal")
    public void testMemoryAccessViolationHexAddress() throws ParseException {
        String code = ".org 0x800\n.text\nmain:\n  hlt\n";
        Assembler a = new Assembler(new StringReader(code));
        a.Program();
        Program p = a.getProgram();
        SimulatorController.loadProgram(p);

        // Accessing an address beyond allocated program bounds
        long outOfBoundsAddr = 0x80000L;
        OperandMemory op = new OperandMemory(-1, -1, -1, -1, (int) outOfBoundsAddr, 8);

        SimulatorException ex = assertThrows(SimulatorException.class, () -> {
            SimulatorController.getOperandValue(op);
        });

        assertTrue(ex.getMessage().contains("0x80000"),
                "Exception message should format address in hex, got: " + ex.getMessage());
    }

    @Test
    @DisplayName("btb $0, %al assembles and disassembles with btb suffix")
    public void testBtbDisassembly() throws ParseException {
        String code = ".org 0x800\n.text\nmain:\n  btb $0, %al\n  hlt\n";
        Assembler a = new Assembler(new StringReader(code));
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty());
        Program p = a.getProgram();

        long rip = p.text.getTarget();
        assertEquals("btb $0, %al", p.getMemoryElementAt(rip).toString());
    }
}
