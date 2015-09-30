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
public class InstructionClass4 extends Instruction {
    
    private final byte bit;
    private final byte val;

    public InstructionClass4(String mnemonic) {
        super(mnemonic);
        this.bit = 0; /* depends on the mnemonic */
        this.val = 0; /* depends on the mnemonic: 0 for clear, 1 for set */
        
        if(mnemonic.equals("clc")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_CF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("clp")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_PF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("clz")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_ZF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("cls")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_SF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("cli")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_IF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("cld")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_DF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("clo")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_OF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("cstc")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_CF_R);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("stp")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_PF_S);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("stz")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_ZF_S);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("sts")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_SF_S);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("sti")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_IF_S);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("std")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_DF_S);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
        }else if(mnemonic.equals("sto")){
            MicroOperation muop = new MicroOperation(MicroOperation.FLAGS_OF_S);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
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
