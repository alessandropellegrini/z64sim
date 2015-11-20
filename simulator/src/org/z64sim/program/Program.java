/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Program {
    
    private final ArrayList<MemoryElement> memoryMap = new ArrayList<>();
    private final Map<String, MemoryElement> labels = new LinkedHashMap<>();
    private final Map<String, Long> equs = new LinkedHashMap<>();
    private final Map<Integer, MemoryElement> idt = new LinkedHashMap<>();
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
    
    public void addMemoryElement(MemoryElement m) throws ProgramException {
        
        // We must preserve the IDT
        if(this.locationCounter < 0x800)
            throw new ProgramException("You are trying to put data/instructions over the IDT.");
        
        try {
            m.setAddress(this.locationCounter);
        } catch(Exception e) {
            throw new ProgramException("Runtime error in the assembler:" + e.getMessage());
        }
        memoryMap.add(m);
        if(m.getSize() == -1)
            throw new RuntimeException("Adding a Data Element with unspecified size");
        this.locationCounter += m.getSize();
    }
    
    public boolean newDriver(Integer idn, MemoryElement memory) {    
        if(idt.containsKey(idn))
            return false;
        
        idt.put(idn, memory);
        return true;
    }
    
    public ArrayList<MemoryElement> getMemoryMap() {
        return this.memoryMap;
    }
    
    public void addEqu(String name, Long value) throws ProgramException {
        if( this.equs.containsKey(name) )
            throw new ProgramException("An EQU with the same name has already been defined");
        
        this.equs.put(name, value);
    }

}
