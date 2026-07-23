/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass3;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 3 — Shift/Rotate (shl/sal, shr, sar, rol, ror, rcl, rcr)")
public class InstructionClass3Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    // --- SHL / SAL ---

    @Test
    @DisplayName("shl $1, %dl with dl=0x01 → result=0x02, CF=0")
    public void testShlNoCarry() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x01L);

        new InstructionClass3("shl", 1, reg).run();

        assertEquals(0x02L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
    }

    @Test
    @DisplayName("shl $1, %dl with dl=0x80 → result=0x00, CF=1")
    public void testShlCarry() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x80L);

        new InstructionClass3("shl", 1, reg).run();

        assertEquals(0x00L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("sal is an alias for shl")
    public void testSal() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x40L);

        new InstructionClass3("sal", 1, reg).run();

        assertEquals(0x80L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
    }

    @Test
    @DisplayName("shl $4, %dl with dl=0x0F → result=0xF0")
    public void testMultiBitShift() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x0FL);

        new InstructionClass3("shl", 4, reg).run();

        assertEquals(0xF0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
    }

    // --- SHR ---

    @Test
    @DisplayName("shr $1, %dl with dl=0x01 → result=0x00, CF=1")
    public void testShrCarry() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x01L);

        new InstructionClass3("shr", 1, reg).run();

        assertEquals(0x00L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("shr $1, %dl with dl=0x80 → result=0x40, CF=0")
    public void testShrNoCarry() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x80L);

        new InstructionClass3("shr", 1, reg).run();

        assertEquals(0x40L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
    }

    // --- SAR ---

    @Test
    @DisplayName("sar $1, %rdx with rdx=0x8000000000000000 → sign preserved (0xC...0)")
    public void testSarSignPreserved() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x8000000000000000L);

        new InstructionClass3("sar", 1, reg).run();

        assertEquals(0xC000000000000000L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
    }

    // --- ROL ---

    @Test
    @DisplayName("rol $1, %dl with dl=0x80 → result=0x01 (MSB rotated to LSB)")
    public void testRol() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x80L);

        new InstructionClass3("rol", 1, reg).run();

        assertEquals(0x01L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
    }

    // --- ROR ---

    @Test
    @DisplayName("ror $1, %dl with dl=0x01 → result=0x80 (LSB rotated to MSB)")
    public void testRor() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x01L);

        new InstructionClass3("ror", 1, reg).run();

        assertEquals(0x80L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
    }

    // --- RCL ---

    @Test
    @DisplayName("rcl $1, %dl with dl=0x80, CF=0 → result=0x00, CF=1")
    public void testRclCarryOut() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x80L);
        SimulatorController.setCF(false);

        new InstructionClass3("rcl", 1, reg).run();

        assertEquals(0x00L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("rcl $1, %dl with dl=0x00, CF=1 → result=0x01, CF=0")
    public void testRclCarryIn() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x00L);
        SimulatorController.setCF(true);

        new InstructionClass3("rcl", 1, reg).run();

        assertEquals(0x01L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
    }

    // --- RCR ---

    @Test
    @DisplayName("rcr $1, %dl with dl=0x01, CF=0 → result=0x00, CF=1")
    public void testRcrCarryOut() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x01L);
        SimulatorController.setCF(false);

        new InstructionClass3("rcr", 1, reg).run();

        assertEquals(0x00L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("rcr $1, %dl with dl=0x00, CF=1 → result=0x80, CF=0")
    public void testRcrCarryIn() throws SimulatorException {
        OperandRegister reg = new OperandRegister(Register.RDX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x00L);
        SimulatorController.setCF(true);

        new InstructionClass3("rcr", 1, reg).run();

        assertEquals(0x80L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
    }
}
