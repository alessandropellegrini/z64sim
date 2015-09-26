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
            MicroOperation muop = new MicroOperation("RIP","EMAR");
            MicroOperation muop1 = new MicroOperation("(EMAR)","EMDR","RIP+8","RIP");
            MicroOperation muop2 = new MicroOperation("EMDR","IR");
        }else if(mnemonic.equals("nop")){
            
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
