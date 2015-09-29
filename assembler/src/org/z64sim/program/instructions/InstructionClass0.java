/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import java.util.ArrayList;
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
        
        if(mnemonic.equals("halt")){
            MicroOperation muop = new MicroOperation(15,7);
            super.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(8,10,17,15);
            super.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(10,16);
            super.addMicroOperation(muop2);
        }else if(mnemonic.equals("nop")){
            MicroOperation muop = new MicroOperation(15,7);
            super.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(8,10,17,15);
            super.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(10,16);
            super.addMicroOperation(muop2);
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
