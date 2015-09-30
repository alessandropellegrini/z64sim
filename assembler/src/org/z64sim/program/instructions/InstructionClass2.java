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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
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
                MicroOperation muop = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP1,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMARm);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("sub")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SUB,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
            }
            else if(destination instanceof OperandMemory){
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
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP1);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(MicroOperation.RIP,MicroOperation.EMAR);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.TEMP2,MicroOperation.RIP8,MicroOperation.RIP);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(MicroOperation.S,MicroOperation.TEMP2);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.ALU_OUT_SUB,MicroOperation.TEMP2,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMAR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMARm);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("adc")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADC,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
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
                MicroOperation muop = new MicroOperation(MicroOperation.ALU_OUT_ADC,MicroOperation.TEMP1,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMARm);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("sbb")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SBB,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
            }
            else if(destination instanceof OperandMemory){
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
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP1);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(MicroOperation.RIP,MicroOperation.EMAR);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.TEMP2,MicroOperation.RIP8,MicroOperation.RIP);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(MicroOperation.S,MicroOperation.TEMP2);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.ALU_OUT_SBB,MicroOperation.TEMP2,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMAR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMARm);
                this.addMicroOperation(muop2);
            }
        }else if(mnemonic.equals("cmp")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_SUB,MicroOperation.TEMP3,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
            }
            else if(destination instanceof OperandMemory){
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
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031,MicroOperation.TEMP1);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP1);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandImmediate){
                    MicroOperation muop = new MicroOperation(MicroOperation.RIP,MicroOperation.EMAR);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.TEMP2,MicroOperation.RIP8,MicroOperation.RIP);
                    this.addMicroOperation(muop1);
                }if(source instanceof OperandRegister){
                    MicroOperation muop = new MicroOperation(MicroOperation.S,MicroOperation.TEMP2);
                    this.addMicroOperation(muop);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.ALU_OUT_SUB,MicroOperation.TEMP3,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop);
            }
        }else if(mnemonic.equals("test")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_AND,MicroOperation.TEMP3,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
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
                MicroOperation muop = new MicroOperation(MicroOperation.ALU_OUT_AND,MicroOperation.TEMP3,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop);
            }
        }else if(mnemonic.equals("neg")){
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
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP2);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP1);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_NEG,MicroOperation.EMARm,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
            }
        }else if(mnemonic.equals("and")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_AND,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
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
                MicroOperation muop = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.TEMP2);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.ALU_OUT_AND,MicroOperation.TEMP1,MicroOperation.SR_UPDATE1 );
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMARm);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("or")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_OR,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
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
                MicroOperation muop = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.TEMP2);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.ALU_OUT_OR,MicroOperation.TEMP1,MicroOperation.SR_UPDATE1 );
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMARm);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("xor")){
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
                MicroOperation muop = new MicroOperation(MicroOperation.D,MicroOperation.TEMP1);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_XOR,MicroOperation.D,MicroOperation.SR_UPDATE1);
                this.addMicroOperation(muop1);
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
                MicroOperation muop = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.EMARm,MicroOperation.EMDR);
                this.addMicroOperation(muop1);
                MicroOperation muop2 = new MicroOperation(MicroOperation.EMDR,MicroOperation.TEMP2);
                this.addMicroOperation(muop2);
                MicroOperation muop3 = new MicroOperation(MicroOperation.ALU_OUT_XOR,MicroOperation.TEMP1,MicroOperation.SR_UPDATE1 );
                this.addMicroOperation(muop3);
                MicroOperation muop4 = new MicroOperation(MicroOperation.TEMP1,MicroOperation.EMARm);
                this.addMicroOperation(muop4);
            }
        }else if(mnemonic.equals("not")){
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
                    MicroOperation muop = new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP2);
                    this.addMicroOperation(muop);
                    MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_ADD,MicroOperation.TEMP1);
                    this.addMicroOperation(muop1);
                }
                MicroOperation muop = new MicroOperation(MicroOperation.TEMP2,MicroOperation.EMAR);
                this.addMicroOperation(muop);
                MicroOperation muop1 = new MicroOperation(MicroOperation.ALU_OUT_NOT,MicroOperation.EMARm,MicroOperation.SR_UPDATE1);
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
