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
import org.jetbrains.annotations.NonNls;


/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass2 extends Instruction {

    private final Operand source;
    private final Operand destination;
    @NonNls
    private static final String[] opcodes = {"add", "sub", "adc", "sbb", "cmp", "test", "neg", "and", "or", "xor", "not", "bt"};

    public InstructionClass2(String mnemonic, Operand s, Operand d) throws ParseException {
        super(mnemonic, 2);
        this.source = s;
        this.destination = d;

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
        boolean sour_memory = s instanceof OperandMemory;
        boolean sour_immediate = s instanceof OperandImmediate;
        boolean dest_register = d instanceof OperandRegister;
        boolean dest_memory = d instanceof OperandMemory;

        byte ss = 0;
        byte sd = 0;
        byte di = 0;
        byte mem = 0;
        byte Bp = 0;
        byte Ip = 0;
        byte scale = 0;
        byte idxReg = 0;
        byte srcReg = 0;
        byte dstReg = 0;

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
        if(!sour_immediate && !dest_memory) di = 0b00000000;
        if(sour_immediate && !dest_memory) di = 0b00000100;
        if(!sour_immediate && dest_memory && ((OperandMemory) d).getDisplacement() != -1) di = 0b00001000;
        if(sour_immediate && dest_memory && ((OperandMemory) d).getDisplacement() != -1) di = 0b00001100;

        //Popolamento campo mem
        if(sour_register && dest_register) mem = 0b00000000;
        if(sour_register && dest_memory) mem = 0b00000001;
        if(sour_memory && dest_register) mem = 0b00000010;

        //Popolamento campo Bp
        if(sour_memory || dest_memory) {
            if(sour_memory) {
                if(((OperandMemory) s).getBase() != -1) Bp = (byte) 0b10000000;
                else Bp = 0b00000000;
            } else {
                if(((OperandMemory) d).getBase() != -1) Bp = (byte) 0b10000000;
                else Bp = 0b00000000;
            }
        }

        //Popolamento campo Ip
        if(sour_memory || dest_memory) {
            if(sour_memory) {
                if(((OperandMemory) s).getIndex() != -1) Ip = (byte) 0b01000000;
                else Ip = 0b00000000;
            } else {
                if(((OperandMemory) d).getIndex() != -1) Ip = (byte) 0b01000000;
                else Ip = 0b00000000;
            }
        }

        //Popolamento campo Scale e IndexRegister
        if(sour_memory) {
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
        if(sour_register) srcReg = (byte) (((OperandRegister) s).getRegister() << 4);
        if(sour_memory) srcReg = (byte) (((OperandMemory) s).getBase() << 4);

        if(dest_register) dstReg = (byte) ((OperandRegister) d).getRegister();
        if(dest_memory) dstReg = (byte) ((OperandMemory) d).getBase();

        //Popolamento Displacement e Immediate
        boolean min32 = false;
        boolean displ = false;
        if(sour_immediate && s.getSize() <= 32) min32 = true;

        if(sour_memory || dest_memory) {
            if(sour_memory && ((OperandMemory) s).getDisplacement() != -1) {
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
        if(sour_immediate && min32) {
            if(displ == false) {
                enc[4] = (byte) ((OperandImmediate) s).getValue();
                enc[5] = (byte) ((((OperandImmediate) s).getValue()) >> 8);
                enc[6] = (byte) ((((OperandImmediate) s).getValue()) >> 16);
                enc[7] = (byte) (((OperandImmediate) s).getValue() >> 24);
            } else {
                enc[8] = (byte) ((OperandImmediate) s).getValue();
                enc[9] = (byte) ((((OperandImmediate) s).getValue()) >> 8);
                enc[10] = (byte) ((((OperandImmediate) s).getValue()) >> 16);
                enc[11] = (byte) ((((OperandImmediate) s).getValue()) >> 24);
                enc[12] = (byte) ((((OperandImmediate) s).getValue()) >> 32);
                enc[13] = (byte) ((((OperandImmediate) s).getValue()) >> 40);
                enc[14] = (byte) ((((OperandImmediate) s).getValue()) >> 48);
                enc[15] = (byte) (((OperandImmediate) s).getValue() >> 56);
            }
        }
        if(sour_immediate && !min32) {
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
        switch(mnemonic) { // TODO: move to a table/map
            case "add":
                this.type = 0x00;
                break;
            case "sub":
                this.type = 0x01;
                break;
            case "adc":
                this.type = 0x02;
                break;
            case "sbb":
                this.type = 0x03;
                break;
            case "cmp":
                this.type = 0x04;
                break;
            case "test":
                this.type = 0x05;
                break;
            case "neg":
                this.type = 0x06;
                break;
            case "and":
                this.type = 0x07;
                break;
            case "or":
                this.type = 0x08;
                break;
            case "xor":
                this.type = 0x09;
                break;
            case "not":
                this.type = 0x0a;
                break;
            case "bt":
                this.type = 0x0b;
                break;
            default:
                throw new ParseException("Unknown Class 2 instruction: " + mnemonic);
        }

        enc[0] = (byte) ((byte) 0b00100000 | this.type);
        this.setEncoding(enc);


    }

    @Override
    public void run() {
        Long srcValue = SimulatorController.getOperandValue(this.source);
        Long dstValue = SimulatorController.getOperandValue(this.destination);

        long mask = 0;
        switch(this.source.getSize()) {
            case 1:
                mask = 0xFF;
                break;
            case 2:
                mask = 0xFFFF;
                break;
            case 4:
                mask = 0xFFFFFFFF;
                break;
            case 8:
                mask = 0xFFFFFFFFFFFFFFFFL;
                break;
        }

        switch(mnemonic) {
            case "add":
                long result = srcValue + dstValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                break;
            case "sub":
                result = dstValue - srcValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(-srcValue, dstValue, result, this.source.getSize());
                break;
            case "adc":
                srcValue += SimulatorController.getCF() ? 1 : 0;
                result = srcValue + dstValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
            case "sbb":
                srcValue += SimulatorController.getCF() ? 0 : 1;
                result = dstValue - srcValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(-srcValue, dstValue, result, this.source.getSize());
                throw new UnsupportedOperationException("Not supported yet.");
            case "cmp":
                result = dstValue - srcValue;
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                break;
            case "test":
                result = dstValue & srcValue;
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "neg":
                result = -srcValue;
                SimulatorController.setOperandValue(this.source, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(srcValue != 0);
                break;
            case "and":
                result = srcValue & dstValue & mask;
                SimulatorController.setOperandValue(this.destination, result);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "or":
                result = srcValue | dstValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "xor":
                result = srcValue ^ dstValue;
                SimulatorController.setOperandValue(this.destination, result);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "not":
                SimulatorController.setOperandValue(this.source, ~srcValue & mask);
                break;
            case "bt":
                result = dstValue & (1L << srcValue);
                SimulatorController.setCF(result != 0);
                break;
            default:
                throw new RuntimeException("Unknown Class 2 instruction: " + mnemonic);
        }
    }

    public Operand getSource() {
        return this.source;
    }

    public Operand getDestination() {
        return this.destination;
    }

    @Override
    public String toString() {
        String insn;
        try {
            insn = this.mnemonic + this.source.getSizeSuffix();
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }

        insn += " " + this.source;
        if(this.destination != null) {
            insn += ", " + this.destination;
        }

        return insn;
    }
}
