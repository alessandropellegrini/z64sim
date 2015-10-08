/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory;

import java.util.ArrayList;
import org.z64sim.program.MemoryElement;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory {
    
    public static ArrayList<MemoryElement> memoryMap;
    
    
    public static synchronized void updateMemory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
