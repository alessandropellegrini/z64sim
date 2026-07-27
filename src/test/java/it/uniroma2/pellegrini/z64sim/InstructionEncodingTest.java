/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.instructions.*;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Instruction Binary Encoding Tests")
public class InstructionEncodingTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    // =====================================================================
    // Helper: convert a hex string like "10 50 00 03 00 00 00 00" to byte[]
    // =====================================================================
    private static byte[] hexToBytes(String hex) {
        String[] parts = hex.trim().split("\\s+");
        byte[] bytes = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            bytes[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return bytes;
    }

    // =====================================================================
    // Class 0: hlt, nop, int
    // =====================================================================

    @Test
    @DisplayName("hlt encoding: opcode 0x01, rest zeros")
    public void testHltEncoding() throws ParseException {
        Instruction insn = new InstructionClass0("hlt", null);
        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("01 00 00 00 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("nop encoding: opcode 0x02, rest zeros")
    public void testNopEncoding() throws ParseException {
        Instruction insn = new InstructionClass0("nop", null);
        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("02 00 00 00 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("int $0x80 encoding: opcode 0x03, vector at byte 7")
    public void testIntEncoding() throws ParseException {
        OperandImmediate ivn = new OperandImmediate(0x80);
        Instruction insn = new InstructionClass0("int", ivn);
        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("03 00 00 00 00 00 00 80");
        assertArrayEquals(expected, encoded);
    }

    // =====================================================================
    // Class 1: mov
    // =====================================================================

    @Test
    @DisplayName("movw %ax, %bx → 10 50 00 03 00 00 00 00")
    public void testMovRegReg() throws ParseException {
        // movw %ax, %bx: word size, reg-to-reg, rax=0 → rbx=3
        OperandRegister src = new OperandRegister(Register.RAX, 2); // %ax
        OperandRegister dst = new OperandRegister(Register.RBX, 2); // %bx
        Instruction insn = new InstructionClass1("mov", src, dst, -1);

        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("10 50 00 03 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("movq %rax, %rcx → 10 f0 00 01 00 00 00 00")
    public void testMovqRegReg() throws ParseException {
        // From encoding.tex: movq %rax, %rcx
        OperandRegister src = new OperandRegister(Register.RAX, 8); // %rax
        OperandRegister dst = new OperandRegister(Register.RCX, 8); // %rcx
        Instruction insn = new InstructionClass1("mov", src, dst, -1);

        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("10 f0 00 01 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("movb $1, (%rax) — Issue #13 fix: 8 bytes, DI=01, imm in Short Imm")
    public void testMovImmToMem_Issue13() throws ParseException {
        // movb $1, (%rax): immediate=1, dest=memory with base rax, no displacement
        // This should be 8 bytes (NOT 16 as the old buggy code produced)
        OperandImmediate src = new OperandImmediate(1);
        src.setSize(1);
        OperandMemory dst = new OperandMemory(Register.RAX, 1, -1, -1, 0, 1);

        Instruction insn = new InstructionClass1("mov", src, dst, -1);
        assertEquals(8, insn.getSize(), "movb $1, (%rax) should be 8 bytes, not 16");

        byte[] encoded = insn.getValue();
        // Opcode: 10 (class 1, type 0)
        // Mode: 00 00 01 01 = 0x05 (SS=00 byte, DS=00 byte, DI=01 imm, Mem=01 dst-mem)
        // SIB: 1000 0000 = 0x80 (Bp=1, Ip=0)
        // R/M: 0000 0000 = 0x00 (src=dc, dst(base)=rax=0)
        // Short Imm: 01 00 00 00
        byte[] expected = hexToBytes("10 05 80 00 01 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("movq 0xabcd(%rsi, %rcx, 4), %rax → 10 fa e1 60 cd ab 00 00")
    public void testMovMemToReg_FullSIB() throws ParseException {
        // Source: memory with base=rsi(6), index=rcx(1), scale=4, displacement=0xabcd
        OperandMemory src = new OperandMemory(Register.RSI, 8, Register.RCX, 4, 0xabcd, 8);
        OperandRegister dst = new OperandRegister(Register.RAX, 8); // %rax

        Instruction insn = new InstructionClass1("mov", src, dst, -1);
        byte[] encoded = insn.getValue();
        // Opcode: 10
        // Mode: SS=11(q), DS=11(q), DI=10(disp), Mem=10(src mem) = 1111 1010 = 0xFA
        // SIB: Bp=1, Ip=1, Scale=10(4), Index=0001(rcx) = 1110 0001 = 0xE1
        // R/M: Src(base)=0110(rsi), Dst=0000(rax) = 0110 0000 = 0x60
        // Disp: 0xabcd LE = cd ab 00 00
        byte[] expected = hexToBytes("10 fa e1 60 cd ab 00 00");
        assertArrayEquals(expected, encoded);
    }

    // =====================================================================
    // Class 2: add, sub
    // =====================================================================

    @Test
    @DisplayName("addq %rax, %rcx → 20 f0 00 01 00 00 00 00")
    public void testAddqRegReg() throws ParseException {
        OperandRegister src = new OperandRegister(Register.RAX, 8);
        OperandRegister dst = new OperandRegister(Register.RCX, 8);
        Instruction insn = new InstructionClass2("add", src, dst);

        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("20 f0 00 01 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("addb $0xa, %dl → 20 04 00 02 0a 00 00 00")
    public void testAddbImmToReg() throws ParseException {
        OperandImmediate src = new OperandImmediate(0xa);
        src.setSize(1);
        OperandRegister dst = new OperandRegister(Register.RDX, 1); // %dl

        Instruction insn = new InstructionClass2("add", src, dst);
        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("20 04 00 02 0a 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("subb $0x2, 0xaaaa → 21 0d 00 00 aa aa 00 00 02 00 00 00 00 00 00 00")
    public void testSubbImmToDisp() throws ParseException {
        // Source: immediate $0x2, Dest: memory at displacement 0xaaaa (no base/index)
        OperandImmediate src = new OperandImmediate(0x2);
        src.setSize(1);
        OperandMemory dst = new OperandMemory(-1, -1, -1, -1, 0xaaaa, 1);

        Instruction insn = new InstructionClass2("sub", src, dst);
        assertEquals(16, insn.getSize(), "subb $0x2, 0xaaaa should be 16 bytes (DI=11)");

        byte[] encoded = insn.getValue();
        // Opcode: 21 (class 2, type 1 = sub)
        // Mode: SS=00(b), DS=00(b), DI=11(disp+imm), Mem=01(dst-mem) = 0000 1101 = 0x0D
        // SIB: 00 (no base, no index)
        // R/M: 00 (no registers)
        // Disp: aa aa 00 00
        // Imm: 02 00 00 00 00 00 00 00
        byte[] expected = hexToBytes("21 0d 00 00 aa aa 00 00 02 00 00 00 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    // =====================================================================
    // Class 4: flag manipulation
    // =====================================================================

    @Test
    @DisplayName("clc encoding: opcode 0x40, rest zeros")
    public void testClcEncoding() {
        Instruction insn = new InstructionClass4("clc");
        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("40 00 00 00 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    @Test
    @DisplayName("sti encoding: opcode 0x4b (class 4, type 11)")
    public void testStiEncoding() {
        Instruction insn = new InstructionClass4("sti");
        byte[] encoded = insn.getValue();
        byte[] expected = hexToBytes("4b 00 00 00 00 00 00 00");
        assertArrayEquals(expected, encoded);
    }

    // =====================================================================
    // Issue #13 regression tests
    // =====================================================================

    @Test
    @DisplayName("Issue #13: immediate + memory without displacement → 8 bytes")
    public void testIssue13_ImmToMemNoDisp_Is8Bytes() throws ParseException {
        // addl $100, (%rbx): immediate 100, dest memory base=rbx, no displacement
        OperandImmediate src = new OperandImmediate(100);
        src.setSize(4);
        OperandMemory dst = new OperandMemory(Register.RBX, 4, -1, -1, 0, 4);

        Instruction insn = new InstructionClass2("add", src, dst);
        assertEquals(8, insn.getSize(), "add $100, (%%rbx) should be 8 bytes when no displacement");
    }

    @Test
    @DisplayName("Issue #13: immediate + memory WITH displacement → 16 bytes")
    public void testIssue13_ImmToMemWithDisp_Is16Bytes() throws ParseException {
        // addl $100, 0x10(%rbx): immediate + displacement
        OperandImmediate src = new OperandImmediate(100);
        src.setSize(4);
        OperandMemory dst = new OperandMemory(Register.RBX, 4, -1, -1, 0x10, 4);

        Instruction insn = new InstructionClass2("add", src, dst);
        assertEquals(16, insn.getSize(), "add $100, 0x10(%%rbx) should be 16 bytes (DI=11)");
    }

    @Test
    @DisplayName("Issue #13: 64-bit immediate to register → 16 bytes")
    public void testIssue13_LargeImm_Is16Bytes() throws ParseException {
        // movq $0x100000000, %rax: 64-bit immediate
        OperandImmediate src = new OperandImmediate(0x100000000L);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);

        Instruction insn = new InstructionClass1("mov", src, dst, -1);
        assertEquals(16, insn.getSize(), "movq $0x100000000, %%rax should be 16 bytes");
    }

    // =====================================================================
    // getType() tests
    // =====================================================================

    @Test
    @DisplayName("getType() for all Class 0 instructions")
    public void testClass0Types() throws ParseException {
        assertEquals(1, new InstructionClass0("hlt", null).getType());
        assertEquals(2, new InstructionClass0("nop", null).getType());
        assertEquals(3, new InstructionClass0("int", new OperandImmediate(0)).getType());
    }

    @Test
    @DisplayName("getType() for Class 2 instructions")
    public void testClass2Types() throws ParseException {
        OperandRegister r = new OperandRegister(Register.RAX, 8);
        assertEquals(0, new InstructionClass2("add", r, r).getType());
        assertEquals(1, new InstructionClass2("sub", r, r).getType());
        assertEquals(9, new InstructionClass2("xor", r, r).getType());
    }
}
