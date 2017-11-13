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

    private final ArrayList<MicroOperation> microOps = new ArrayList<>();
    protected final String mnemonic;
    protected final byte clas;
    protected byte type;

    public Instruction(String mnemonic, int clas) {
        this.mnemonic = mnemonic;
        this.clas = (byte)clas;
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

    public byte getClas() {
        return this.clas;
    }

    public abstract void run();

}
