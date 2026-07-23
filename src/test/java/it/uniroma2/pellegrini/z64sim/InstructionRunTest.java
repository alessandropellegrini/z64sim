/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.*;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Instruction run() tests")
public class InstructionRunTest {

    @BeforeEach
    public void setup() {
        SimulatorController.init();
    }

    // =====================================================================
    // Class 0: hlt, int
    // =====================================================================
    @Nested
    @DisplayName("Class 0 — System instructions")
    class Class0Tests {
        @Test
        @DisplayName("hlt displaces RIP backward by instruction size")
        public void testHlt() throws ParseException, SimulatorException {
            SimulatorController.getCpuState().setRIP(0x1000L);
            Instruction inst = new InstructionClass0("hlt", null);
            inst.run();
            assertEquals(0x1000L - 8, SimulatorController.getCpuState().getRIP());
        }

        @Test
        @DisplayName("int throws UnsupportedOperationException")
        public void testInt() throws ParseException {
            Instruction inst = new InstructionClass0("int", new OperandImmediate(0x80));
            assertThrows(UnsupportedOperationException.class, inst::run);
        }
    }

    // =====================================================================
    // Class 1: mov, movsX, movzX, lea, push, pop, pushf, popf
    // =====================================================================
    @Nested
    @DisplayName("Class 1 — Data movement")
    class Class1Tests {
        @Test
        @DisplayName("mov reg to reg")
        public void testMov() throws ParseException, SimulatorException {
            OperandRegister src = new OperandRegister(Register.RBX, 8);
            OperandRegister dst = new OperandRegister(Register.RAX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x12345678L);

            Instruction inst = new InstructionClass1("mov", src, dst, 0);
            inst.run();

            assertEquals(0x12345678L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        }

        @Test
        @DisplayName("movsX sign-extends negative byte to qword")
        public void testMovsXNegative() throws ParseException, SimulatorException {
            OperandRegister src = new OperandRegister(Register.RBX, 1);
            OperandRegister dst = new OperandRegister(Register.RAX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x80L);

            Instruction inst = new InstructionClass1("movsX", src, dst, 0);
            inst.run();

            assertEquals(0xFFFFFFFFFFFFFF80L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        }

        @Test
        @DisplayName("movsX sign-extends positive byte to qword (no extension)")
        public void testMovsXPositive() throws ParseException, SimulatorException {
            OperandRegister src = new OperandRegister(Register.RBX, 1);
            OperandRegister dst = new OperandRegister(Register.RAX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x7FL);

            Instruction inst = new InstructionClass1("movsX", src, dst, 0);
            inst.run();

            assertEquals(0x7FL, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        }

        @Test
        @DisplayName("movsX sign-extends negative word to qword")
        public void testMovsXWordNegative() throws ParseException, SimulatorException {
            OperandRegister src = new OperandRegister(Register.RBX, 2);
            OperandRegister dst = new OperandRegister(Register.RAX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0x8000L);

            Instruction inst = new InstructionClass1("movsX", src, dst, 0);
            inst.run();

            assertEquals(0xFFFFFFFFFFFF8000L, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        }

        @Test
        @DisplayName("movzX zero-extends byte to qword")
        public void testMovzX() throws ParseException, SimulatorException {
            OperandRegister src = new OperandRegister(Register.RBX, 1);
            OperandRegister dst = new OperandRegister(Register.RAX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0xFFL);

            Instruction inst = new InstructionClass1("movzX", src, dst, 0);
            inst.run();

            assertEquals(0xFFL, SimulatorController.getCpuState().getRegisterValue(Register.RAX));
        }

        @Test
        @DisplayName("movzX zero-extends word to qword")
        public void testMovzXWord() throws ParseException, SimulatorException {
            OperandRegister src = new OperandRegister(Register.RBX, 2);
            OperandRegister dst = new OperandRegister(Register.RAX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RBX, 0xFFFF_ABCDL);

            Instruction inst = new InstructionClass1("movzX", src, dst, 0);
            inst.run();

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

    // =====================================================================
    // Class 2: add, sub, adc, sbb, cmp, test, neg, and, or, xor, not, bt
    // =====================================================================
    @Nested
    @DisplayName("Class 2 — Arithmetic/Logic")
    class Class2Tests {

        // --- ADD ---
        @Test
        @DisplayName("add $5 to rax=10 → result=15, no flags")
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
        @DisplayName("add reg to reg: al=0xFF + bl=1 → overflow byte, CF=1, ZF=1")
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
        @DisplayName("add reg to reg: al=0x7F + bl=0x01 → signed overflow, OF=1, SF=1")
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

        // --- SUB ---
        @Test
        @DisplayName("sub $3 from rax=10 → result=7")
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
        @DisplayName("sub $1 from al=0 → borrow, CF=1")
        public void testSubBorrow() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(1);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0L);

            new InstructionClass2("sub", src, dst).run();

            assertEquals(0xFFL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
            assertTrue(SimulatorController.getCF());
        }

        // --- CMP ---
        @Test
        @DisplayName("cmp $5, %rax with rax=5 → ZF=1, CF=0, destination unchanged")
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
        @DisplayName("cmp $10, %rax with rax=5 → CF=1 (5 < 10)")
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
        @DisplayName("test $0xFF, %al with al=0 → ZF=1, CF=0, OF=0")
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
        @DisplayName("test $0x0F, %al with al=0xF0 → ZF=1 (AND=0)")
        public void testTestNonOverlapping() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(0x0F);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0xF0L);

            new InstructionClass2("test", src, dst).run();

            assertTrue(SimulatorController.getZF());
        }

        // --- NEG ---
        @Test
        @DisplayName("neg al=5 → result=0xFB (-5 byte), CF=1")
        public void testNeg() throws ParseException, SimulatorException {
            OperandRegister op = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);

            // neg takes (source, destination) — operates on source
            new InstructionClass2("neg", op, null).run();

            assertEquals(0xFBL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
            assertTrue(SimulatorController.getCF());
        }

        @Test
        @DisplayName("neg al=0 → result=0, CF=0")
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
        @DisplayName("and $0x0F, %al with al=0xFF → result=0x0F, CF=0, OF=0")
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
        @DisplayName("or $0xF0, %al with al=0x0F → result=0xFF")
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
        @DisplayName("xor %rax, %rax → result=0, ZF=1")
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
        @DisplayName("not al=0x55 → result=0xAA, no flag change")
        public void testNot() throws ParseException, SimulatorException {
            OperandRegister op = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x55L);

            // not takes (source, destination) — operates on source
            new InstructionClass2("not", op, null).run();

            assertEquals(0xAAL, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        }

        // --- BT ---
        @Test
        @DisplayName("bt $3, %al with al=0x08 → CF=1 (bit 3 set)")
        public void testBtTrue() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(3);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x08L);

            new InstructionClass2("bt", src, dst).run();

            assertTrue(SimulatorController.getCF());
        }

        @Test
        @DisplayName("bt $3, %al with al=0x00 → CF=0 (bit 3 clear)")
        public void testBtFalse() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(3);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 0x00L);

