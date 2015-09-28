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
            if(s instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   //1 sarebbe I ; 6 = TEMP2
                    super.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); //13 = SHIFTER_OUT[SX,T]; 14= SR_UPDATE=0
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    
                }if(o.getDisplacement() != -1){
                    
                }
                
            }if(d instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    
                }if(o.getBase() != -1){
                    
                }if(o.getDisplacement() != -1){
                    
                }if(s.getClass().equals(OperandImmediate.class)){
                    
                }if(s.getClass().equals(OperandRegister.class)){
                    
                }
                
            }
        }else if(mnemonic.equals("lea")){
            if(s instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    /*MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
                    addMicroOperation(muop1);*/
                }if(o.getBase() != -1){
                    
                }if(o.getDisplacement() != -1){
                    
                }
            }
        }else if(mnemonic.equals("push")){
            if(s instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    //MicroOperation muop = new MicroOperation();
                    //addMicroOperation(muop);
                    //MicroOperation muop1 = new MicroOperation();
                    //addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    
                }if(o.getDisplacement() != -1){
                    
                }
            }
        }else if(mnemonic.equals("pop")){
            if(s instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    //MicroOperation muop = new MicroOperation();
                    //addMicroOperation(muop);
                    //MicroOperation muop1 = new MicroOperation();
                    //addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    
                }if(o.getDisplacement() != -1){
                    
                }
            }
        }else if(mnemonic.equals("pushf")){
            //MicroOperation muop = new MicroOperation();
            //addMicroOperation(muop);
            //MicroOperation muop1 = new MicroOperation();
            //addMicroOperation(muop1);
              
        }else if(mnemonic.equals("popf")){
            /*MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation();
            addMicroOperation(muop1);*/
        }else if(mnemonic.equals("movs")){
            /*MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);*/
        }else if(mnemonic.equals("stos")){
            
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
