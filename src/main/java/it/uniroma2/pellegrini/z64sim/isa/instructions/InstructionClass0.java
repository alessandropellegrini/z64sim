/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;

public class InstructionClass0 extends Instruction {

    OperandImmediate ivn;

    public InstructionClass0(String mnemonic, OperandImmediate ivn) throws ParseException {
        super(mnemonic, 0);
        this.ivn = ivn;

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
                break;
            default:
                throw new ParseException("Unknown Class 0 instruction: " + mnemonic);
        }

        this.setEncoding(enc);
    }

    @Override
    public void run() {
        if(this.mnemonic.equals("hlt")) {
            SimulatorController.displaceRIP(-this.size);
        }
        if(this.mnemonic.equals("int")) {
            throw new UnsupportedOperationException("Interrupt management is not yet supported.");
        }
    }

    @Override
    public String toString() {
        String insn = this.mnemonic;
        if(this.mnemonic.equals("int"))
            insn += " " + this.ivn;
        return insn;
    }
}
