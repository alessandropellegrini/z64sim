/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import java.util.ArrayList;
import org.z64sim.program.Instruction;
import org.z64sim.program.subtasks.MicroOperation;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class InstructionClass4 extends Instruction {
    
    private final byte bit;
    private final byte val;

    public InstructionClass4(int address, int size, String mnemonic, ArrayList<MicroOperation> ops, byte bit, byte val) {
        super(address, size, mnemonic, ops);
        this.bit = bit;
        this.val = val;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
