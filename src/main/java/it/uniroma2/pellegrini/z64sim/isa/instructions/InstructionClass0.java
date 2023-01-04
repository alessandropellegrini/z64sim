/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
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
