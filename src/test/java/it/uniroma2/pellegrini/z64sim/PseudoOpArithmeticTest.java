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

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Arithmetic Pseudo-Operations")
public class PseudoOpArithmeticTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    private void runProgram(String progStr) throws ParseException, SimulatorException {
        Assembler a = new Assembler(new StringReader(progStr));
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(), "Syntax errors in assembly");
        Program program = a.getProgram();
        SimulatorController.loadProgram(program);
        stepUntilHlt(program);
    }

    private void stepUntilHlt(Program program) throws SimulatorException {
        for (int i = 0; i < 2000; i++) {
            long rip = SimulatorController.getCpuState().getRIP();
            MemoryElement element = program.getMemoryElementAt(rip);
            if (element == null) fail("RIP out of bounds or null element");
            Instruction instruction = (Instruction) element;
            SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
            instruction.run();
            if (instruction.getMnemonic().equals("hlt")) return;
        }
        fail("Program did not halt within 2000 steps");
    }

    @Test
    public void testMulBasic() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $3, %rax\n" +
                "    movq $7, %rcx\n" +
                "    mulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(21L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testMulLargeResult() throws Exception {
        // Use 0x10000 * 0x10000 = 0x100000000 which fits in rax (no rdx overflow)
        // Then test rdx by using 0x7FFFFFFF * 4 = 0x1FFFFFFFC (rdx=1)
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0x10000, %rax\n" +
                "    movq $0x10000, %rcx\n" +
                "    mulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(0x100000000L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testMulByZero() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $42, %rax\n" +
                "    movq $0, %rcx\n" +
                "    mulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testImulPositive() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $5, %rax\n" +
                "    movq $6, %rcx\n" +
                "    imulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(30L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testImulNegative() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $-3, %rax\n" +
                "    movq $4, %rcx\n" +
                "    imulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(-12L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(-1L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testImulBothNegative() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $-3, %rax\n" +
                "    movq $-4, %rcx\n" +
                "    imulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(12L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testDivBasic() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0, %rdx\n" +
                "    movq $20, %rax\n" +
                "    movq $6, %rcx\n" +
                "    divq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(2L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testDivExact() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0, %rdx\n" +
                "    movq $21, %rax\n" +
                "    movq $7, %rcx\n" +
                "    divq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testDivByZero() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0, %rdx\n" +
                "    movq $42, %rax\n" +
                "    movq $0, %rcx\n" +
                "    divq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(-1L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    public void testIdivBasic() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0, %rdx\n" +
                "    movq $20, %rax\n" +
                "    movq $6, %rcx\n" +
                "    idivq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(2L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testIdivNegativeDividend() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $-1, %rdx\n" +
                "    movq $-20, %rax\n" +
                "    movq $6, %rcx\n" +
                "    idivq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(-3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(-2L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    @Test
    public void testNoClobber() throws Exception {
        String prog = ".org 0x800\n" +
                ".data\n" +
                ".text\n" +
                "main:\n" +
                "    movq $0xDEAD, %rdi\n" +
                "    movq $0xBEEF, %rsi\n" +
                "    movq $0xCAFE, %rbx\n" +
                "    movq $0x1234, %r8\n" +
                "    movq $5, %rax\n" +
                "    movq $5, %rcx\n" +
                "    mulq %rcx\n" +
                "    hlt\n";
        runProgram(prog);
        assertEquals(0xDEADL, SimulatorController.getCpuState().getRegisterValue(Register.RDI));
        assertEquals(0xBEEFL, SimulatorController.getCpuState().getRegisterValue(Register.RSI));
        assertEquals(0xCAFEL, SimulatorController.getCpuState().getRegisterValue(Register.RBX));
        assertEquals(0x1234L, SimulatorController.getCpuState().getRegisterValue(Register.R8));
    }
}
