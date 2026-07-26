/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass5 extends Instruction {
    private static final Logger log = LoggerFactory.getLogger();

    private static final String[] MNEMONICS = {"jmp", "call", "ret", "iret"};

    private final Operand target;

    public InstructionClass5(String mnemonic, Operand t) {
        super(mnemonic, 5);
        this.target = t;
        this.setSize(8);
    }

    @Override
    public int getType() {
        if ("retq".equals(this.mnemonic)) {
            return 2;
        }
        if ("iretq".equals(this.mnemonic)) {
            return 3;
        }
        return lookupType(MNEMONICS);
    }

    @Override
    protected byte[] encode() {
        byte[] buf = new byte[this.size];
        buf[0] = encodeOpcode(getType());

        if (this.target instanceof OperandMemory) {
            OperandMemory mem = (OperandMemory) this.target;
            buf[1] = encodeMode(0, 0, 2, 0);
            buf[2] = encodeSib(0, 0, 0, 0);
            buf[3] = encodeRm(0, 0);
            if (mem.getDisplacement() != null) {
                writeLE32(buf, 4, mem.getDisplacement());
            }
        } else if (this.target instanceof OperandRegister) {
            OperandRegister reg = (OperandRegister) this.target;
            buf[1] = encodeMode(0, 0, 0, 0);
            buf[2] = encodeSib(0, 0, 0, 0);
            buf[3] = encodeRm(0, reg.getRegister());
        }

        return buf;
    }

    @Override
    public void run() throws SimulatorException {
        Long dest = SimulatorController.getOperandValue(this.target);

        switch(mnemonic) {
            case "jmp":
                SimulatorController.setRIP(dest);
                break;
            case "call":
                OperandRegister sp = new OperandRegister(Register.RSP, 8);
                Long spValue = SimulatorController.getOperandValue(sp) - 8;
                // TODO: this is a hack, fix it: SP should be 8 bytes, but we're using 4 bytes for now
                OperandMemory spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                SimulatorController.setOperandValue(spMem, SimulatorController.getRIP());
                SimulatorController.setOperandValue(sp, spValue);
                SimulatorController.setRIP(dest);
                break;
            case "ret":
            case "retq":
                sp = new OperandRegister(Register.RSP, 8);
                spValue = SimulatorController.getOperandValue(sp);
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                SimulatorController.setRIP(SimulatorController.getOperandValue(spMem));
                SimulatorController.setOperandValue(sp, spValue + 8);
                break;
            case "iret":
            case "iretq":
                // Pop FLAGS first (top of stack — pushed last by int)
                sp = new OperandRegister(Register.RSP, 8);
                spValue = SimulatorController.getOperandValue(sp);
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                Long rflags = SimulatorController.getOperandValue(spMem);
                spValue += 8;

                // Pop RIP second (deeper — pushed first by int)
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                Long rip = SimulatorController.getOperandValue(spMem);
                spValue += 8;

                SimulatorController.setOperandValue(sp, spValue);
                SimulatorController.getCpuState().setFlags(rflags);
                SimulatorController.setRIP(rip);
                // Re-enable interrupts (pushed FLAGS had IF=0)
                SimulatorController.getCpuState().setIF(true);
                break;
            default:
                throw new RuntimeException("Unknown Class 5 instruction: " + mnemonic);
        }
    }

    public Operand getTarget() {
        return this.target;
    }

    @Override
    public String toString() {
        String insn = this.mnemonic + " ";
        if(this.target != null) {
            if(this.target instanceof OperandRegister)
                insn += "*";
            insn += this.target;
        }
        return insn;
    }

}
