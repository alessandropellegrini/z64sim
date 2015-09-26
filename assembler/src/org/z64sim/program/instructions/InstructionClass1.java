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
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation("I", "TEMP2");
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation("SHIFTER_OUT[SX, T]", "TEMP2","SR_UPDATE = 0");
                    addMicroOperation(muop1);
                }if(s.getBase() != -1){
                    
                }if(s.getDisplacement() != -1){
                    
                }
                
            }if(d.getClass().isInstance(OperandMemory.class)){
                if(d.getIndex() != -1){
                    
                }if(d.getBase() != -1){
                    
                }if(d.getDisplacement() != -1){
                    
                }if(s.getClass().equals(OperandImmediate.class)){
                    
                }if(s.getClass().equals(OperandRegister.class)){
                    
                }
                
            }
        }else if(mnemonic.equals("lea")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
                    addMicroOperation(muop1);
                }if(s.getBase() != -1){
                    
                }if(s.getDisplacement() != -1){
                    
                }
            }
        }else if(mnemonic.equals("push")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
                    addMicroOperation(muop1);
                }if(s.getBase() != -1){
                    
                }if(s.getDisplacement() != -1){
                    
                }
            }
        }else if(mnemonic.equals("pop")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
                    addMicroOperation(muop1);
                }if(s.getBase() != -1){
                    
                }if(s.getDisplacement() != -1){
                    
                }
            }
        }else if(mnemonic.equals("pushf")){
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation();
            addMicroOperation(muop1);
              
        }else if(mnemonic.equals("popf")){
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop1 = new MicroOperation();
            addMicroOperation(muop1);
        }else if(mnemonic.equals("movs")){
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
            MicroOperation muop = new MicroOperation();
            addMicroOperation(muop);
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
