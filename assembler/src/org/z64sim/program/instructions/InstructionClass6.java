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
public class InstructionClass6 extends Instruction {
    
    private final byte bit;
    private final OperandMemory target;

    public InstructionClass6(String mnemonic, OperandMemory t) {
        super(mnemonic);
        this.bit = 0; /* depends on the mnemonic */
        this.target = t;
        
        if(mnemonic.equals("jc")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_CF_1);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jp")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_PF_1);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jz")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_ZF_1);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("js")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_SF_1);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jo")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_OF_1);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jnc")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_CF_0);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jnp")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_PF_0);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jnz")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_ZF_0);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jns")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_SF_0);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
        }else if(mnemonic.equals("jno")){
            MicroOperation muop = new MicroOperation(MicroOperation.if_FLAGS_OF_0);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop2);
            MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
            this.addMicroOperation(muop3);
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
