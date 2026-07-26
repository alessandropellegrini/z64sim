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
    @DisplayName("iret pops RIP and RFLAGS from stack")
    public void testIret() throws ParseException, SimulatorException {
        Program program = new Program();
        program.textSectionStart(0x800);
        Memory.setProgram(program);

        // Simulate interrupt entry: int pushes RIP first (deeper), FLAGS second (top)
        long savedRflags = 0x0202L;
        long savedRip = 0x1500L;
        long stackTop = 0x2000L;

        // Write RIP at stackTop - 8 (pushed first, so deeper)
        for (int i = 0; i < 8; i++) {
            Memory.setValueAt(stackTop - 8 + i, (byte) (savedRip >> (i * 8)));
        }
        // Write FLAGS at stackTop - 16 (pushed second, so on top)
        for (int i = 0; i < 8; i++) {
            Memory.setValueAt(stackTop - 16 + i, (byte) (savedRflags >> (i * 8)));
        }

        // RSP points to the top of the pushed frame (FLAGS)
        SimulatorController.getCpuState().setRegisterValue(Register.RSP, stackTop - 16);
        SimulatorController.getCpuState().setRIP(0x3000L); // current handler address

        new InstructionClass5("iret", null).run();

        assertEquals(savedRip, SimulatorController.getCpuState().getRIP());
        assertEquals(savedRflags, SimulatorController.getCpuState().getFlags());
        assertEquals(stackTop, SimulatorController.getCpuState().getRegisterValue(Register.RSP));
    }

    @Test
    @DisplayName("getType and encode for Class 5 instructions")
    public void testGetTypeAndEncode() {
        OperandMemory mem = new OperandMemory(-1, -1, -1, -1, 0x100, -1);
        InstructionClass5 jmpMem = new InstructionClass5("jmp", mem);
        assertEquals(0, jmpMem.getType());
        byte[] buf = jmpMem.getValue();
        assertEquals(8, buf.length);
        assertEquals((byte) 0x50, buf[0]);
        assertEquals((byte) 0x08, buf[1]); // SS=0, DS=0, DI=2, Mem=0 -> 0x08
        assertEquals((byte) 0x00, buf[4]);
        assertEquals((byte) 0x01, buf[5]);

        InstructionClass5 ret = new InstructionClass5("ret", null);
        assertEquals(2, ret.getType());

        InstructionClass5 retq = new InstructionClass5("retq", null);
        assertEquals(2, retq.getType());

        InstructionClass5 iret = new InstructionClass5("iret", null);
        assertEquals(3, iret.getType());

        InstructionClass5 iretq = new InstructionClass5("iretq", null);
        assertEquals(3, iretq.getType());
    }
}
