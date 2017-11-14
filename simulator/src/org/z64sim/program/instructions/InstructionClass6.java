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
public class InstructionClass6 extends Instruction {

    private final byte bit;
    private final OperandMemory target;

    public InstructionClass6(String mnemonic, OperandMemory t) {
        super(mnemonic, 6);
        this.bit = 0; /* depends on the mnemonic */
        this.target = t;
        
        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        // Set the size in memory
        this.setSize(8);
        byte dest = 0b00000000;
        byte sour = 0b00000000;
        dest = (byte)(((OperandMemory)target).getBase());
        
        enc[3] = (byte) (sour | dest);

        switch (mnemonic) {
            case "jc":
                this.type = 0x00;
                break;
            case "jp":
                this.type = 0x01;
                break;
            case "jz":
                this.type = 0x02;
                break;
            case "js":
                this.type = 0x03;
                break;
            case "jo":
                this.type = 0x04;
                break;
            case "jnc":
                this.type = 0x05;
                break;
            case "jnp":
                this.type = 0x06;
                break;
            case "jnz":
                this.type = 0x07;
                break;
            case "jns":
                this.type = 0x08;
                 break;
            case "jno":
                this.type = 0x09;
                break;
            default:
                throw new RuntimeException("Unknown Class 6 instruction: " + mnemonic);
        }
        
        enc[0] = (byte)(enc[0] | this.type);
        this.setEncoding(enc);
        
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
        mnem = mnem.concat(this.target.toString());
        return mnem;
    }

    public Operand getTarget() {
        return this.target;
    }

}
