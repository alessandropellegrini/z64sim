/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory;

import org.z64sim.memory.MemoryElement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class MemoryElementSearchable extends MemoryElement {

    public MemoryElementSearchable(long address) {
        try {
            this.setAddress(address);
        } catch (Exception ex) {
            Logger.getLogger(MemoryElementSearchable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void update() {
        throw new UnsupportedOperationException("Cannot update a searchable object");
    }

}
