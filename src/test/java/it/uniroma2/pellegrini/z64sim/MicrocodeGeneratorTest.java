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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Microcode Generator Tests")
public class MicrocodeGeneratorTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    // =====================================================================
    // Class 0
    // =====================================================================

    @Test
    @DisplayName("hlt: fetch + halt state = 4 lines")
    public void testHlt() throws ParseException {
        Instruction insn = new InstructionClass0("hlt", null);
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertEquals(4, ops.size());
        assertEquals("MAR \u2190 RIP", ops.get(0));
        assertEquals("Enter HALT state", ops.get(3));
    }

    @Test
    @DisplayName("nop: fetch only = 3 lines")
    public void testNop() throws ParseException {
        Instruction insn = new InstructionClass0("nop", null);
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertEquals(3, ops.size());
        assertEquals("IR \u2190 MDR", ops.get(2));
    }

    @Test
    @DisplayName("int: fetch + clear IF, push RIP, push FLAGS, IACK, IVT lookup")
    public void testInt() throws ParseException {
        OperandImmediate ivn = new OperandImmediate(0x80);
        Instruction insn = new InstructionClass0("int", ivn);
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.size() > 3, "int should have more than fetch lines");
        // IF cleared first (combined with TEMP1 ← RSP)
        assertEquals("FLAGS[I] \u2190 0; TEMP1 \u2190 RSP", ops.get(3), "int should clear IF first");
        // RIP pushed before FLAGS
        int ripPushIdx = ops.indexOf("MDR \u2190 RIP");
        int flagsPushIdx = ops.indexOf("MDR \u2190 FLAGS");
        assertTrue(ripPushIdx < flagsPushIdx, "int should push RIP before FLAGS");
        // RSP decremented via ALU_OUT[SUB]
        assertTrue(ops.contains("RSP \u2190 ALU_OUT[SUB]"), "int should use ALU_OUT[SUB] for RSP");
        // IACK bus cycle
        assertTrue(ops.contains("IACK"), "int should have IACK bus cycle");
        // IVT lookup via SHIFTER_OUT
        assertTrue(ops.contains("MAR \u2190 SHIFTER_OUT[SX, 3]"), "int should use SHIFTER_OUT[SX, 3] for IVT");
        assertTrue(ops.contains("RIP \u2190 MDR"), "int should set RIP from IVT");
    }

    // =====================================================================
    // Class 1
    // =====================================================================

    @Test
    @DisplayName("movq %rax, %rcx: fetch + reg-to-reg = 5 lines")
    public void testMovRegReg() throws ParseException {
        OperandRegister src = new OperandRegister(Register.RAX, 8);
        OperandRegister dst = new OperandRegister(Register.RCX, 8);
        Instruction insn = new InstructionClass1("mov", src, dst, -1);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertEquals(5, ops.size());
        assertEquals("TEMP2 \u2190 RAX", ops.get(3));
        assertEquals("RCX \u2190 SHIFTER_OUT[SHL, 0]", ops.get(4));
    }

    @Test
    @DisplayName("movq mem-to-reg with full SIB: has address calc + memory read")
    public void testMovMemToReg() throws ParseException {
        OperandMemory src = new OperandMemory(Register.RSI, 8, Register.RCX, 4, 0xabcd, 8);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        Instruction insn = new InstructionClass1("mov", src, dst, -1);

        List<String> ops = MicrocodeGenerator.generate(insn);
        // Should have: fetch(3) + SIB calc(7) + MDR←(MAR)(1) + RAX←MDR(1) = 12
        assertTrue(ops.size() >= 10, "Full SIB mov should have many µ-ops");
        assertTrue(ops.contains("TEMP2 \u2190 RCX"), "Should reference index register RCX");
        assertTrue(ops.contains("TEMP2 \u2190 RSI"), "Should reference base register RSI");
        assertTrue(ops.contains("RAX \u2190 MDR"), "Should transfer to dest register");
    }

    @Test
    @DisplayName("lea: address calc + MAR to register, no memory read")
    public void testLea() throws ParseException {
        OperandMemory src = new OperandMemory(Register.RBX, 8, -1, -1, 0x10, 8);
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        Instruction insn = new InstructionClass1("lea", src, dst, -1);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("RAX \u2190 MAR"), "LEA should put MAR into dest register");
        assertFalse(ops.contains("MDR \u2190 (MAR)"), "LEA should NOT read memory");
    }

    @Test
    @DisplayName("pushf: fetch + stack push FLAGS")
    public void testPushf() throws ParseException {
        Instruction insn = new InstructionClass1("pushf", null, null, -1);
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("MDR \u2190 FLAGS"), "pushf should read FLAGS into MDR");
        assertTrue(ops.contains("RSP \u2190 ALU_OUT[SUB]"), "pushf should decrement RSP via ALU");
    }

    // =====================================================================
    // Class 2
    // =====================================================================

    @Test
    @DisplayName("addq %rax, %rcx: fetch + ALU ADD")
    public void testAddRegReg() throws ParseException {
        OperandRegister src = new OperandRegister(Register.RAX, 8);
        OperandRegister dst = new OperandRegister(Register.RCX, 8);
        Instruction insn = new InstructionClass2("add", src, dst);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("RCX \u2190 ALU_OUT[ADD]"), "add should write ALU_OUT[ADD] to dest");
    }

    @Test
    @DisplayName("cmp does NOT write result back")
    public void testCmpDoesNotWriteBack() throws ParseException {
        OperandRegister src = new OperandRegister(Register.RAX, 8);
        OperandRegister dst = new OperandRegister(Register.RCX, 8);
        Instruction insn = new InstructionClass2("cmp", src, dst);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("ALU_OUT[SUB]"), "cmp should invoke ALU_OUT[SUB]");
        // Verify no result writeback
        for (String op : ops) {
            if (op.contains("ALU_OUT[SUB]")) {
                assertFalse(op.contains("\u2190"), "cmp should NOT write ALU result: " + op);
            }
        }
    }

    @Test
    @DisplayName("test does NOT write result back")
    public void testTestDoesNotWriteBack() throws ParseException {
        OperandRegister src = new OperandRegister(Register.RAX, 8);
        OperandRegister dst = new OperandRegister(Register.RCX, 8);
        Instruction insn = new InstructionClass2("test", src, dst);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("ALU_OUT[AND]"), "test should invoke ALU_OUT[AND]");
        for (String op : ops) {
            if (op.contains("ALU_OUT[AND]")) {
                assertFalse(op.contains("\u2190"), "test should NOT write ALU result: " + op);
            }
        }
    }

    @Test
    @DisplayName("neg: unary operation on register")
    public void testNeg() throws ParseException {
        OperandRegister dst = new OperandRegister(Register.RAX, 8);
        Instruction insn = new InstructionClass2("neg", null, dst);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("TEMP2 \u2190 RAX"), "neg should load operand into TEMP2");
        assertTrue(ops.contains("RAX \u2190 ALU_OUT[NEG]"), "neg should write ALU_OUT[NEG] to dest");
    }

    // =====================================================================
    // Class 3
    // =====================================================================

    @Test
    @DisplayName("shl: shift left operation")
    public void testShl() throws ParseException {
        OperandRegister reg = new OperandRegister(Register.RAX, 8);
        Instruction insn = new InstructionClass3("shl", 4, reg);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertEquals(5, ops.size()); // fetch(3) + TEMP2←RAX + RAX←SHIFTER_OUT
        assertTrue(ops.contains("TEMP2 \u2190 RAX"));
        assertTrue(ops.contains("RAX \u2190 SHIFTER_OUT[SHL, 4]"));
    }

    // =====================================================================
    // Class 4
    // =====================================================================

    @Test
    @DisplayName("clc: fetch + clear carry flag = 4 lines")
    public void testClc() {
        Instruction insn = new InstructionClass4("clc");
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertEquals(4, ops.size());
        assertEquals("FLAGS[CF] \u2190 0", ops.get(3));
    }

    @Test
    @DisplayName("sti: fetch + set interrupt flag")
    public void testSti() {
        Instruction insn = new InstructionClass4("sti");
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertEquals(4, ops.size());
        assertEquals("FLAGS[IF] \u2190 1", ops.get(3));
    }

    // =====================================================================
    // Class 5
    // =====================================================================

    @Test
    @DisplayName("ret: fetch + pop RIP from stack")
    public void testRet() throws ParseException {
        Instruction insn = new InstructionClass5("ret", null);
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("MAR \u2190 RSP"), "ret should set MAR to RSP");
        assertTrue(ops.contains("MDR \u2190 (MAR)"), "ret should read from stack (memory read, not MAR copy)");
        assertTrue(ops.contains("RIP \u2190 MDR"), "ret should set RIP from MDR");
        assertTrue(ops.contains("RSP \u2190 ALU_OUT[ADD]"), "ret should increment RSP via ALU");
    }

    @Test
    @DisplayName("iret: fetch + pop FLAGS + pop RIP + re-enable interrupts")
    public void testIret() throws ParseException {
        Instruction insn = new InstructionClass5("iret", null);
        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("FLAGS \u2190 MDR"), "iret should restore FLAGS");
        assertTrue(ops.contains("RIP \u2190 MDR"), "iret should restore RIP");
        // FLAGS is popped first (top of stack — pushed last by int)
        int flagsIdx = ops.indexOf("FLAGS \u2190 MDR");
        int ripIdx = ops.indexOf("RIP \u2190 MDR");
        assertTrue(flagsIdx < ripIdx, "iret should pop FLAGS before RIP");
        // RSP incremented via ALU_OUT[ADD]
        long rspIncs = ops.stream().filter(s -> s.equals("RSP \u2190 ALU_OUT[ADD]")).count();
        assertEquals(2, rspIncs, "iret should increment RSP twice via ALU_OUT[ADD]");
        // Re-enable interrupts at the end
        assertEquals("FLAGS[I] \u2190 1", ops.get(ops.size() - 1), "iret should re-enable interrupts");
    }

    // =====================================================================
    // Class 6
    // =====================================================================

    @Test
    @DisplayName("jz: conditional jump on zero flag")
    public void testJz() throws ParseException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x100, 8);
        Instruction insn = new InstructionClass6("jz", target);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("if FLAGS[ZF] == 1"), "jz should check ZF == 1");
        assertTrue(ops.contains("    RIP \u2190 ALU_OUT[ADD]"), "jz should update RIP");
        assertTrue(ops.contains("endif"), "jz should have endif");
    }

    @Test
    @DisplayName("jnc: negated conditional jump on carry flag")
    public void testJnc() throws ParseException {
        OperandMemory target = new OperandMemory(-1, -1, -1, -1, 0x100, 8);
        Instruction insn = new InstructionClass6("jnc", target);

        List<String> ops = MicrocodeGenerator.generate(insn);
        assertTrue(ops.contains("if FLAGS[CF] == 0"), "jnc should check CF == 0");
    }
}
