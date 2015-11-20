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

    private final byte[] istruzione = new byte[8];
    private final byte opcode = 00000000;
    private final byte mode = 00000000;
    private final byte sib = 00000000;
    private final byte rm = 00000000;
    private final byte Displacement = 00000000;
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
        
        // Set the size in memory
        if(s instanceof OperandImmediate && s.getSize() == 8)
            this.setSize(16);
        else
            this.setSize(8);

        if (mnemonic.equals("mov")) {
            
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
            }
            if (destination instanceof OperandMemory) {
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
            }
            
        } else if (mnemonic.equals("lea")) {
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
            }

        } else if (mnemonic.equals("push")) {
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
            }
            
        } else if (mnemonic.equals("pop")) {
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
            }

        } else if (mnemonic.equals("pushf")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
            this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_SUB_X, MicroOperation.RSP));
            this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMAR));

        } else if (mnemonic.equals("popf")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.FLAGS));
            this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
            this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_X, MicroOperation.RSP));

        } else if (mnemonic.equals("movs")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.RSI, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.RDI, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SX_0, MicroOperation.EMARm));

        } else if (mnemonic.equals("stos")) {
            this.addMicroOperation(new MicroOperation(MicroOperation.RDI, MicroOperation.EMAR));
            this.addMicroOperation(new MicroOperation(MicroOperation.RAX, MicroOperation.EMARm));
        }
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getRepresentation() {
        return null; //To change body of generated methods, choose Tools | Templates.
    }

}
