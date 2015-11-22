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
public class InstructionClass4 extends Instruction {

    private final byte bit;
    private final byte val;

    public InstructionClass4(String mnemonic) {
        super(mnemonic);
        this.bit = 0; /* depends on the mnemonic */
        this.val = 0; /* depends on the mnemonic: 0 for clear, 1 for set */

        // Set the size in memory
        this.setSize(8);

        // Will be initialized in the switch case, as well the class
        byte[] encoding = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        switch (mnemonic) {
            case "clc":
                encoding[0] = 0b01000000;
                this.type = 0x00;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_CF_R));
                break;
            case "clp":
                encoding[0] = 0b01000001;
                this.type = 0x01;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_PF_R));
                break;
            case "clz":
                encoding[0] = 0b01000010;
                this.type = 0x02;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_ZF_R));
                break;
            case "cls":
                encoding[0] = 0b01000011;
                this.type = 0x03;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_SF_R));
                break;
            case "cli":
                encoding[0] = 0b01000100;
                this.type = 0x04;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_IF_R));
                break;
            case "cld":
                encoding[0] = 0b01000101;
                this.type = 0x05;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_DF_R));
                break;
            case "clo":
                encoding[0] = 0b01000110;
                this.type = 0x06;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_OF_R));
                break;
            case "stc":
                encoding[0] = 0b01000111;
                this.type = 0x07;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_CF_R));
                break;
            case "stp":
                encoding[0] = 0b01001000;
                this.type = 0x08;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_PF_S));
                break;
            case "stz":
                encoding[0] = 0b01001001;
                this.type = 0x09;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_ZF_S));
                break;
            case "sts":
                encoding[0] = 0b01001010;
                this.type = 0x0a;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_SF_S));
                break;
            case "sti":
                encoding[0] = 0b01001011;
                this.type = 0x0b;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_IF_S));
                break;
            case "std":
                encoding[0] = 0b01001100;
                this.type = 0x0c;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_DF_S));
                break;
            case "sto":
                encoding[0] = 0b01001101;
                this.type = 0x0d;
                this.addMicroOperation(new MicroOperation(MicroOperation.FLAGS_OF_S));
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }

        this.setValue(encoding);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return this.mnemonic;
    }

}
