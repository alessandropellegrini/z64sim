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
public class InstructionClass1 extends Instruction {
    
    private final byte opcode;
    private final byte mode;
    private final byte sib;
    private final byte rm;
    private final byte Displacement;
    private final Operand source;
    private final Operand destination;
    private final int implicitSize; // For instructions such as pushf, popf,
                                    // movs, stos, we store the size ('b', 'w',
                                    // 'l', 'q') here because they have no operands

    public InstructionClass1(String mnemonic, Operand s, Operand d, int implicitSize) {
        super(mnemonic);
        this.source = s;
        this.destination = d;
        this.implicitSize = implicitSize;
        
        
        if(mnemonic.equals("mov")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.I,MicroOperation.TEMP2);   //1 sarebbe I ; 6 = TEMP2
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T,MicroOperation.TEMP2,MicroOperation.SR_UPDATE0 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.B,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }
            MicroOperation muop = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0,MicroOperation.EMAR);
            this.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
            this.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.D);
            this.addMicroOperation(muop2);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.I,MicroOperation.TEMP2);   //1 sarebbe I ; 6 = TEMP2
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T,MicroOperation.TEMP2,MicroOperation.SR_UPDATE0 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.B,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(MicroOperation.RIP,MicroOperation.EMAR);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.TEMP1,MicroOperation.RIP8,MicroOperation.RIP);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(MicroOperation.S,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMARm);
                this.addMicroOperation(muop1);
            }
        }else if(mnemonic.equals("lea")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.I,MicroOperation.TEMP2);   //1 sarebbe I ; 6 = TEMP2
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T,MicroOperation.TEMP2,MicroOperation.SR_UPDATE0 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.B,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.D);
                this.addMicroOperation(muop2);
            }            
        }else if(mnemonic.equals("push")){
            MicroOperation muopa = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);  
            this.addMicroOperation(muopa);
            MicroOperation muopb = new MicroOperation(MicroOperation.ALU_OUT_SUB_X, MicroOperation.RSP);  
            this.addMicroOperation(muopb);
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.I,MicroOperation.TEMP2);   //1 sarebbe I ; 6 = TEMP2
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T,MicroOperation.TEMP2,MicroOperation.SR_UPDATE0 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.B,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.EMDR,MicroOperation.EMAR);
                this.addMicroOperation(muop3);
            }else if(source instanceof OperandRegister){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SUB_X,MicroOperation.RSP);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.S,MicroOperation.EMDR);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.EMDR,MicroOperation.EMARm);
                this.addMicroOperation(muop4);
            }    
        }else if(mnemonic.equals("pop")){
            if(source instanceof OperandMemory){
                MicroOperation muopa = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);
                this.addMicroOperation(muopa);
                MicroOperation muopb = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muopb);
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.I,MicroOperation.TEMP2);   //1 sarebbe I ; 6 = TEMP2
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T,MicroOperation.TEMP2,MicroOperation.SR_UPDATE0 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.B,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP2);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMDR,MicroOperation.EMARm);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.ALU_OUT_ADD_X,MicroOperation.RSP);
                this.addMicroOperation(muop3);
            }else if(source instanceof OperandRegister){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMAR,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.S);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.ALU_OUT_ADD_X, MicroOperation.RSP);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("pushf")){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SUB_X,MicroOperation.RSP);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.FLAGS,MicroOperation.EMAR);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.EMDR,MicroOperation.EMAR);
                this.addMicroOperation(muop4);
              
        }else if(mnemonic.equals("popf")){
                MicroOperation muop = new MicroOperation(MicroOperation.RSP,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.FLAGS);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.RSP,MicroOperation.TEMP1);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.ALU_OUT_ADD_X,MicroOperation.RSP);
                this.addMicroOperation(muop4);
        }else if(mnemonic.equals("movs")){
                MicroOperation muop = new MicroOperation(MicroOperation.RSI,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.TEMP2);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.RDI,MicroOperation.EMAR);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0,MicroOperation.EMARm);
                this.addMicroOperation(muop3);
        }else if(mnemonic.equals("stos")){
                MicroOperation muop = new MicroOperation(MicroOperation.RDI,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.RAX,MicroOperation.EMARm);
                this.addMicroOperation(muop1);
        }
    }
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getRepresentation() {
        return [opcode,rm]; //To change body of generated methods, choose Tools | Templates.
    }
    
}
