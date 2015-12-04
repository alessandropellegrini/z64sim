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
public class InstructionClass7 extends Instruction {

    private final int transfer_size; // The size of a data transfer

    public InstructionClass7(String mnemonic, int size) {
        super(mnemonic, 7);
        this.transfer_size = size;

        byte[] encoding = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        switch(mnemonic) {
            case "in":
                encoding[0] |= (byte)0b10000000;
                this.type = 0x00;
                break;
            case "out":
                encoding[0] |= (byte)0b10000001;
                this.type = 0x01;
                break;
            case "ins":
                encoding[0] |= (byte)0b10000010;
                this.type = 0x02;
                break;
            case "outs":
                encoding[0] |= (byte)0b10000011;
                this.type = 0x03;
                break;
            default:
                throw new RuntimeException("Unknown Class 7 instruction: " + mnemonic);
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