            new InstructionClass2("bt", src, dst).run();

            assertFalse(SimulatorController.getCF());
        }

        // --- ADC ---
        @Test
        @DisplayName("adc $1, %al with al=2, CF=1 → result=4")
        public void testAdcWithCarry() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(1);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 2L);
            SimulatorController.setCF(true);

            new InstructionClass2("adc", src, dst).run();

            assertEquals(4L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        }

        @Test
        @DisplayName("adc $1, %al with al=2, CF=0 → result=3")
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
        @DisplayName("sbb $1, %al with al=5, CF=0 → result=3 (CF inverted adds 1)")
        public void testSbbNoCarry() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(1);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);
            SimulatorController.setCF(false);

            new InstructionClass2("sbb", src, dst).run();

            // sbb: srcValue += CF ? 0 : 1 → srcValue = 1+1=2, result = 5-2=3
            assertEquals(3L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        }

        @Test
        @DisplayName("sbb $1, %al with al=5, CF=1 → result=4")
        public void testSbbWithCarry() throws ParseException, SimulatorException {
            OperandImmediate src = new OperandImmediate(1);
            OperandRegister dst = new OperandRegister(Register.RAX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RAX, 5L);
            SimulatorController.setCF(true);

            new InstructionClass2("sbb", src, dst).run();

            // sbb: srcValue += CF ? 0 : 1 → srcValue = 1+0=1, result = 5-1=4
            assertEquals(4L, SimulatorController.getCpuState().getRegisterValue(Register.RAX) & 0xFFL);
        }

        // --- Multi-size test ---
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
    }

    // =====================================================================
    // Class 3: shl/sal, sar, shr, rcl, rcr, rol, ror
    // =====================================================================
    @Nested
    @DisplayName("Class 3 — Shift/Rotate")
    class Class3Tests {
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

        @Test
        @DisplayName("sar $1, %rdx with rdx=0x8000000000000000 → sign preserved (0xC...0)")
        public void testSarSignPreserved() throws SimulatorException {
            OperandRegister reg = new OperandRegister(Register.RDX, 8);
            SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x8000000000000000L);

            new InstructionClass3("sar", 1, reg).run();

            assertEquals(0xC000000000000000L, SimulatorController.getCpuState().getRegisterValue(Register.RDX));
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
        @DisplayName("rol $1, %dl with dl=0x80 → result=0x01 (MSB rotated to LSB)")
        public void testRol() throws SimulatorException {
            OperandRegister reg = new OperandRegister(Register.RDX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x80L);

            new InstructionClass3("rol", 1, reg).run();

            assertEquals(0x01L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        }

        @Test
        @DisplayName("ror $1, %dl with dl=0x01 → result=0x80 (LSB rotated to MSB)")
        public void testRor() throws SimulatorException {
            OperandRegister reg = new OperandRegister(Register.RDX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x01L);

            new InstructionClass3("ror", 1, reg).run();

            assertEquals(0x80L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        }

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

        @Test
        @DisplayName("shl $4, %dl with dl=0x0F → result=0xF0")
        public void testMultiBitShift() throws SimulatorException {
            OperandRegister reg = new OperandRegister(Register.RDX, 1);
            SimulatorController.getCpuState().setRegisterValue(Register.RDX, 0x0FL);

            new InstructionClass3("shl", 4, reg).run();

            assertEquals(0xF0L, SimulatorController.getCpuState().getRegisterValue(Register.RDX) & 0xFFL);
        }
    }

    // =====================================================================
    // Class 4: flag set/clear instructions
    // =====================================================================
    @Nested
    @DisplayName("Class 4 — Flag manipulation")
    class Class4Tests {
        @Test
        @DisplayName("clc/stc: clear and set carry flag")
        public void testClcStc() throws SimulatorException {
            SimulatorController.setCF(true);
            new InstructionClass4("clc").run();
            assertFalse(SimulatorController.getCF());

            new InstructionClass4("stc").run();
            assertTrue(SimulatorController.getCF());
        }

        @Test
        @DisplayName("clz/stz: clear and set zero flag")
        public void testClzStz() throws SimulatorException {
            SimulatorController.setZF(true);
            new InstructionClass4("clz").run();
            assertFalse(SimulatorController.getZF());

            new InstructionClass4("stz").run();
            assertTrue(SimulatorController.getZF());
        }

        @Test
        @DisplayName("cls/sts: clear and set sign flag")
        public void testClsSts() throws SimulatorException {
            SimulatorController.setSF(true);
            new InstructionClass4("cls").run();
            assertFalse(SimulatorController.getSF());

            new InstructionClass4("sts").run();
            assertTrue(SimulatorController.getSF());
        }

        @Test
        @DisplayName("clp/stp: clear and set parity flag")
        public void testClpStp() throws SimulatorException {
            SimulatorController.setPF(true);
            new InstructionClass4("clp").run();
            assertFalse(SimulatorController.getPF());

            new InstructionClass4("stp").run();
            assertTrue(SimulatorController.getPF());
        }

        @Test
        @DisplayName("clo/sto: clear and set overflow flag")
        public void testCloSto() throws SimulatorException {
            SimulatorController.setOF(true);
            new InstructionClass4("clo").run();
            assertFalse(SimulatorController.getOF());

            new InstructionClass4("sto").run();
            assertTrue(SimulatorController.getOF());
        }

        @Test
        @DisplayName("cli/sti: clear and set interrupt flag")
        public void testCliSti() throws SimulatorException {
            SimulatorController.setIF(true);
            new InstructionClass4("cli").run();
            assertFalse(SimulatorController.getIF());

            new InstructionClass4("sti").run();
            assertTrue(SimulatorController.getIF());
        }

        @Test
        @DisplayName("cld/std: clear and set direction flag")
        public void testCldStd() throws SimulatorException {
            SimulatorController.setDF(true);
            new InstructionClass4("cld").run();
            assertFalse(SimulatorController.getDF());

            new InstructionClass4("std").run();
            assertTrue(SimulatorController.getDF());
        }
    }

    // =====================================================================
    // Class 5: jmp, call, ret
    // =====================================================================
    @Nested
    @DisplayName("Class 5 — Control flow")
    class Class5Tests {
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

    // =====================================================================
    // Class 6: conditional jumps
    // =====================================================================
    @Nested
    @DisplayName("Class 6 — Conditional jumps")
    class Class6Tests {
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
    }

    // =====================================================================
    // Class 7: I/O (unsupported)
    // =====================================================================
    @Nested
    @DisplayName("Class 7 — I/O instructions")
    class Class7Tests {
        @Test
        @DisplayName("in throws UnsupportedOperationException")
        public void testIn() throws ParseException {
            OperandImmediate ioport = new OperandImmediate(0x123);
            Instruction in = new InstructionClass7("in", 4, ioport);
            assertThrows(UnsupportedOperationException.class, in::run);
        }

        @Test
        @DisplayName("out throws UnsupportedOperationException")
        public void testOut() throws ParseException {
            OperandImmediate ioport = new OperandImmediate(0x123);
            Instruction out = new InstructionClass7("out", 4, ioport);
            assertThrows(UnsupportedOperationException.class, out::run);
        }
    }
}
