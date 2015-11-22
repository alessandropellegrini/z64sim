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
public class InstructionClass6 extends Instruction {

    private final byte bit;
    private final OperandMemory target;

    public InstructionClass6(String mnemonic, OperandMemory t) {
        super(mnemonic);
        this.bit = 0; /* depends on the mnemonic */

        this.target = t;

        // Set the size in memory
        this.setSize(8);

        switch (mnemonic) {
            case "jc":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_CF_1));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jp":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_PF_1));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jz":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_ZF_1));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "js":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_SF_1));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jo":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_OF_1));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jnc":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_CF_0));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jnp":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_PF_0));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jnz":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_ZF_0));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jns":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_SF_0));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            case "jno":
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_OF_0));
                this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                break;
            default:
                throw new RuntimeException("Unknown Class 6 instruction: " + mnemonic);
        }
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        String mnem = this.mnemonic;
        mnem = mnem.concat(this.target.toString());
        return mnem;
    }

}
