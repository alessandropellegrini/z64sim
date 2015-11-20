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

    private long address = -1; // This must be set by the Program class when placing in memory view
    private int size = -1; // This must be set by the constructors of the subclasses

    public MemoryElement() {
    }

    public long getAddress() {
        return address;
    }
    
    public void setAddress(long address) throws Exception {
        if(this.address != -1)
               throw new Exception("Address has already been set");
        
        this.address = address;
    }

    public int getSize() {
        return size;
    }
    
    public void setSize(int size) throws RuntimeException {
        if(this.size != -1)
               throw new RuntimeException("Size has already been set");
        
        this.size = size;
    }
    
    public abstract void update();
}
