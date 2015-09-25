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
public class InstructionClass6 extends Instruction {
    
    private final byte bit;
    private final OperandMemory target;

    public InstructionClass6(String mnemonic, byte b, OperandMemory t) {
        super(mnemonic);
        this.bit = b;
        this.target = t;
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
