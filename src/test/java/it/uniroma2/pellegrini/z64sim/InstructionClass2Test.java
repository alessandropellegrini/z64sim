/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass2;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class 2 — Arithmetic/Logic (add, sub, adc, sbb, cmp, test, neg, and, or, xor, not, bt)")
public class InstructionClass2Test {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    // --- ADD ---

    @Test
    @DisplayName("add $5 to rax=10 --> result=15, no flags")
    public void testAdd() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(5);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 10L);

        new InstructionClass2("add", src, dst).run();

        assertEquals(15L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertFalse(SimulatorController.getZF());
        assertFalse(SimulatorController.getSF());
        assertFalse(SimulatorController.getCF());
        assertFalse(SimulatorController.getOF());
    }

    @Test
    @DisplayName("add reg to reg: al=0xFF + bl=1 --> overflow byte, CF=1, ZF=1")
    public void testAddOverflow() throws ParseException, SimulatorException {
        // Use register-to-register so the flag computation uses 1-byte size
        OperandRegister src = new OperandRegister(Register.RBX, 1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 1L);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0xFFL);

        new InstructionClass2("add", src, dst).run();

        assertEquals(0x00L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
        assertTrue(SimulatorController.getZF());
    }

    @Test
    @DisplayName("add reg to reg: al=0x7F + bl=0x01 --> signed overflow, OF=1, SF=1")
    public void testAddSignedOverflow() throws ParseException, SimulatorException {
        // Two positive values that overflow to negative: 0x7F + 0x01 = 0x80 (-128)
        OperandRegister src = new OperandRegister(Register.RBX, 1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x01L);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x7FL);

        new InstructionClass2("add", src, dst).run();

        assertEquals(0x80L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
        assertTrue(SimulatorController.getOF());
        assertTrue(SimulatorController.getSF());
    }

    @Test
    @DisplayName("add works with 16-bit operands")
    public void testAdd16bit() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(0x1234);
        OperandRegister dst = new OperandRegister(Register.RAX, 2);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x5678L);

        new InstructionClass2("add", src, dst).run();

        assertEquals(0x68ACL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFFFL);
    }

    @Test
    @DisplayName("add works with 32-bit operands")
    public void testAdd32bit() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(0x10000000);
        OperandRegister dst = new OperandRegister(Register.RAX, 4);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x20000000L);

        new InstructionClass2("add", src, dst).run();

        assertEquals(0x30000000L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFFFFFFFL);
    }

    // --- SUB ---

    @Test
    @DisplayName("sub $3 from rax=10 --> result=7")
    public void testSub() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(3);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 10L);

        new InstructionClass2("sub", src, dst).run();

        assertEquals(7L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertFalse(SimulatorController.getZF());
        assertFalse(SimulatorController.getCF());
    }

    @Test
    @DisplayName("sub $1 from al=0 --> borrow, CF=1")
    public void testSubBorrow() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0L);

        new InstructionClass2("sub", src, dst).run();

        assertEquals(0xFFL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
    }

    // --- ADC ---

    @Test
    @DisplayName("adc $1, %al with al=2, CF=1 --> result=4")
    public void testAdcWithCarry() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 2L);
        SimulatorController.setCF(true);

        new InstructionClass2("adc", src, dst).run();

        assertEquals(4L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
    }

    @Test
    @DisplayName("adc $1, %al with al=2, CF=0 --> result=3")
    public void testAdcNoCarry() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 2L);
        SimulatorController.setCF(false);

        new InstructionClass2("adc", src, dst).run();

        assertEquals(3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
    }

    // --- SBB ---

    @Test
    @DisplayName("sbb $1, %al with al=5, CF=0 --> result=3 (CF inverted adds 1)")
    public void testSbbNoCarry() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);
        SimulatorController.setCF(false);

        new InstructionClass2("sbb", src, dst).run();

        // sbb: srcValue += CF ? 0 : 1 --> srcValue = 1+1=2, result = 5-2=3
        assertEquals(3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
    }

    @Test
    @DisplayName("sbb $1, %al with al=5, CF=1 --> result=4")
    public void testSbbWithCarry() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(1);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);
        SimulatorController.setCF(true);

        new InstructionClass2("sbb", src, dst).run();

        // sbb: srcValue += CF ? 0 : 1 --> srcValue = 1+0=1, result = 5-1=4
        assertEquals(4L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
    }

    // --- CMP ---

    @Test
    @DisplayName("cmp $5, %rax with rax=5 --> ZF=1, CF=0, destination unchanged")
    public void testCmpEqual() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(5);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);

        new InstructionClass2("cmp", src, dst).run();

        assertEquals(5L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertTrue(SimulatorController.getZF());
        assertFalse(SimulatorController.getCF());
    }

    @Test
    @DisplayName("cmp $10, %rax with rax=5 --> CF=1 (5 < 10)")
    public void testCmpLess() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(10);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);

        new InstructionClass2("cmp", src, dst).run();

        assertTrue(SimulatorController.getCF());
        assertFalse(SimulatorController.getZF());
        assertTrue(SimulatorController.getSF());
    }

    // --- TEST ---

    @Test
    @DisplayName("test $0xFF, %al with al=0 --> ZF=1, CF=0, OF=0")
    public void testTestZero() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(0xFF);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0L);

        new InstructionClass2("test", src, dst).run();

        assertTrue(SimulatorController.getZF());
        assertFalse(SimulatorController.getCF());
        assertFalse(SimulatorController.getOF());
    }

    @Test
    @DisplayName("test $0x0F, %al with al=0xF0 --> ZF=1 (AND=0)")
    public void testTestNonOverlapping() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(0x0F);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0xF0L);

        new InstructionClass2("test", src, dst).run();

        assertTrue(SimulatorController.getZF());
    }

    // --- NEG ---

    @Test
    @DisplayName("neg al=5 --> result=0xFB (-5 byte), CF=1")
    public void testNeg() throws ParseException, SimulatorException {
        OperandRegister op = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);

        // neg takes (source, destination) — operates on source
        new InstructionClass2("neg", op, null).run();

        assertEquals(0xFBL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("neg al=0 --> result=0, CF=0")
    public void testNegZero() throws ParseException, SimulatorException {
        OperandRegister op = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0L);

        new InstructionClass2("neg", op, null).run();

        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
        assertTrue(SimulatorController.getZF());
    }

    // --- AND ---

    @Test
    @DisplayName("and $0x0F, %al with al=0xFF --> result=0x0F, CF=0, OF=0")
    public void testAnd() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(0x0F);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0xFFL);

        new InstructionClass2("and", src, dst).run();

        assertEquals(0x0FL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
        assertFalse(SimulatorController.getOF());
    }

    // --- OR ---

    @Test
    @DisplayName("or $0xF0, %al with al=0x0F --> result=0xFF")
    public void testOr() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(0xF0);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x0FL);

        new InstructionClass2("or", src, dst).run();

        assertEquals(0xFFL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        assertFalse(SimulatorController.getCF());
        assertFalse(SimulatorController.getOF());
    }

    // --- XOR ---

    @Test
    @DisplayName("xor %rax, %rax --> result=0, ZF=1")
    public void testXor() throws ParseException, SimulatorException {
        OperandRegister src = new OperandRegister(Register.RAX, 8);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0xABCDL);

        new InstructionClass2("xor", src, dst).run();

        assertEquals(0L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        assertTrue(SimulatorController.getZF());
    }

    // --- NOT ---

    @Test
    @DisplayName("not al=0x55 --> result=0xAA, no flag change")
    public void testNot() throws ParseException, SimulatorException {
        OperandRegister op = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x55L);

        // not takes (source, destination) — operates on source
        new InstructionClass2("not", op, null).run();

        assertEquals(0xAAL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
    }

    // --- BT ---

    @Test
    @DisplayName("bt $3, %al with al=0x08 --> CF=1 (bit 3 set)")
    public void testBtTrue() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(3);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x08L);

        new InstructionClass2("bt", src, dst).run();

        assertTrue(SimulatorController.getCF());
    }

    @Test
    @DisplayName("bt $3, %al with al=0x00 --> CF=0 (bit 3 clear)")
    public void testBtFalse() throws ParseException, SimulatorException {
        OperandImmediate src = new OperandImmediate(3);
        OperandRegister dst = new OperandRegister(Register.RAX, 1);
        SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x00L);

        new InstructionClass2("bt", src, dst).run();

        assertFalse(SimulatorController.getCF());
    }
}
