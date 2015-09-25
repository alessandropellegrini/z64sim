/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.z64sim.assembler.ParseException;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Program {
    
    private final ArrayList<MemoryElement> memoryMap = new ArrayList<MemoryElement>();
    private final Map<String, MemoryElement> labels = new LinkedHashMap<String, MemoryElement>();
    private final Map<Integer, MemoryElement> idt = new LinkedHashMap<Integer, MemoryElement>();
    private long locationCounter = 0;
    
    public Program() {
    }
       
    public MemoryElement findLabelAddress(String name) {
        return labels.get(name);
    }
    
    // Returns false if a label with the same name alrady exists
    public boolean newLabel(String name, MemoryElement memory) {    
        if(labels.containsKey(name))
            return false;
        
        labels.put(name, memory);
        return true;
    }

    public long getLocationCounter() {
        return locationCounter;
    }

    public void setLocationCounter(long locationCounter) {
        this.locationCounter = locationCounter;
    }
    
    public void addMemoryElement(MemoryElement m) throws ParseException, Exception {
        
        // We must preserve the IDT
        if(this.locationCounter < 0x800)
            throw new ParseException("You are trying to put data/instructions over the IDT.");
        
        m.setAddress(this.locationCounter);
        memoryMap.add(m);
        this.locationCounter += m.getSize();
    }
    
    public boolean newDriver(Integer idn, MemoryElement memory) {    
        if(idt.containsKey(idn))
            return false;
        
        idt.put(idn, memory);
        return true;
    }

}
