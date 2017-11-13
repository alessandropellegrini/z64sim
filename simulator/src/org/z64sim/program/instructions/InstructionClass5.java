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
        
        byte[] encoding = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        // Set the size in memory
        this.setSize(8);
        
        byte sour = 0b00000000;
        byte dest = 0b00000000;
        byte sd = 0b00000000;
        
        if(t!=null){
            switch (t.getSize()) {
                case 8:
                    sd = 0b00000000;
                    break;
                case 16:
                    sd = 0b00010000;
                    break;
                case 32:
                    sd = 0b00100000;
                    break;
                case 64:
                    sd = 0b00110000;
                    break;
            }
        }
        if(t!=null){
            if(t instanceof OperandMemory)
                dest = (byte) (((OperandMemory)t).getBase());
            else {
                dest = (byte)(((OperandRegister)t).getRegister());
            }
        }
        encoding[3] = (byte) (sour | dest);
        
        switch (mnemonic) {
            case "jmp": 
                if(t instanceof OperandMemory){
                    this.type = 0x00;
                }else{
                    this.type = 0x01;
                }
                break;
            case "call":
                if(t instanceof OperandMemory){
                    this.type = 0x02;
                }else{
                    this.type = 0x03;
                }
            case "ret":
                this.type = 0x04;
                break;
            case "iret":
                this.type = 0x05;
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }
        
        encoding[0] = (byte)(encoding[0] | this.type);
        this.setValue(encoding);
        System.out.println("dest:"+dest);
        System.out.println(t instanceof OperandMemory);
        System.out.println(t instanceof OperandRegister);
        System.out.println(t instanceof OperandImmediate);
        System.out.println(t == null);
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
