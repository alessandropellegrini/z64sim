/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;

public class InstructionClass0 extends Instruction {

    private static final String[] MNEMONICS = {null, "hlt", "nop", "int"}; // index 0 unused

    final OperandImmediate ivn;

    public InstructionClass0(String mnemonic, OperandImmediate ivn) {
        super(mnemonic, 0);
        this.ivn = ivn;

        // Set the size in memory
        this.setSize(8);
    }

    public OperandImmediate getIvn() {
        return this.ivn;
    }

    public int getType() {
        for (int i = 0; i < MNEMONICS.length; i++) {
            if (this.mnemonic.equals(MNEMONICS[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void run() {
        if(this.mnemonic.equals("hlt")) {
            // Nothing to do — SimulatorController sets the halted flag and
            // RIP is left pointing past hlt so that interrupt return addresses
            // are correct.
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

    @Override
    protected byte[] encode() {
        byte[] buf = new byte[this.size];
        buf[0] = encodeOpcode(getType());
        if ("int".equals(this.mnemonic) && this.ivn != null) {
            buf[7] = (byte)(this.ivn.getValue() & 0xFF);
        }
        return buf;
    }
}
