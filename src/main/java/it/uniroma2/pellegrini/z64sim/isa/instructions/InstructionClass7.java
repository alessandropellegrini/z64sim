/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;

;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass7 extends Instruction {

    private final int transferSize; // The size of a data transfer
    private Operand ioport; // The I/O port number in case of an explicit I/O port

//    public InstructionClass7(String mnemonic, int size) throws ParseException {
//        super(mnemonic, 7);
//        this.transferSize = size;
//
//
//        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//        enc[0] = 0b01110000;
//        byte di = 0b00000000;
//        byte diImm = 0b00000000;
//        byte mem = 0b00000000;
//        byte ss = (byte) transferSize;
//        byte ds = (byte) transferSize;
//
//        enc[1] = (byte) (ss | ds | diImm | di | mem);
//
//        switch(mnemonic) {
//            case "in":
//                this.type = 0x00;
//                break;
//            case "out":
//                this.type = 0x01;
//                break;
//            case "ins":
//                this.type = 0x02;
//                break;
//            case "outs":
//                this.type = 0x03;
//                break;
//            default:
//                throw new ParseException("Unknown Class 7 instruction: " + mnemonic);
//        }
//
//        enc[0] = (byte) (enc[0] | this.type);
//        this.setEncoding(enc);
//
//        this.setSize(8);
//        this.ioport = -1;
//    }

    public InstructionClass7(String mnemonic, int size, Operand ioport) throws ParseException {
        super(mnemonic, 7);

        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        this.transferSize = size;
        this.ioport = ioport;
        this.setSize(8);

        this.setEncoding(enc);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static String disassemble(byte[] encoding) throws DisassembleException {
        String instr = "";

        switch (encoding[0]) {
            case 0x70:
                instr += "in";
                break;
            case 0x71:
                instr += "out";
                break;
            case 0x72:
                instr += "ins";
                break;
            case 0x73:
                instr += "outs";
                break;
            default:
                throw new DisassembleException("Unknown instruction type");
        }
        switch(encoding[1]) {
            case 0x00:
                instr = instr.concat("b");
                break;
            case 0x50:
                instr = instr.concat("w");
                break;
            case (byte) 0xa0:
                instr = instr.concat("l");
                break;
            default:
                throw new DisassembleException("Wrong value size");

        }
        return instr;
    }

    private String transferSizeToInsnSuffix() throws DisassembleException {
        switch(this.transferSize) {
            case 1:
                return "b";
            case 2:
                return "w";
            case 4:
                return "l";
            case 8:
                return "q";
        }
        throw new DisassembleException("Invalid transfer size");
    }

    private String transferSizeToReg() throws DisassembleException {
        switch(this.transferSize) {
            case 1:
                return "%al";
            case 2:
                return "%ax";
            case 4:
                return "%eax";
            case 8:
                return "%rax";
        }
        throw new DisassembleException("Invalid transfer size");
    }

    private String getIoPort() {
        if(this.ioport == null) {
            return "%dx";
        }
        return this.ioport.toString();
    }

    @Override
    public String toString() {
        String insn;
        try {
            insn = this.mnemonic + this.transferSizeToInsnSuffix() + " ";
            if(this.mnemonic.equals("in")) {
                insn += this.getIoPort() + ", " + this.transferSizeToReg();
            }
            if(this.mnemonic.equals("out")) {
                insn += this.transferSizeToReg() + ", " + this.getIoPort();
            }
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }

        return insn;
    }
}
