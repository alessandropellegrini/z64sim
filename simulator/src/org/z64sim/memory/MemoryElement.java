/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory;

import java.util.Comparator;
import org.z64sim.program.ProgramException;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public abstract class MemoryElement implements Comparable<MemoryElement>, Comparator<MemoryElement> {

    private long address = -1; // This must be set by the Program class when placing in memory view
    private int size = -1; // This must be set by the constructors of the subclasses
    private byte[] value = null;

    public MemoryElement() {
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) throws ProgramException {
        if (this.address != -1) {
            throw new ProgramException("Address has already been set");
        }

        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) throws RuntimeException {
        if (this.size != -1) {
            throw new RuntimeException("Size has already been set");
        }

        this.size = size;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public abstract void update();

    @Override
    public int compareTo(MemoryElement u) {
        return (int) this.address - (int) u.getAddress();
    }

    @Override
    public int compare(MemoryElement u1, MemoryElement u2) {
        return (int) u1.getAddress() - (int) u2.getAddress();
    }

}
