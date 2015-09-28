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
public class InstructionClass2 extends Instruction {

    private final Operand source;
    private final Operand destination;

    public InstructionClass2(String mnemonic, Operand s, Operand d) {
        super(mnemonic);
        this.source = s;
        this.destination = d;
        /*
        if(mnemonic.equals("add")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("sub")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("adc")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("sbb")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("cmp")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("test")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("neg")){
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
        }else if(mnemonic.equals("and")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("or")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("xor")){
            if(s.getClass().isInstance(OperandMemory.class)){
                if(source.getIndex() != -1){
                    MicroOperation muop = new MicroOperation();
                    addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation();
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
        }else if(mnemonic.equals("not")){
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
        }*/
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
