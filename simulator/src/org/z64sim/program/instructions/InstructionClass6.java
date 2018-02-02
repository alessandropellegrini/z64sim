/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import org.z64sim.memory.Memory;
import org.z64sim.program.Instruction;
import org.z64sim.simulator.Register;

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
        enc[0] = 0b01100000;
        // Set the size in memory
        this.setSize(8);
        byte dest = 0b00000000;
        byte sour = 0b00000000;
        if(t instanceof OperandMemory){
            dest = (byte)(((OperandMemory)target).getBase());
        }
        
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
        
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    public static String disassemble(int address) {
        byte b[] = new byte[8];
        for(int i = 0; i < 8; i++) {
            b[i] = Memory.getProgram().program[address + i];
        }
        String instr="";
        switch (b[0]){
            case 0x60:
                instr+="jc";
                break;
            case 0x61:
                instr+="jp";
                break;
            case 0x62:
                instr+="jz";
                break;
            case 0x63:
                instr+="js";
                break;
            case 0x64:
                instr+="jo";
                break;
            case 0x65:
                instr+="jnc";
                break;
            case 0x66:
                instr+="jnp";
                break;
            case 0x67:
                instr+="jnz";
                break;
            case 0x68:
                instr+="jns";
                break;
            case 0x69:
                instr+="jno";
                break;
            default:
                throw new RuntimeException("Unkown instruction type");
        }
        
        int sizeInt = 0;
        
        switch(byteToBits(b[1],5,4)){
            case 0:
                sizeInt = 8;
                break;
            case 1:
                sizeInt = 16;
                break;
            case 2:
                sizeInt = 32;
                break;
            case 3:
                sizeInt = 64;
                break;
            default:
                throw new RuntimeException("Wrong value size");
        }
       
        int destRegister = byteToBits(b[3],3,0);
        String dest_Reg = Register.getRegisterName(destRegister, sizeInt);
        instr+=" "+dest_Reg;


        return instr;
        
    }

    public Operand getTarget() {
        return this.target;
    }
    
}
