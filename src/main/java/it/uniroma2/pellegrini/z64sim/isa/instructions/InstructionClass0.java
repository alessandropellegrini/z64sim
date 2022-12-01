/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;

public class InstructionClass0 extends Instruction {

    int idn;

    public InstructionClass0(String mnemonic, int idn) throws ParseException {
        super(mnemonic, 0);
        this.idn = idn;

        // Set the size in memory
        this.setSize(8);

        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        switch(mnemonic) {
            case "hlt":
                enc[0] = 0x01;
                this.type = 0x01;
                break;
            case "nop":
                enc[0] = 0x02;
                this.type = 0x02;
                break;
            case "int":
                enc[0] = 0x03;
                this.type = 0x03;
            default:
                throw new ParseException("Unknown Class 0 instruction: " + mnemonic);
        }

        this.setEncoding(enc);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static String disassemble(byte[] encoding) throws DisassembleException {
        String instr = "";
        switch(encoding[0]) {
            case 0x01:
                instr += "hlt";
                break;
            case 0x02:
                instr += "nop";
                break;
            case 0x03:
                instr += "int";
                break;
            default:
                throw new DisassembleException("Unkown instruction type");

        }
        return instr;
    }

}
