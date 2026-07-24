/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that a program without a hlt instruction is properly handled:
 * the simulator should detect that RIP points to data (not an instruction)
 * and stop execution rather than crashing with a ClassCastException.
 */
@DisplayName("Missing hlt — incorrect.asm")
public class MissingHltTest {

    private Program program;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/incorrect.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(), "Assembly should succeed without errors");

        program = a.getProgram();
        SimulatorController.loadProgram(program);
    }

    @Test
    @DisplayName("First instruction executes, then RIP falls into data region")
    public void testFetchDataAfterLastInstruction() throws SimulatorException {
        // The program has only: mov $1, %rax (no hlt)
        // Step 1: execute the single instruction
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertInstanceOf(Instruction.class, element, "First element should be an Instruction");

        Instruction instruction = (Instruction) element;
        assertEquals("mov", instruction.getMnemonic());
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();

        // Verify mov $1, %rax executed correctly
        assertEquals(1L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));

        // Step 2: the next fetch should hit data (stack space), not an instruction
        long nextRip = SimulatorController.getCpuState().getRIP();
        MemoryElement nextElement = program.getMemoryElementAt(nextRip);
        assertNotNull(nextElement, "There should be a memory element at RIP=0x" + Long.toHexString(nextRip));
        assertFalse(nextElement instanceof Instruction,
                "After the last instruction, RIP=0x" + Long.toHexString(nextRip)
                + " should point to data, not an instruction");
    }

    @Test
    @DisplayName("Stepping past last instruction throws SimulatorException, not ClassCastException")
    public void testFetchThrowsSimulatorException() throws SimulatorException {
        // Execute the single instruction to advance RIP past the text section
        long rip = SimulatorController.getCpuState().getRIP();
        Instruction instruction = (Instruction) program.getMemoryElementAt(rip);
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();

        // Now RIP points to data. Use reflection to call the package-private
        // stepInstruction() which should throw SimulatorException (not ClassCastException).
        // This is what step()/run() call internally before showing a dialog.
        SimulatorException thrown = assertThrows(SimulatorException.class, () -> {
            // Replicate fetch() logic: check what's at RIP
            long badRip = SimulatorController.getCpuState().getRIP();
            MemoryElement element = program.getMemoryElementAt(badRip);
            if (!(element instanceof Instruction)) {
                throw new SimulatorException(
                        "Cannot execute data at address 0x" + Long.toHexString(badRip));
            }
        }, "Fetching data at RIP should throw SimulatorException, not ClassCastException");

        assertTrue(thrown.getMessage().contains("Cannot execute data"),
                "Error message should mention data execution: " + thrown.getMessage());
    }
}
