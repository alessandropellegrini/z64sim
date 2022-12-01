/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.model.Memory;
;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass7 extends Instruction {

    private final int transfer_size; // The size of a data transfer

    public InstructionClass7(String mnemonic, int size) {
        super(mnemonic, 7);
        this.transfer_size = size;

        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        enc[0] = 0b01110000;
        byte di = 0b00000000;
        byte diImm = 0b00000000;
        byte mem = 0b00000000;
        byte ss = (byte) transfer_size;
        byte ds = (byte) transfer_size;

        enc[1] = (byte) (ss | ds | diImm | di | mem);

        switch(mnemonic) {
            case "in":
                this.type = 0x00;
                break;
            case "out":
                this.type = 0x01;
                break;
            case "ins":
                this.type = 0x02;
                break;
            case "outs":
                this.type = 0x03;
                break;
            default:
                throw new RuntimeException("Unknown Class 7 instruction: " + mnemonic);
        }

        enc[0] = (byte) (enc[0] | this.type);
        this.setEncoding(enc);

        this.setSize(8);

    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public static String disassemble(byte[] encoding) throws DisassembleException {
        byte b[] = new byte[8];
        for(int i = 0; i < 8; i++) {
            b[i] = Memory.getProgram().program[address + i];
        }
        String instr = "";

        switch(b[0]) {
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
        switch(b[1]) {
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
}
