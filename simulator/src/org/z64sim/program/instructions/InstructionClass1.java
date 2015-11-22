/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

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
        
        // First byte is the opcode
        byte[] encoding = {0b00010000, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                           0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        
        // Set the size in memory
        if(s instanceof OperandImmediate && s.getSize() == 8)
            this.setSize(16);
        else
            this.setSize(8);

        switch (mnemonic) {
            case "mov":
                
                this.type = 0x00;
               
                if (source instanceof OperandMemory) {
                    OperandMemory o = (OperandMemory) s;
                    if (o.getIndex() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.I, MicroOperation.TEMP2));
                        this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T, MicroOperation.TEMP2, MicroOperation.SR_UPDATE0));
                    }
                    if (o.getBase() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.B, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    if (o.getDisplacement() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.D));
                }   if (destination instanceof OperandMemory) {
                    OperandMemory o = (OperandMemory) d;
                    if (o.getIndex() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.I, MicroOperation.TEMP2));
                        this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T, MicroOperation.TEMP2, MicroOperation.SR_UPDATE0));
                    }
                    if (o.getBase() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.B, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    if (o.getDisplacement() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    if (source instanceof OperandImmediate) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMAR));
                        this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.TEMP1, MicroOperation.RIP8, MicroOperation.RIP));
                    }
                    if (source instanceof OperandRegister) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.S, MicroOperation.TEMP1));
                    }
                    this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.TEMP1, MicroOperation.EMARm));
                }   break;
            case "movsX": // TODO
                encoding[0] = (byte)(encoding[0] | 0x01);
                this.type = 0x01;
                break;
            case "movzX": // TODO
                encoding[0] = (byte)(encoding[0] | 0x02);
                this.type = 0x02;
                break;
            case "lea":
                encoding[0] = (byte)(encoding[0] | 0x03);
                this.type = 0x03;
                if (source instanceof OperandMemory) {
                    OperandMemory o = (OperandMemory) s;
                    if (o.getIndex() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.I, MicroOperation.TEMP2));
                        this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T, MicroOperation.TEMP2, MicroOperation.SR_UPDATE0));
                    }
                    if (o.getBase() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.B, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    if (o.getDisplacement() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.D));
                }   break;
            case "push":
                encoding[0] = (byte)(encoding[0] | 0x04);
                this.type = 0x04;
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_SUB_X, MicroOperation.RSP));
                if (source instanceof OperandMemory) {
                    OperandMemory o = (OperandMemory) s;
                    if (o.getIndex() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.I, MicroOperation.TEMP2));
                        this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T, MicroOperation.TEMP2, MicroOperation.SR_UPDATE0));
                    }
                    if (o.getBase() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.B, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    if (o.getDisplacement() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    this.addMicroOperation(new MicroOperation(MicroOperation.TEMP2, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMAR));
                } else if (source instanceof OperandRegister) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                    this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_SUB_X, MicroOperation.RSP));
                    this.addMicroOperation(new MicroOperation(MicroOperation.S, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMARm));
                }   break;
            case "pop":
                encoding[0] = (byte)(encoding[0] | 0x05);
                this.type = 0x05;
                if (source instanceof OperandMemory) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    OperandMemory o = (OperandMemory) s;
                    if (o.getIndex() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.I, MicroOperation.TEMP2));
                        this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_T, MicroOperation.TEMP2, MicroOperation.SR_UPDATE0));
                    }
                    if (o.getBase() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.B, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    if (o.getDisplacement() != -1) {
                        this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.TEMP1));
                        this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD, MicroOperation.TEMP2));
                    }
                    this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMARm));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                    this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_X, MicroOperation.RSP));
                    
                } else if (source instanceof OperandRegister) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMAR, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.S));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                    this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_X, MicroOperation.RSP));
                }   break;
            case "pushf":
                encoding[0] = (byte)(encoding[0] | 0x06);
                this.type = 0x06;
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_SUB_X, MicroOperation.RSP));
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMAR));
                break;
            case "popf":
                encoding[0] = (byte)(encoding[0] | 0x07);
                this.type = 0x07;
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.FLAGS));
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_X, MicroOperation.RSP));
                break;
            case "movs":
                encoding[0] = (byte)(encoding[0] | 0x08);
                this.type = 0x08;
                this.addMicroOperation(new MicroOperation(MicroOperation.RSI, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.RDI, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0, MicroOperation.EMARm));
                break;
            case "stos":
                encoding[0] = (byte)(encoding[0] | 0x09);
                this.type = 0x09;
                this.addMicroOperation(new MicroOperation(MicroOperation.RDI, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.RAX, MicroOperation.EMARm));
                break;
            default:
                throw new RuntimeException("Unknown Class 1 instruction: " + mnemonic);
        }
        
        this.setValue(encoding);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String toString() {
        String mnem = this.mnemonic;
        
        if(this.implicitSize != -1) {
            switch(this.implicitSize) {
                case 8:
                    mnem = mnem.concat("b");
                    break;
                case 16:
                    mnem = mnem.concat("w");
                    break;
                case 32:
                    mnem = mnem.concat("l");
                    break;
                case 64:
                    mnem = mnem.concat("q");
                    break;
                default:
                    throw new RuntimeException("Implicit Size of Class 1 instruction is set, yet to a wrong value");
                    
            }
        }
        
        if(this.source != null) {
            mnem = mnem.concat(this.source.toString());
        }
        
        if(this.destination != null) {
            mnem = mnem.concat(", " + this.destination.toString());
        }
        
        return mnem;
    }

}
