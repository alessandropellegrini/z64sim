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
public class InstructionClass0 extends Instruction {

    int idn;

    public InstructionClass0(String mnemonic, int idn) {
        super(mnemonic);
        this.idn = idn;

        // Set the size in memory
        this.setSize(8);
        
        byte encoding[] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        switch (mnemonic) {
            case "halt":
                encoding[0] = 0x01;
                this.type = 0x01;
                this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR, MicroOperation.RIP8, MicroOperation.RIP));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.IR));
                break;
            case "nop":
                encoding[0] = 0x02;
                this.type = 0x02;
                this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR, MicroOperation.RIP8, MicroOperation.RIP));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.IR));
                break;
            default:
                throw new RuntimeException("Unknown Class 0 instruction: " + mnemonic);
        }

        this.setValue(encoding);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return this.mnemonic;
    }

}
