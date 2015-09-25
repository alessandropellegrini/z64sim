/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.z64sim.program.muops.MicroOperation;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public abstract class Instruction extends MemoryElement {
    
    private static final Logger logger = Logger.getLogger(Instruction.class.getName());
    private ArrayList<MicroOperation> microOps;
    private final String mnemonic;
    private byte type;
    
    public Instruction(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    void addMicroOperation(MicroOperation muop) {
        this.microOps.add(muop);
    }
    
    @Override
    public void update() {
        // Allowing to change the content of instructions allows for a sort of mutagen code.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public abstract void run();
    
    public abstract byte[] getRepresentation();
}
