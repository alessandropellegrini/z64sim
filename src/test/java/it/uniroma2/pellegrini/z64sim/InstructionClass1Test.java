/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass1;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 1 — Data movement (mov, movsX, movzX, push, pop, pushf, popf)")
public class InstructionClass1Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    @Test
    @DisplayName("mov reg to reg")
    public void testMov() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RBX, 8);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x12345678L);

        new InstructionClass1("mov", src, dst, 0).run();

        assertEquals(0x12345678L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("movsX sign-extends negative byte to qword")
    public void testMovsXNegative() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RBX, 1);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x80L);

        new InstructionClass1("movsX", src, dst, 0).run();

        assertEquals(0xFFFFFFFFFFFFFF80L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("movsX sign-extends positive byte to qword (no extension)")
    public void testMovsXPositive() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RBX, 1);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x7FL);

        new InstructionClass1("movsX", src, dst, 0).run();

        assertEquals(0x7FL, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("movsX sign-extends negative word to qword")
    public void testMovsXWordNegative() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RBX, 2);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x8000L);

        new InstructionClass1("movsX", src, dst, 0).run();

        assertEquals(0xFFFFFFFFFFFF8000L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("movzX zero-extends byte to qword")
    public void testMovzX() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RBX, 1);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0xFFL);

        new InstructionClass1("movzX", src, dst, 0).run();

        assertEquals(0xFFL, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("movzX zero-extends word to qword")
    public void testMovzXWord() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RBX, 2);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0xFFFF_ABCDL);

        new InstructionClass1("movzX", src, dst, 0).run();

        assertEquals(0xABCDL, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
    }

    @Test
    @DisplayName("push/pop round-trip preserves value and restores RSP")
    public void testPushPop() throws ParseException, SimulatorException {
        Program program = new Program();
        program.textSectionStart(0x800);
        Memory.setProgram(program);

        SimulatorController.getCpuState().setRegisterValue(Register.RSP, 0x1000L);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0xAABBCCDDL);

        OperandRegister opPush = new OperandRegister(Register.RAX, 8);
        Instruction instPush = new InstructionClass1("push", opPush, null, 0);
        instPush.run();

        assertEquals(0x1000L - 8, SimulatorController.getCpuState().getRegisterValue(Register.RSP));

        // Clear RAX to verify pop restores it
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0L);

        OperandRegister opPop = new OperandRegister(Register.RAX, 8);
        Instruction instPop = new InstructionClass1("pop", opPop, null, 0);
        instPop.run();

        assertEquals(0xAABBCCDDL, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertEquals(0x1000L, SimulatorController.getCpuState().getRegisterValue(Register.RSP));
    }

    @Test
    @DisplayName("pushf/popf saves and restores flags")
    public void testPushfPopf() throws ParseException, SimulatorException {
        Program program = new Program();
        program.textSectionStart(0x800);
        Memory.setProgram(program);

        SimulatorController.getCpuState().setRegisterValue(Register.RSP, 0x1000L);
        SimulatorController.setCF(true);
        SimulatorController.setZF(true);

        Instruction instPushf = new InstructionClass1("pushf", null, null, 8);
        instPushf.run();

        // Change flags
        SimulatorController.setCF(false);
        SimulatorController.setZF(false);

        Instruction instPopf = new InstructionClass1("popf", null, null, 8);
        instPopf.run();

        // Flags should be restored
        assertTrue(SimulatorController.getCF());
        assertTrue(SimulatorController.getZF());
    }
}
