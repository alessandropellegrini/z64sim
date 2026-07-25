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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that assembles relocations.asm, loads it into the simulator,
 * and single-steps through the program verifying the control flow at each step.
 */
@DisplayName("Control-flow integration test — relocations.asm")
public class ControlFlowTest {

    private Program program;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/relocations.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(), "Assembly should succeed without errors");

        program = a.getProgram();
        SimulatorController.loadProgram(program);
    }

    /**
     * Helper: fetch and execute one instruction, returning its mnemonic.
     */
    private String stepAndGetMnemonic() throws SimulatorException {
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertNotNull(element, "Expected element at RIP=0x" + Long.toHexString(rip));
        assertInstanceOf(Instruction.class, element,
                "Expected Instruction at RIP=0x" + Long.toHexString(rip)
                + " but found " + element.getClass().getSimpleName());
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();
        return instruction.getMnemonic();
    }

    @Test
    @DisplayName("Step through relocations.asm — var=1 dispatches to case1")
    public void testControlFlowCase1() throws SimulatorException {
        long startRIP = SimulatorController.getCpuState().getRIP();
        assertTrue(program.getMemoryElementAt(startRIP) instanceof Instruction,
                "RIP=0x" + Long.toHexString(startRIP) + " should point to an Instruction");

        // Collect all mnemonics by stepping until hlt or failure
        List<String> trace = new ArrayList<>();
        int maxSteps = 20;
        for (int i = 0; i < maxSteps; i++) {
            String mnemonic = stepAndGetMnemonic();
            trace.add(mnemonic);
            if (mnemonic.equals("hlt")) break;
        }

        // Verify the exact execution trace for var=1 (dispatches to case1):
        //   main:    mov, mov, jmp
        //   case1:   mov, jmp
        //   leave:   jz (not taken), add, sub, jmp
        //   end:     hlt
        List<String> expectedTrace = Arrays.asList(
                "mov",  // movl var, %eax
                "mov",  // movq table(,%rax,8), %rax
                "jmp",  // jmp *%rax  --> .case1
                "mov",  // movl $1, %ebx
                "jmp",  // jmp .leave
                "jz",   // jz .end  (not taken, ZF=0)
                "add",  // addl $1, var
                "sub",  // subl $1, %ebx
                "jmp",  // jmp .end
                "hlt"   // hlt
        );
        assertEquals(expectedTrace, trace, "Execution trace should match expected control flow");

        // Verify final register state
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RBX) & 0xFFFFFFFFL,
                "RBX should be 0 after subl $1 from 1");
        assertTrue(SimulatorController.getZF(), "ZF should be set after subl $1 from 1");
    }

    @Test
    @DisplayName("Complete execution reaches hlt within expected number of steps")
    public void testExecutionTerminates() throws SimulatorException {
        int maxSteps = 100;
        int steps = 0;
        while (steps < maxSteps) {
            String mnemonic = stepAndGetMnemonic();
            steps++;
            if (mnemonic.equals("hlt")) {
                break;
            }
        }
        assertTrue(steps < maxSteps, "Program should halt within " + maxSteps + " steps, but ran " + steps);
        assertEquals(10, steps, "relocations.asm (var=1) should take exactly 10 steps to reach hlt");
    }
}
