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
        
        if(mnemonic.equals("add")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(18,20,9);
                this.addMicroOperation(muop1);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(18,5,9);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(6,7);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(5,8);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("sub")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(28,20,9);
                this.addMicroOperation(muop1);
            }
            else if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,5);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,6);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(28,6,9);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(5,7);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(6,8);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("adc")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(29,20,9);
                this.addMicroOperation(muop1);               
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(29,5,9);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(6,7);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(5,8);
                this.addMicroOperation(muop2);                
            }
        }else if(mnemonic.equals("sbb")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(30,20,9);
                this.addMicroOperation(muop1);                
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,5);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,6);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(30,6,9);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(5,7);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(6,8);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("cmp")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(28,31,9);
                this.addMicroOperation(muop1);
            }
            else if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,5);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,6);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(28,31,9);
                this.addMicroOperation(muop);
            }
        }else if(mnemonic.equals("test")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(32,31,9);
                this.addMicroOperation(muop1);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(32,31,9);
                this.addMicroOperation(muop);
            }
        }else if(mnemonic.equals("neg")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(6,7);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(33,8,9);
                this.addMicroOperation(muop1);
            }
        }else if(mnemonic.equals("and")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(32,20,9);
                this.addMicroOperation(muop1);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(6,7);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,10);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(10,6);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(32,5,9);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(5,8);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("or")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(34,20,9);
                this.addMicroOperation(muop1);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(6,7);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,10);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(10,6);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(34,5,9);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(5,8);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("xor")){
            if(source instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(20,5);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(35,20,9);
                this.addMicroOperation(muop1);
            }if(destination instanceof OperandMemory){
                OperandMemory o = (OperandMemory)d;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);   
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 );
                    addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(15,7);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(8,10,17,15);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(19,5);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(6,7);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(8,10);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(10,6);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(35,5,9);
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(5,8);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("not")){
            if(s instanceof OperandMemory){
                OperandMemory o = (OperandMemory)s;
                if(o.getIndex() != -1){
                    MicroOperation muop = new MicroOperation(1,6);  
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(13,6,14 ); 
                    this.addMicroOperation(muop1);
                }if(o.getBase() != -1){
                    MicroOperation muop = new MicroOperation(2,5);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }if(o.getDisplacement() != -1){
                    MicroOperation muop = new MicroOperation(11,6);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(18,6);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(6,7);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(36,8,9);
                this.addMicroOperation(muop1);
            }
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
