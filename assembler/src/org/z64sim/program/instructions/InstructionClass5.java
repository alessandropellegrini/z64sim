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
public class InstructionClass5 extends Instruction {
    
    private final Operand target;

    public InstructionClass5(String mnemonic, Operand t) {
        super(mnemonic);
        this.target = t;
        
        if(mnemonic.equals("jmp")){
            if(target instanceof OperandMemory){
                MicroOperation muop = new MicroOperation(MicroOperation.IR031,MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop2);
            }else if(target instanceof OperandRegister){
                MicroOperation muop = new MicroOperation(MicroOperation.R,MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("call")){
            if(target instanceof OperandMemory){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SUB_8, MicroOperation.RSP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.RIP, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.EMDR, MicroOperation.EMARm);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop4);
                MicroOperation muop5 = new MicroOperation(MicroOperation.M, MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop5);
            }else if(target instanceof OperandRegister){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SUB_8, MicroOperation.RSP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.RIP, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.EMDR, MicroOperation.EMARm);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop4);
                MicroOperation muop5 = new MicroOperation(MicroOperation.R, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop5);
                MicroOperation muop6 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop6);
                MicroOperation muop7 = new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop7);
            }
        }else if(mnemonic.equals("ret")){
                 MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.ALU_OUT_ADD_8, MicroOperation.RSP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop4);
        }else if(mnemonic.equals("iret")){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR, MicroOperation.FLAGS);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.ALU_OUT_ADD_8, MicroOperation.RSP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop4);
                MicroOperation muop5 = new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop5);
                MicroOperation muop6 = new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop6);
                MicroOperation muop7 = new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop7);
                MicroOperation muop8 = new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop8);
                MicroOperation muop9 = new MicroOperation(MicroOperation.ALU_OUT_ADD_8, MicroOperation.RSP);   //1 sarebbe I ; 6 = TEMP2
                this.addMicroOperation(muop9);
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
