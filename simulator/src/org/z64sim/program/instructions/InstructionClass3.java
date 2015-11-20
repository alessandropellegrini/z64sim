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
public class InstructionClass3 extends Instruction {

    private final int places;
    private final OperandRegister reg;

    public InstructionClass3(String mnemonic, int p, OperandRegister r) {
        super(mnemonic);
        this.places = p;
        this.reg = r;
        
        // Set the size in memory
        this.setSize(8);


        if (mnemonic.equals("sal")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SAL_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SAL_K, MicroOperation.D));
        } else if (mnemonic.equals("shl")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SHL_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SHL_K, MicroOperation.D));
        } else if (mnemonic.equals("sar")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SAR_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SAR_K, MicroOperation.D));
        } else if (mnemonic.equals("shr")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SHR_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_SHR_K, MicroOperation.D));
        } else if (mnemonic.equals("rcl")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_RCL_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_RCL_K, MicroOperation.D));
        } else if (mnemonic.equals("rcr")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_RCR_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_RCR_K, MicroOperation.D));
        } else if (mnemonic.equals("rol")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_ROL_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_ROL_K, MicroOperation.D));
        } else if (mnemonic.equals("ror")) {
            if (this.places <= 0) {
                this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
                this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_ROR_RCX, MicroOperation.D));
            }
            this.addMicroOperation(new MicroOperation(MicroOperation.D, MicroOperation.TEMP2));
            this.addMicroOperation(new MicroOperation(MicroOperation.SHIFTER_OUT_ROR_K, MicroOperation.D));
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
