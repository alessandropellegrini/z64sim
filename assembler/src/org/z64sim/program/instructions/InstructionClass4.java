/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import org.z64sim.program.Instruction;
import org.z64sim.program.muops.MicroOperation;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class InstructionClass4 extends Instruction {

    private final byte bit;
    private final byte val;

    public InstructionClass4(String mnemonic) {
        super(mnemonic);
        this.bit = 0; /* depends on the mnemonic */
        this.val = 0; /* depends on the mnemonic: 0 for clear, 1 for set */

        if (mnemonic.equals("clc")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_CF_R));
        } else if (mnemonic.equals("clp")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_PF_R));
        } else if (mnemonic.equals("clz")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_ZF_R));
        } else if (mnemonic.equals("cls")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_SF_R));
        } else if (mnemonic.equals("cli")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_IF_R));
        } else if (mnemonic.equals("cld")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_DF_R));
        } else if (mnemonic.equals("clo")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_OF_R));
        } else if (mnemonic.equals("cstc")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_CF_R));
        } else if (mnemonic.equals("stp")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_PF_S));
        } else if (mnemonic.equals("stz")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_ZF_S));
        } else if (mnemonic.equals("sts")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_SF_S));
        } else if (mnemonic.equals("sti")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_IF_S));
        } else if (mnemonic.equals("std")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_DF_S));
        } else if (mnemonic.equals("sto")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_OF_S));
        }
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getRepresentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
