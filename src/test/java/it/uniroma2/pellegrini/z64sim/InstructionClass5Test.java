/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass5;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 5 — Control flow (jmp, call, ret, iret)")
public class InstructionClass5Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("jmp sets RIP to target address")
    public void testJmp() throws SimulatorException {
        // size=-1 means getOperandValue returns the address directly
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);
        new InstructionClass5("jmp", target).run();

        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
    }

    @Test
    @DisplayName("call pushes return address and jumps; ret pops and returns")
    public void testCallRet() throws ParseException, SimulatorException {
        Program program = new Program();
        program.textSectionStart(0x800);
        Memory.setProgram(program);

        SimulatorController.getCpuState().setRegisterValue(Register.RSP, 0x2000L);
        SimulatorController.getCpuState().setRIP(0x1500L);

        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);
        new InstructionClass5("call", target).run();

        assertEquals(0x1000L, SimulatorController.getCpuState().getRIP());
        assertEquals(0x2000L - 8, SimulatorController.getCpuState().getRegisterValue(Register.RSP));

        // ret should pop the return address and restore RIP
        new InstructionClass5("ret", null).run();

        assertEquals(0x1500L, SimulatorController.getCpuState().getRIP());
        assertEquals(0x2000L, SimulatorController.getCpuState().getRegisterValue(Register.RSP));
    }

    @Test
    @DisplayName("iret throws UnsupportedOperationException")
    public void testIret() {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x1000, -1);
        Instruction inst = new InstructionClass5("iret", target);
        assertThrows(UnsupportedOperationException.class, inst::run);
    }
}
