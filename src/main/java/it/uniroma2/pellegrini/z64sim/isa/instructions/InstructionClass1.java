/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass1 extends Instruction {
    private static final Logger log = LoggerFactory.getLogger();

    private final Operand source;
    private final Operand destination;
    private final int implicitSize; // For instructions such as pushf, popf, movs, stos

    public InstructionClass1(String mnemonic, Operand s, Operand d, int implicitSize) throws ParseException {
        super(mnemonic, 1);
        this.source = s;
        this.destination = d;
        this.implicitSize = implicitSize;

        if(s instanceof OperandImmediate && s.getSize() == 8 || s instanceof OperandImmediate && d instanceof OperandMemory && d.getSize() == 8) {
            this.setSize(16);
        } else {
            this.setSize(8);
        }
    }


    @Override
    public void run() throws SimulatorException {
        Long srcValue = SimulatorController.getOperandValue(this.source);

        // TODO: migrate all these to algorithm lambdas
        // TODO: use an enum to identify the instruction rather than a string
        switch(mnemonic) {
            case "mov":
                SimulatorController.setOperandValue(this.destination, srcValue);
                break;
            case "movsX":
                switch(this.source.getSize()) {
                    case 1:
                        boolean msb = (srcValue.byteValue() & 0x80) == 0x80;
                        if(msb) {
                            srcValue = srcValue | 0xFFFFFFFFFFFFFF00L;
                        } else {
                            srcValue = srcValue & 0x00000000000000FFL;
                        }
                        break;
                    case 2:
                        msb = (srcValue.shortValue() & 0x8000) == 0x8000;
                        if(msb) {
                            srcValue = srcValue | 0xFFFFFFFFFFFF0000L;
                        } else {
                            srcValue = srcValue & 0x000000000000FFFFL;
                        }
                        break;
                    case 4:
                        msb = (srcValue.intValue() & 0x80000000) == 0x80000000;
                        if(msb) {
                            srcValue = srcValue | 0xFFFFFFFF00000000L;
                        } else {
                            srcValue = srcValue & 0x00000000FFFFFFFFL;
                        }
                        break;
                }
                SimulatorController.setOperandValue(this.destination, srcValue);
                break;
            case "movzX":
                switch(this.source.getSize()) {
                    case 1:
                        srcValue = srcValue & 0x00000000000000FFL;
                        break;
                    case 2:
                        srcValue = srcValue & 0x000000000000FFFFL;
                        break;
                    case 4:
                        srcValue = srcValue & 0x00000000FFFFFFFFL;
                        break;
                }
                SimulatorController.setOperandValue(this.destination, srcValue);
                break;
            case "lea":
                srcValue = SimulatorController.computeAddressingMode((OperandMemory) this.source);
                SimulatorController.setOperandValue(this.destination, srcValue);
                break;
            case "push":
                OperandRegister sp = new OperandRegister(Register.RSP, 8);
                Long spValue = SimulatorController.getOperandValue(sp) - 8;
                // TODO: this is a hack, fix it: SP should be 8 bytes, but we're using 4 bytes for now
                OperandMemory spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                SimulatorController.setOperandValue(spMem, srcValue);
                SimulatorController.setOperandValue(sp, spValue);
                break;
            case "pop":
                sp = new OperandRegister(Register.RSP, 8);
                spValue = SimulatorController.getOperandValue(sp);
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                srcValue = SimulatorController.getOperandValue(spMem);
                SimulatorController.setOperandValue(sp, spValue + 8);
                SimulatorController.setOperandValue(this.source, srcValue);
                break;
            case "movs":
                Long rcx = SimulatorController.getOperandValue(new OperandRegister(Register.RCX, 8));
                Long rsi = SimulatorController.getOperandValue(new OperandRegister(Register.RSI, 8));
                Long rdi = SimulatorController.getOperandValue(new OperandRegister(Register.RDI, 8));
                for(int i = 0; i < rcx * this.implicitSize; i++) {
                    OperandMemory srcMem = new OperandMemory(-1, -1, -1, -1, rsi.intValue() + i, 1);
                    OperandMemory dstMem = new OperandMemory(-1, -1, -1, -1, rdi.intValue() + i, 1);
                    srcValue = SimulatorController.getOperandValue(srcMem);
                    SimulatorController.setOperandValue(dstMem, srcValue);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RCX, 8), rcx - 1);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RDI, 8), rdi + 1);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RSI, 8), rsi + 1);
                }
                break;
            case "stos":
                Long rax = SimulatorController.getOperandValue(new OperandRegister(Register.RAX, 8));
                rcx = SimulatorController.getOperandValue(new OperandRegister(Register.RCX, 8));
                rdi = SimulatorController.getOperandValue(new OperandRegister(Register.RDI, 8));
                for(int i = 0; i < rcx; i++) {
                    for(int j = 0; j < this.implicitSize; j++) {
                        OperandMemory dstMem = new OperandMemory(-1, -1, -1, -1, rdi.intValue() + i * this.implicitSize + j, 1);
                        long byteValue = (rax >> (j * 8)) & 0xFF;
                        SimulatorController.setOperandValue(dstMem, byteValue);
                    }
                    SimulatorController.setOperandValue(new OperandRegister(Register.RCX, 8), rcx - 1);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RDI, 8), rdi + this.implicitSize);
                }
                break;
            case "popf":
                sp = new OperandRegister(Register.RSP, 8);
                spValue = SimulatorController.getOperandValue(sp);
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                srcValue = SimulatorController.getOperandValue(spMem);
                SimulatorController.setOperandValue(sp, spValue + 8);
                SimulatorController.getCpuState().setFlags(srcValue);
                break;
            case "pushf":
                sp = new OperandRegister(Register.RSP, 8);
                spValue = SimulatorController.getOperandValue(sp) - 8;
                // TODO: this is a hack, fix it: SP should be 8 bytes, but we're using 4 bytes for now
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                SimulatorController.setOperandValue(spMem, SimulatorController.getCpuState().getFlags());
                SimulatorController.setOperandValue(sp, spValue);
                break;
            default:
                throw new RuntimeException("Unknown Class 1 instruction: " + mnemonic);
        }
    }

    // TODO: consider moving source and destination to the parent class
    public Operand getSource() {
        return this.source;
    }

    public Operand getDestination() {
        return this.destination;
    }

    private String movsXGetSize() {
        String size = "";
        switch(this.source.getSize()) {
            case 1:
                size += "b";
                break;
            case 2:
                size += "w";
                break;
            case 4:
                size += "l";
                break;
        }
        switch(this.destination.getSize()) {
            case 2:
                size += "w";
                break;
            case 4:
                size += "l";
                break;
            case 8:
                size += "q";
                break;
        }
        return size;
    }

    private String implicitSizeToInsnSuffix() throws DisassembleException {
        switch(this.implicitSize) {
            case 1:
                return "b";
            case 2:
                return "w";
            case 4:
                return "l";
            case 8:
                return "q";
       }
       throw new DisassembleException("Invalid implicit size");
    }
    @Override
    public String toString() {
        String insn;

        if("movsX".equals(this.mnemonic)) {
            insn = "movs" + this.movsXGetSize();
        } else if("movzX".equals(this.mnemonic)) {
            insn = "movz" + this.movsXGetSize();
        } else if("movs".equals(this.mnemonic) || "stos".equals(this.mnemonic)) {
            try {
                insn = this.mnemonic + this.implicitSizeToInsnSuffix();
            } catch(DisassembleException e) {
                throw new RuntimeException(e);
            }
        } else if("pushf".equals(this.mnemonic) || "popf".equals(this.mnemonic)) {
            insn = this.mnemonic;
        } else {
            try {
                insn = this.mnemonic + this.source.getSizeSuffix();
            } catch(DisassembleException e) {
                throw new RuntimeException(e);
            }
        }

        if(this.source != null) {
            insn += " " + this.source;
        }
        if(this.destination != null) {
            insn += ", " + this.destination;
        }

        return insn;
    }
}
