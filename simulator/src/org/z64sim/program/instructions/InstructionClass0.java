/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import javax.swing.JOptionPane;
import org.z64sim.memory.Memory;
import org.z64sim.program.Instruction;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class InstructionClass0 extends Instruction {

    int idn;

    public InstructionClass0(String mnemonic, int idn) {
        super(mnemonic, 0);
        this.idn = idn;

        // Set the size in memory
        this.setSize(8);

        byte enc[] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        switch (mnemonic) {
            case "hlt":
                enc[0] = 0x01;
                this.type = 0x01;
                break;
            case "nop":
                enc[0] = 0x02;
                this.type = 0x02;
                break;
            case "int":
		enc[0] = 0x03;
		this.type = 0x03;
            default:
                throw new RuntimeException("Unknown Class 0 instruction: " + mnemonic);
        }

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
        
      //  JOptionPane.showMessageDialog(null, "" + address + " - " + b[0] + " " + b[1]);
        
        String instr = "";
        switch (b[0]){
            case 0x00:
                instr+= "";
                break;
            case 0x01:
                instr+= "hlt";
                break;
            case 0x02:
                instr+= "nop";
                break;
            case 0x03:
                instr+= "int";
                break;
            default:
                throw new RuntimeException("Unkown instruction type");
                
        }
        return instr;
    }
    
}
