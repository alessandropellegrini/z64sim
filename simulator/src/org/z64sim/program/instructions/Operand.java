/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public abstract class Operand {

    protected final int size;

    public Operand(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    // toString() must be explicitly re-implemented
    public abstract String toString();
}
