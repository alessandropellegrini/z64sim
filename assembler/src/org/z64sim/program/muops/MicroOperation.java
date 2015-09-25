/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// TODO: this should be rather moved to the simulator package, when implemented
package org.z64sim.program.muops;
import org.z64sim.program.instructions.*;
import java.util.ArrayList;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class MicroOperation {
    private final Operand s;
    private final Operand d;
    private final String mnemonic;
    
    
    public MicroOperation(String mnemonic, Operand s, Operand d){
        this.s = s;
        this.d = d;
        this.mnemonic = mnemonic;
        if(mnemonic.equals("HALT")){
            
        }else if(mnemonic.equals("NOP")){
            
        }else if(mnemonic.equals("MOV")){
            if(s.getClass().equals(OperandMemory.class)){
                if(s.getIndex() != -1){
                    
                }if(s.getBase() != -1){
                    
                }if(s.getDisplacement() != -1){
                    
                }
                
            }if(d.getClass().equals(OperandMemory.class)){
                if(d.getIndex() != -1){
                    
                }if(d.getBase() != -1){
                    
                }if(d.getDisplacement() != -1){
                    
                }if(s.getClass().equals(OperandImmediate.class)){
                    
                }if(s.getClass().equals(OperandRegister.class)){
                    
                }
                
            }
        }else if(mnemonic.equals("LEA")){
            
        }else if(mnemonic.equals("PUSH")){
            
        }else if(mnemonic.equals("POP")){
            
        }else if(mnemonic.equals("PUSHF")){
            
        }else if(mnemonic.equals("POPF")){
            
        }else if(mnemonic.equals("MOVS")){
            
        }else if(mnemonic.equals("STOS")){
            
        }else if(mnemonic.equals("ADD")){
            
        }else if(mnemonic.equals("ADD")){
            
        }else if(mnemonic.equals("SUB")){
            
        }else if(mnemonic.equals("ADC")){
            
        }else if(mnemonic.equals("SBB")){
            
        }else if(mnemonic.equals("CMP")){
            
        }else if(mnemonic.equals("TEST")){
            
        }else if(mnemonic.equals("NEG")){
            
        }else if(mnemonic.equals("AND")){
            
        }else if(mnemonic.equals("OR")){
            
        }else if(mnemonic.equals("XOR")){
            
        }else if(mnemonic.equals("NOT")){
            
        }
    }
                
}
