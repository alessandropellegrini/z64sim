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
public class InstructionClass5 extends Instruction {

    private final Operand target;

    public InstructionClass5(String mnemonic, Operand t) {
        super(mnemonic, 5);
        this.target = t;

        // Set the size in memory
        this.setSize(8);

        switch (mnemonic) {
            case "jmp":
                if (target instanceof OperandMemory) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.IR031, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                } else if (target instanceof OperandRegister) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.R, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                }
                break;
            case "call":
                if (target instanceof OperandMemory) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                    this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_SUB_8, MicroOperation.RSP));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMARm));
                    this.addMicroOperation(new MicroOperation(MicroOperation.M, MicroOperation.RIP));
                } else if (target instanceof OperandRegister) {
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                    this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_SUB_8, MicroOperation.RSP));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RIP, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.EMARm));
                    this.addMicroOperation(new MicroOperation(MicroOperation.R, MicroOperation.EMAR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                    this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                }
                break;
            case "ret":
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_8, MicroOperation.RSP));
                break;
            case "iret":
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.FLAGS));
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_8, MicroOperation.RSP));
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.EMAR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMARm, MicroOperation.EMDR));
                this.addMicroOperation(new MicroOperation(MicroOperation.EMDR, MicroOperation.RIP));
                this.addMicroOperation(new MicroOperation(MicroOperation.RSP, MicroOperation.TEMP1));
                this.addMicroOperation(new MicroOperation(MicroOperation.ALU_OUT_ADD_8, MicroOperation.RSP));
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        String mnem = this.mnemonic;

        if (this.target != null) {
            if (this.target instanceof OperandRegister) {
                mnem = mnem.concat("*");
            }
            mnem = mnem.concat(this.target.toString());
        }

        return mnem;
    }

    public Operand getTarget() {
        return this.target;
    }

}
