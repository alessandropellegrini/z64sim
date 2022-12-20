/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
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
    private static final String[] opcodes = {"mov", "movs", "movz", "lea", "push", "pop", "pushf", "popf", "movs", "stos"};

    private final Operand source;
    private final Operand destination;
    private final int implicitSize; // For instructions such as pushf, popf,

    public InstructionClass1(String mnemonic, Operand s, Operand d, int implicitSize) throws ParseException {
        super(mnemonic, 1);
        this.source = s;
        this.destination = d;
        this.implicitSize = implicitSize;

        byte[] enc;

        if(s instanceof OperandImmediate && s.getSize() == 8 ||
            s instanceof OperandImmediate && d instanceof OperandMemory) {
            this.setSize(16);
            enc = new byte[16];
        } else {
            this.setSize(8);
            enc = new byte[8];
        }

        boolean sour_register = s instanceof OperandRegister;
        boolean srcMem = s instanceof OperandMemory;
        boolean srcImm = s instanceof OperandImmediate;
        boolean dest_register = d instanceof OperandRegister;
        boolean dest_memory = d instanceof OperandMemory;

        byte ss = 0, sd = 0, di = 0, mem = 0, Bp = 0, Ip = 0, scale = 0;
        byte idxReg = 0, srcReg = 0, dstReg = 0;

        //Popolamento campo ss
        if(s != null) {
            switch(s.getSize()) {
                case 1:
                    ss = 0b00000000;
                    break;
                case 2:
                    ss = 0b01000000;
                    break;
                case 4:
                    ss = (byte) 0b10000000;
                    break;
                case 8:
                    ss = (byte) 0b11000000;
                    break;
            }
        }

        //Popolamento campo ds
        if(d != null) {
            switch(d.getSize()) {
                case 1:
                    sd = 0b00000000;
                    break;
                case 2:
                    sd = 0b00010000;
                    break;
                case 4:
                    sd = 0b00100000;
                    break;
                case 8:
                    sd = 0b00110000;
                    break;
            }
        }
        //Popolamento campo di
        if(!srcImm && !dest_memory) di = 0b00000000;
        if(srcImm && !dest_memory) di = 0b00000100;
        if(!srcImm && dest_memory && ((OperandMemory) d).getDisplacement() != -1) di = 0b00001000;
        if(srcImm && dest_memory && ((OperandMemory) d).getDisplacement() != -1) di = 0b00001100;

        //Popolamento campo mem
        if(sour_register && dest_register) mem = 0b00000000;
        if(sour_register && dest_memory) mem = 0b00000001;
        if(srcMem && dest_register) mem = 0b00000010;

        //Popolamento campo Bp
        if(srcMem || dest_memory) {
            if(srcMem) {
                if(((OperandMemory) s).getBase() != -1) Bp = (byte) 0b10000000;
                else Bp = 0b00000000;
            } else {
                if(((OperandMemory) d).getBase() != -1) Bp = (byte) 0b10000000;
                else Bp = 0b00000000;
            }
        }

        //Popolamento campo Ip
        if(srcMem || dest_memory) {
            if(srcMem) {
                if(((OperandMemory) s).getIndex() != -1) Ip = (byte) 0b01000000;
                else Ip = 0b00000000;
            } else {
                if(((OperandMemory) d).getIndex() != -1) Ip = (byte) 0b01000000;
                else Ip = 0b00000000;
            }
        }

        //Popolamento campo Scale e IndexRegister
        if(srcMem) {
            switch(((OperandMemory) s).getScale()) {
                case 1:
                    scale = 0b00000000;
                    break;
                case 2:
                    scale = 0b00010000;
                    break;
                case 4:
                    scale = 0b00100000;
                    break;
                case 8:
                    scale = 0b00110000;
                    break;
            }
            idxReg = (byte) ((OperandMemory) s).getIndex();
        }
        if(dest_memory) {
            switch(((OperandMemory) d).getScale()) {
                case 1:
                    scale = 0b00000000;
                    break;
                case 2:
                    scale = 0b00010000;
                    break;
                case 4:
                    scale = 0b00100000;
                    break;
                case 8:
                    scale = 0b00110000;
                    break;
            }
            idxReg = (byte) ((OperandMemory) d).getIndex();
        }

        //Popolamento campo R/M
        if(sour_register) {
            assert s instanceof OperandRegister;
            srcReg = (byte) (((OperandRegister) s).getRegister() << 4);
        }
        if(srcMem) srcReg = (byte) (((OperandMemory) s).getBase() << 4);

        if(dest_register) {
            assert d instanceof OperandRegister;
            dstReg = (byte) ((OperandRegister) d).getRegister();
        }
        if(dest_memory) dstReg = (byte) ((OperandMemory) d).getBase();

        //Popolamento Displacement e Immediate
        boolean min32 = false;
        boolean displ = false;
        if(srcImm && s.getSize() <= 32) min32 = true;

        if(srcMem || dest_memory) {
            if(srcMem && ((OperandMemory) s).getDisplacement() != -1) {
                enc[4] = (byte) (((OperandMemory) s).getDisplacement() & 0xFF);
                enc[5] = (byte) (((((OperandMemory) s).getDisplacement()) >> 8) & 0xFF);
                enc[6] = (byte) (((((OperandMemory) s).getDisplacement()) >> 16) & 0xFF);
                enc[7] = (byte) ((((OperandMemory) s).getDisplacement() >> 24) & 0xFF);
                displ = true;
            }
            if(dest_memory && ((OperandMemory) d).getDisplacement() != -1) {
                enc[4] = (byte) (((OperandMemory) d).getDisplacement() & 0xFF);
                enc[5] = (byte) (((((OperandMemory) d).getDisplacement()) >> 8) & 0xFF);
                enc[6] = (byte) (((((OperandMemory) d).getDisplacement()) >> 16) & 0xFF);
                enc[7] = (byte) ((((OperandMemory) d).getDisplacement() >> 24) & 0xFF);
                displ = true;
            }
        }
        if(srcImm && min32) {
            if(displ) {
                enc[8] = (byte) ((OperandImmediate) s).getValue();
                enc[9] = (byte) ((((OperandImmediate) s).getValue()) >> 8);
                enc[10] = (byte) ((((OperandImmediate) s).getValue()) >> 16);
                enc[11] = (byte) ((((OperandImmediate) s).getValue()) >> 24);
                enc[12] = (byte) ((((OperandImmediate) s).getValue()) >> 32);
                enc[13] = (byte) ((((OperandImmediate) s).getValue()) >> 40);
                enc[14] = (byte) ((((OperandImmediate) s).getValue()) >> 48);
                enc[15] = (byte) (((OperandImmediate) s).getValue() >> 56);
            } else {
                enc[4] = (byte) ((OperandImmediate) s).getValue();
                enc[5] = (byte) ((((OperandImmediate) s).getValue()) >> 8);
                enc[6] = (byte) ((((OperandImmediate) s).getValue()) >> 16);
                enc[7] = (byte) (((OperandImmediate) s).getValue() >> 24);
            }
        }
        if(srcImm && !min32) {
            enc[8] = (byte) ((OperandImmediate) s).getValue();
            enc[9] = (byte) ((((OperandImmediate) s).getValue()) >> 8);
            enc[10] = (byte) ((((OperandImmediate) s).getValue()) >> 16);
            enc[11] = (byte) ((((OperandImmediate) s).getValue()) >> 24);
            enc[12] = (byte) ((((OperandImmediate) s).getValue()) >> 32);
            enc[13] = (byte) ((((OperandImmediate) s).getValue()) >> 40);
            enc[14] = (byte) ((((OperandImmediate) s).getValue()) >> 48);
            enc[15] = (byte) (((OperandImmediate) s).getValue() >> 56);
        }

        //MODE
        enc[1] = (byte) (ss | sd | di | mem);
        //SIB
        enc[2] = (byte) (Bp | Ip | scale | idxReg);
        //R-M
        enc[3] = (byte) (srcReg | dstReg);
        //Opcode
        switch(mnemonic) {
            case "mov":
                this.type = 0x00;
                break;
            case "movsX":
                this.type = 0x01;
                break;
            case "movzX":
                this.type = 0x02;
                break;
            case "lea":
                this.type = 0x03;
                break;
            case "push":
                this.type = 0x04;
                break;
            case "pop":
                this.type = 0x05;
                break;
            case "pushf":
                this.type = 0x06;
                break;
            case "popf":
                this.type = 0x07;
                break;
            case "movs":
                this.type = 0x08;
                break;
            case "stos":
                this.type = 0x09;
                break;
            default:
                throw new ParseException("Unknown Class 1 instruction: " + mnemonic);
        }

        enc[0] = (byte) ((byte) 0b00010000 | this.type);
        this.setEncoding(enc);
        log.trace("Assembled INS1: {0} SS: {0} DS: {0} DI: {0} MEM: {0} Bp: {0} Ip: {0} Scale: {0} IDX: {0} srcReg: {0} dstReg: {0}",
            enc[0],
            ss,
            sd,
            di,
            mem,
            Bp,
            Ip,
            scale,
            idxReg,
            srcReg,
            dstReg);
    }


    @Override
    public void run() {
        Long srcValue = SimulatorController.getOperandValue(this.source);

        // TODO: migrate all these to algorithm lambdas
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
                SimulatorController.setOperandValue(this.destination, srcValue);
                break;
            case "movs":
                Long rcx = SimulatorController.getOperandValue(new OperandRegister(Register.RCX, 8));
                Long rsi = SimulatorController.getOperandValue(new OperandRegister(Register.RDI, 8));
                Long rdi = SimulatorController.getOperandValue(new OperandRegister(Register.RSI, 8));
                for(int i = 0; i < rcx * this.source.getSize(); i++) {
                    OperandMemory srcMem = new OperandMemory(-1, -1, -1, -1, rsi.intValue() + i, 1);
                    OperandMemory dstMem = new OperandMemory(-1, -1, -1, -1, rdi.intValue() + i, 1);
                    srcValue = SimulatorController.getOperandValue(srcMem);
                    SimulatorController.setOperandValue(dstMem, srcValue);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RCX, 8), rcx - 1);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RDI, 8), rdi + this.source.getSize());
                    SimulatorController.setOperandValue(new OperandRegister(Register.RSI, 8), rsi + this.source.getSize());
                }
            case "stos":
                Long rax = SimulatorController.getOperandValue(new OperandRegister(Register.RAX, 8));
                rcx = SimulatorController.getOperandValue(new OperandRegister(Register.RCX, 8));
                rdi = SimulatorController.getOperandValue(new OperandRegister(Register.RDI, 8));
                for(int i = 0; i < rcx * this.destination.getSize(); i++) {
                    OperandMemory dstMem = new OperandMemory(-1, -1, -1, -1, rdi.intValue() + i, 1);
                    SimulatorController.setOperandValue(dstMem, rax);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RCX, 8), rcx - 1);
                    SimulatorController.setOperandValue(new OperandRegister(Register.RDI, 8), rdi + this.destination.getSize());
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
