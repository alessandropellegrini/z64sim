/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass6;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 6 — Conditional jumps (jc, jnc, jz, jnz, js, jns, jo, jno, jp, jnp)")
public class InstructionClass6Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("jc jumps when CF=1, stays when CF=0")
    public void testJc() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);
        Instruction jc = new InstructionClass6("jc", target);

        SimulatorController.setCF(true);
        jc.run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());

        SimulatorController.getCpuState().setRIP(0x2000L);
        SimulatorController.setCF(false);
        jc.run();
        assertEquals(0x2000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jnc jumps when CF=0, stays when CF=1")
    public void testJnc() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);
        Instruction jnc = new InstructionClass6("jnc", target);

        SimulatorController.setCF(false);
        jnc.run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());

        SimulatorController.getCpuState().setRIP(0x2000L);
        SimulatorController.setCF(true);
        jnc.run();
        assertEquals(0x2000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jz jumps when ZF=1")
    public void testJz() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setZF(true);
        new InstructionClass6("jz", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jnz jumps when ZF=0")
    public void testJnz() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setZF(false);
        new InstructionClass6("jnz", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("js jumps when SF=1")
    public void testJs() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setSF(true);
        new InstructionClass6("js", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jns jumps when SF=0")
    public void testJns() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setSF(false);
        new InstructionClass6("jns", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jo jumps when OF=1")
    public void testJo() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setOF(true);
        new InstructionClass6("jo", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jno jumps when OF=0")
    public void testJno() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setOF(false);
        new InstructionClass6("jno", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jp jumps when PF=1")
    public void testJp() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setPF(true);
        new InstructionClass6("jp", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("jnp jumps when PF=0")
    public void testJnp() throws ParseException, SimulatorException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);

        SimulatorController.setPF(false);
        new InstructionClass6("jnp", target).run();
        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("getType and encode for Class 6 instructions")
    public void testGetTypeAndEncode() throws ParseException {
        String[] mnemonics = {"jc", "jp", "jz", "js", "jo", "jnc", "jnp", "jnz", "jns", "jno"};
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x200, -1);
        for (int i = 0; i < mnemonics.length; i++) {
            InstructionClass6 inst = new InstructionClass6(mnemonics[i], target);
            assertEquals(i, inst.getType());
            byte[] buf = inst.getValue();
            assertEquals(8, buf.length);
            assertEquals((byte) (0x60 | i), buf[0]);
            assertEquals((byte) 0x08, buf[1]); // SS=0, DS=0, DI=2, Mem=0
            assertEquals((byte) 0x00, buf[4]);
            assertEquals((byte) 0x02, buf[5]);
        }
    }
}
