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

    private final byte[] istruzione = new byte[8];
    private byte opcode = 00000000;
    private byte mode = 00000000;
    private byte sib = 00000000;
    private byte rm = 00000000;
    private byte displacementa = 00000000;
    private byte displacementb = 00000000;
    private byte displacementc = 00000000;
    private byte displacementd = 00000000;

    public InstructionClass0(String mnemonic, int idn) {
        super(mnemonic);
        this.idn = idn;
        
        // Set the size in memory
        this.setSize(8);

        if (mnemonic.equals("halt")) {
            opcode += 1;
            this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR, MicroOperation.RIP8, MicroOperation.RIP));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.IR));
            
        } else if (mnemonic.equals("nop")) {
            opcode += 10;
            this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR, MicroOperation.RIP8, MicroOperation.RIP));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.IR));
        }
        istruzione[0] = opcode;
        istruzione[1] = mode;
        istruzione[2] = sib;
        istruzione[3] = rm;
        istruzione[4] = displacementa;
        istruzione[5] = displacementb;
        istruzione[6] = displacementc;
        istruzione[7] = displacementd;

    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getRepresentation() {
        return istruzione; //To change body of generated methods, choose Tools | Templates.
    }

}
