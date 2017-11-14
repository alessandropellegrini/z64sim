/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import org.z64sim.program.Instruction;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class InstructionClass4 extends Instruction {

    private final byte bit;
    private byte val;

    public InstructionClass4(String mnemonic) {
        super(mnemonic, 4);
        this.bit = 0; /* depends on the mnemonic */
        this.val = 0; /* depends on the mnemonic: 0 for clear, 1 for set */

        // Set the size in memory
        this.setSize(8);

        // Will be initialized in the switch case, as well the class
        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        switch (mnemonic) {
            case "clc":
                this.type = 0x00;
                this.val = 0;
                break;
            case "clp":
                this.type = 0x01;
                this.val = 0;
                break;
            case "clz":            
                this.type = 0x02;
                this.val = 0;
                break;
            case "cls":
                this.type = 0x03;
                this.val = 0;
                break;
            case "cli":
                this.type = 0x04;
                this.val = 0;
                break;
            case "cld":
                this.type = 0x05;
                this.val = 0;
                break;
            case "clo":
                this.type = 0x06;
                this.val = 0;
                break;
            case "stc":
                this.type = 0x07;
                this.val = 1;
                break;
            case "stp":
                this.type = 0x08;
                this.val = 1;
                break;
            case "stz":
                this.type = 0x09;
                this.val = 1;
                break;
            case "sts":
                this.type = 0x0a;
                this.val = 1;
                break;
            case "sti":
                this.type = 0x0b;
                this.val = 1;
                break;
            case "std":
                this.type = 0x0c;
                this.val = 1;
                break;
            case "sto":
                this.type = 0x0d;
                this.val = 1;
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }
        
        enc[0] = (byte)(enc[0] | this.type);
        this.setEncoding(enc);
        System.out.println(val);
        
        System.out.println("encoding[4]: "+encoding[4]);
        System.out.println("encoding[5]: "+encoding[5]);
        System.out.println("encoding[6]: "+encoding[6]);
        System.out.println("encoding[7]: "+encoding[7]);
        
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
