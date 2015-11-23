/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

import org.z64sim.memory.MemoryElement;
import java.util.ArrayList;
import org.z64sim.program.muops.MicroOperation;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public abstract class Instruction extends MemoryElement {
    
    private ArrayList<MicroOperation> microOps;
    protected final String mnemonic;
    protected byte type;
    
    public Instruction(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    // toString() must be explicitly re-implemented
    public abstract String toString();
    
    public void addMicroOperation(MicroOperation muop) {
        this.microOps.add(muop);
    }
    
    @Override
    public void update() {
        // Allowing to change the content of instructions allows for a sort of mutagen code.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public abstract void run();
    
}
