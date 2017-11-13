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
        
        byte di = 0b00000000;
        byte diImm = 0b00000000;
        byte mem = 0b00000000;
        byte ss = (byte)transfer_size;
        byte ds = (byte)transfer_size;
        
        encoding[1] = (byte) (ss | ds | diImm | di | mem);
        
        switch(mnemonic) {
            case "in":
                this.type = 0x00;
                break;
            case "out":
                this.type = 0x01;
                break;
            case "ins":
                this.type = 0x02;
                break;
            case "outs":
                this.type = 0x03;
                break;
            default:
                throw new RuntimeException("Unknown Class 7 instruction: " + mnemonic);
        }
        
        encoding[0] = (byte)(encoding[0] | this.type);
        this.setValue(encoding);
        
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
        String mnem = this.mnemonic;
        
        
            switch (this.transfer_size) {
                case 8:
                    mnem = mnem.concat("b");
                    break;
                case 16:
                    mnem = mnem.concat("w");
                    break;
                case 32:
                    mnem = mnem.concat("l");
                    break;
                default:
                    throw new RuntimeException("Implicit Size of Class 1 instruction is set, yet to a wrong value");

        }
        return mnem;     
    }

}