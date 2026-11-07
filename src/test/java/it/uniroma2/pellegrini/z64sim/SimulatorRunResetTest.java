/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.CpuState;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class SimulatorRunResetTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("CpuState.reset clears all registers, resets flags to 2, and clears halted")
    public void testCpuStateReset() {
        CpuState cpu = SimulatorController.getCpuState();
        cpu.setRAX(0x1111L);
        cpu.setRCX(0x2222L);
        cpu.setRDX(0x3333L);
        cpu.setRBX(0x4444L);
        cpu.setRSP(0x5555L);
        cpu.setRBP(0x6666L);
        cpu.setRSI(0x7777L);
        cpu.setRDI(0x8888L);
        cpu.setR8(0x9999L);
        cpu.setR9(0xAAAAL);
        cpu.setR10(0xBBBBL);
        cpu.setR11(0xCCCCL);
        cpu.setR12(0xDDDDL);
        cpu.setR13(0xEEEEL);
        cpu.setR14(0xFFFFL);
        cpu.setR15(0x1234L);
        cpu.setRIP(0x800L);
        cpu.setFlags(0xFFL);
        cpu.setHalted(true);

        cpu.reset();

        for (int r = Register.RAX; r <= Register.R15; r++) {
            assertEquals(0L, cpu.getRegisterValue(r), "Register " + r + " should be reset to 0");
        }
        assertEquals(0L, cpu.getRIP(), "RIP should be reset to 0");
        assertEquals(2L, cpu.getFlags(), "FLAGS should be reset to 2");
        assertFalse(cpu.isHalted(), "Halted should be reset to false");
    }

    @Test
    @DisplayName("SimulatorController.resetCpuState restores RIP to _start and RSP to top of memory")
    public void testSimulatorControllerResetCpuState() throws ParseException {
        String code = ".org 0x800\n.text\nmain:\n  movq $42, %rax\n  hlt\n";
        Assembler a = new Assembler(new StringReader(code));
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty());
        Program p = a.getProgram();

        SimulatorController.loadProgram(p);
        CpuState cpu = SimulatorController.getCpuState();
        long initialRip = p._start.getTarget();
        long initialRsp = p.getLargestAddress();

        assertEquals(initialRip, cpu.getRIP());
        assertEquals(initialRsp, cpu.getRSP());

        // Step through instructions
        SimulatorController.step();
        assertEquals(42L, cpu.getRAX());
        SimulatorController.step();
        assertTrue(cpu.isHalted());

        // Reset CPU state
        SimulatorController.resetCpuState();

        assertEquals(0L, cpu.getRAX());
        assertEquals(initialRip, cpu.getRIP());
        assertEquals(initialRsp, cpu.getRSP());
        assertEquals(2L, cpu.getFlags());
        assertFalse(cpu.isHalted());
        assertFalse(SimulatorController.getInstance().halted);
    }

    @Test
    @DisplayName("SimulatorController.run resets CPU state before starting execution")
    public void testRunResetsCpuState() throws Exception {
        String code = ".org 0x800\n.text\nmain:\n  addq $1, %rax\n  hlt\n";
        Assembler a = new Assembler(new StringReader(code));
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty());
        Program p = a.getProgram();

        SimulatorController.loadProgram(p);
        CpuState cpu = SimulatorController.getCpuState();
        long initialRip = p._start.getTarget();

        // Run until hlt
        SimulatorController.run();

        // Wait for execution to halt
        long deadline = System.currentTimeMillis() + 2000;
        while (!SimulatorController.getInstance().halted && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(SimulatorController.getInstance().halted);
        assertEquals(1L, cpu.getRAX());
        assertNotEquals(initialRip, cpu.getRIP());

        // Press run again: state should reset and run again
        SimulatorController.run();

        // Immediately after run() call, RIP should be reset or execution restarted
        deadline = System.currentTimeMillis() + 2000;
        while (!SimulatorController.getInstance().halted && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(SimulatorController.getInstance().halted);
        // RAX was reset to 0, then incremented to 1 again (not 2!)
        assertEquals(1L, cpu.getRAX(), "RAX should be 1 after second run because CPU state was reset");

        SimulatorController.stop();
    }
}
