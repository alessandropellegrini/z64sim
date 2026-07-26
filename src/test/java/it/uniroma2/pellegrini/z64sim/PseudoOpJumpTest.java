/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Jump Pseudo-Operations")
public class PseudoOpJumpTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    private void runJumpTest(String jumpInst, boolean cf, boolean zf, boolean sf, boolean of, long expectedRbx) throws Exception {
        String progStr = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0, %rbx\n" +
                "    " + jumpInst + " taken\n" +
                "    movq $1, %rbx\n" +
                "    jmp done\n" +
                "taken:\n" +
                "    movq $2, %rbx\n" +
                "done:\n" +
                "    hlt\n";

        Assembler a = new Assembler(new StringReader(progStr));
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(), "Syntax errors in assembly");
        Program program = a.getProgram();
        SimulatorController.loadProgram(program);

        // Step movq $0, %rbx (1 instruction)
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        if (element == null) fail("RIP out of bounds or null element");
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();

        // Set flags manually before the conditional jump
        SimulatorController.setCF(cf);
        SimulatorController.setZF(zf);
        SimulatorController.setSF(sf);
        SimulatorController.setOF(of);

        // Step remaining instructions until hlt
        stepUntilHlt(program);

        assertEquals(expectedRbx, SimulatorController.getCpuState().getRegisterValue(Register.RBX));
    }

    private void stepUntilHlt(Program program) throws SimulatorException {
        for (int i = 0; i < 500; i++) {
            long rip = SimulatorController.getCpuState().getRIP();
            MemoryElement element = program.getMemoryElementAt(rip);
            if (element == null) fail("RIP out of bounds or null element");
            Instruction instruction = (Instruction) element;
            SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
            instruction.run();
            if (instruction.getMnemonic().equals("hlt")) return;
        }
        fail("Program did not halt within 500 steps");
    }

    @Test
    public void testJbJumps() throws Exception {
        runJumpTest("jb", true, false, false, false, 2L);
    }

    @Test
    public void testJbNoJump() throws Exception {
        runJumpTest("jb", false, false, false, false, 1L);
    }

    @Test
    public void testJaeJumps() throws Exception {
        runJumpTest("jae", false, false, false, false, 2L);
    }

    @Test
    public void testJbeJumps() throws Exception {
        runJumpTest("jbe", true, false, false, false, 2L);
    }

    @Test
    public void testJbeJumpsOnZF() throws Exception {
        runJumpTest("jbe", false, true, false, false, 2L);
    }

    @Test
    public void testJbeNoJump() throws Exception {
        runJumpTest("jbe", false, false, false, false, 1L);
    }

    @Test
    public void testJaJumps() throws Exception {
        runJumpTest("ja", false, false, false, false, 2L);
    }

    @Test
    public void testJaNoJumpCF() throws Exception {
        runJumpTest("ja", true, false, false, false, 1L);
    }

    @Test
    public void testJaNoJumpZF() throws Exception {
        runJumpTest("ja", false, true, false, false, 1L);
    }

    @Test
    public void testJlJumps() throws Exception {
        runJumpTest("jl", false, false, true, false, 2L);
    }

    @Test
    public void testJlJumpsOF() throws Exception {
        runJumpTest("jl", false, false, false, true, 2L);
    }

    @Test
    public void testJlNoJump() throws Exception {
        runJumpTest("jl", false, false, true, true, 1L);
    }

    @Test
    public void testJgeJumps() throws Exception {
        runJumpTest("jge", false, false, true, true, 2L);
    }

    @Test
    public void testJgeNoJump() throws Exception {
        runJumpTest("jge", false, false, true, false, 1L);
    }

    @Test
    public void testJleJumps() throws Exception {
        runJumpTest("jle", false, true, false, false, 2L);
    }

    @Test
    public void testJleJumpsOnSF() throws Exception {
        runJumpTest("jle", false, false, true, false, 2L);
    }

    @Test
    public void testJleNoJump() throws Exception {
        runJumpTest("jle", false, false, true, true, 1L);
    }

    @Test
    public void testJgJumps() throws Exception {
        runJumpTest("jg", false, false, true, true, 2L);
    }

    @Test
    public void testJgNoJumpZF() throws Exception {
        runJumpTest("jg", false, true, true, true, 1L);
    }

    @Test
    public void testJgNoJumpSF() throws Exception {
        runJumpTest("jg", false, false, true, false, 1L);
    }
}
