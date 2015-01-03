/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public abstract class MemoryElement {

    private final int address;
    private final int size;

    public MemoryElement(int address, int size) {
        this.address = address;
        this.size = size;
    }

    public int getAddress() {
        return address;
    }

    public int getSize() {
        return size;
    }

    public abstract void update();
}
