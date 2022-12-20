/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass3 extends Instruction {
    private static final Logger log = LoggerFactory.getLogger();

    // TODO: make consistent with src and dest. Can use RCX as source when explicit places is given
    private final int places;
    private final OperandRegister reg;

    public InstructionClass3(String mnemonic, int p, OperandRegister r) {
        super(mnemonic, 3);
        this.places = p;
        this.reg = r;

        // First byte is the class
        byte[] enc = {0b00110000, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        this.setSize(8);

        enc[0] = 0b00110000;

        byte ss = 0b00000000;
        byte sd = 0b00000000;
        byte di = 0b00000000;
        byte diImm = 0b00000000;
        byte mem = 0b00000000;

        if(reg != null) {
            switch(reg.getSize()) {
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
        } else {
            ss = 0b00000000;
        }
        if(places == -1) {
            sd = ss;
            diImm = 0b00000000;
        } else {
            diImm = 0b00000100;
        }
        //MODE
        enc[1] = (byte) (ss | sd | diImm | di | mem);

        //SIB
        byte Bp = 0b00000000;
        byte Ip = 0b00000000;
        byte Scale = 0b00000000;
        byte Reg = 0b00000000;

        enc[2] = (byte) (Bp | Ip | Scale | Reg);

        //R-M
        byte sour = 0b00000000;
        byte dest = 0b00000000;

        dest = (byte) (((OperandRegister) reg).getRegister());
        enc[3] = (byte) (sour | dest);

        enc[4] = (byte) (places >> 24);
        enc[5] = (byte) (places >> 16);
        enc[6] = (byte) (places >> 8);
        enc[7] = (byte) (places);

        switch(mnemonic) {
            case "sal":
            case "shl":
                if(this.places != -1) {
                    this.type = 0x00;
                } else this.type = 0x01;
                break;
            case "sar":
                if(this.places != -1) {
                    this.type = 0x02;
                } else this.type = 0x03;
                break;
            case "shr":
                if(this.places != -1) {
                    this.type = 0x04;
                } else this.type = 0x05;
                break;
            case "rcl":
                if(this.places != -1) {
                    this.type = 0x06;
                } else this.type = 0x07;
                break;
            case "rcr":
                if(this.places != -1) {
                    this.type = 0x08;
                } else this.type = 0x09;
                break;
            case "rol":
                if(this.places != -1) {
                    this.type = 0x0a;
                } else this.type = 0x0b;
                break;
            case "ror":
                if(this.places != -1) {
                    this.type = 0x0c;
                } else this.type = 0x0d;
                break;
            default:
                throw new RuntimeException("Unknown Class 3 instruction: " + mnemonic);
        }
        this.setSize(size);
        enc[0] = (byte) (enc[0] | this.type);
        this.setEncoding(enc);

    }

    @Override
    public void run() {
        Long value = SimulatorController.getOperandValue(this.reg);

        long mask = 0;
        switch(this.reg.getSize()) {
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
        long msbMask = mask & (~mask >> 1);
        long msb = value & msbMask;
        long lsb = value & 1;

        long result;

        switch(mnemonic) {
            case "sal":
            case "shl":
                result = value << places;
                SimulatorController.updateFlagsAndRefresh(-1, -1, result, this.reg.getSize());
                SimulatorController.setCF(msb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
                break;
            case "sar":
                result = value >>> places;
                SimulatorController.updateFlagsAndRefresh(-1, -1, result, this.reg.getSize());
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(false);
                }
                break;
            case "shr":
                result = value >> places;
                SimulatorController.updateFlagsAndRefresh(-1, -1, result, this.reg.getSize());
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(msb == 1);
                }
                break;
            case "rcl":
                result = 0; // TODO
                SimulatorController.setCF(msb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
            case "rcr":
                result = 0; // TODO
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
            case "rol":
                result = 0; // TODO
                SimulatorController.setCF(msb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
            case "ror":
                result = 0; // TODO
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ (result & (msbMask >> 1))) == 1);
                }
                throw new UnsupportedOperationException("Not supported yet.");
            default:
                throw new RuntimeException("Unknown Class 3 instruction: " + mnemonic);
        }

        SimulatorController.setOperandValue(this.reg, result);
    }


    public static String disassemble(byte[] encoding) throws DisassembleException {
        String instr = "";
        switch(encoding[0]) {
            case 0x30:
            case 0x31:
                instr += "shl ";
                break;
            case 0x32:
            case 0x33:
                instr += "sar ";
                break;
            case 0x34:
            case 0x35:
                instr += "shr ";
                break;
            case 0x36:
            case 0x37:
                instr += "rcl ";
                break;
            case 0x38:
            case 0x39:
                instr += "rcr ";
                break;
            case 0x3a:
            case 0x3b:
                instr += "rol ";
                break;
            case 0x3c:
            case 0x3d:
                instr += "ror ";
                break;
            default:
                throw new RuntimeException("Unknown instruction type");

        }
        int sizeInt = 0;

        switch(byteToBits(encoding[1], 7, 6)) {
            case 0:
                sizeInt = 8;
                break;
            case 1:
                sizeInt = 16;
                break;
            case 2:
                sizeInt = 32;
                break;
            case 3:
                sizeInt = 64;
                break;
            default:
                throw new DisassembleException("Wrong value size");
        }
        if(byteToBits(encoding[1], 2, 2) == 1) {
            instr += "$" + ((encoding[7] << 24) + (encoding[6] << 16) + (encoding[5] << 8) + encoding[4]) + ",";
        }
        int destRegister = byteToBits(encoding[3], 3, 0);
        String dest_Reg = Register.getRegisterName(destRegister, sizeInt);
        instr += dest_Reg;

        log.trace("disassembled: {} {} {} {} {} {} {} {}",
            encoding[0], encoding[1], encoding[2], encoding[3], encoding[4], encoding[5], encoding[6], encoding[7]);
        return instr;
    }

    @Override
    public String toString() {
        try {
            return this.mnemonic + this.reg.getSizeSuffix() + " $" + this.places + ", " + this.reg;
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }
    }
}
