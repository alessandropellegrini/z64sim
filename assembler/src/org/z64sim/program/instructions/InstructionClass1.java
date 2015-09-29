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
                    MicroOperation muop = new MicroOperation(1,6);   //1 sarebbe I ; 6 = TEMP2
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }
            MicroOperation muop = new MicroOperation(12,6);
            super.addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation(8,10);
            super.addMicroOperation(muop1);
            MicroOperation muop2 = new MicroOperation(10,20);
            super.addMicroOperation(muop2);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    super.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    super.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(12,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(5,8);
                super.addMicroOperation(muop1);
            }
        }else if(mnemonic.equals("lea")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }
            }            
        }else if(mnemonic.equals("push")){
            MicroOperation muopa = new MicroOperation(21,5);  
            super.addMicroOperation(muopa);
            MicroOperation muopb = new MicroOperation(22,21);  
            super.addMicroOperation(muopb);
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(6,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,10);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(21,7);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(10,8);
                super.addMicroOperation(muop3);
            }else if(source instanceof OperandRegister){
                MicroOperation muop = new MicroOperation(21,5);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(22,21);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(19,10);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(21,7);
                super.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(10,8);
                super.addMicroOperation(muop4);
            }    
        }else if(mnemonic.equals("pop")){
            if(source instanceof OperandMemory){
                MicroOperation muopa = new MicroOperation(21,7);
                super.addMicroOperation(muopa);
                MicroOperation muopb = new MicroOperation(8,10);
                super.addMicroOperation(muopb);
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    super.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(12,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(10,8);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(21,5);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(23,21);
                super.addMicroOperation(muop3);
            }else if(source instanceof OperandMemory){
                MicroOperation muop = new MicroOperation(21,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,10);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(10,19);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(21,5);
                super.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(23,21);
                super.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("pushf")){
                MicroOperation muop = new MicroOperation(21,5);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(22,21);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(24,10);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(21,7);
                super.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(10,8);
                super.addMicroOperation(muop4);
              
        }else if(mnemonic.equals("popf")){
                MicroOperation muop = new MicroOperation(21,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,10);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(10,24);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(21,7);
                super.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(10,8);
                super.addMicroOperation(muop4);
        }else if(mnemonic.equals("movs")){
                MicroOperation muop = new MicroOperation(25,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,6);
                super.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(26,7);
                super.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(23,21);
                super.addMicroOperation(muop3);
        }else if(mnemonic.equals("stos")){
                MicroOperation muop = new MicroOperation(26,7);
                super.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(27,8);
                super.addMicroOperation(muop1);
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
