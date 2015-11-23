/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

import java.nio.ByteBuffer;
import org.z64sim.memory.MemoryElement;
import org.z64sim.memory.DataElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.openide.util.Exceptions;
import org.z64sim.memory.Memory;


/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Program {

    private final ArrayList<MemoryElement> mMap; // This field is needed only to serialize the object to a file
    private final Map<String, Long> labels = new LinkedHashMap<>();
    private final Map<String, Long> equs = new LinkedHashMap<>();
    private long locationCounter = 0;

    public Program() {

        // Only one program at a time can be loaded in memory
        Memory.wipeMemory();

        // Point to the Memory class
        this.mMap = Memory.getMemory();

        // Create element in the memory map for the IDT
        byte[] quadword = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        for(int i = 0; i < 256; i++) {
            DataElement el = new DataElement(quadword.clone());
            try {
                el.setAddress(i * 8);
            } catch (ProgramException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.mMap.add(el);
        }
    }

    public long findLabelAddress(String name) {
        Long address = labels.get(name);
        return (address != null ? address : -1);
    }

    // Returns false if a label with the same name alrady exists
    public boolean newLabel(String name, long address) {
        if(labels.containsKey(name))
            return false;

        labels.put(name, address);
        return true;
    }

    public long getLocationCounter() {
        return locationCounter;
    }

    public void setLocationCounter(long locationCounter) {
        this.locationCounter = locationCounter;
    }

    public void addInstructionToMemory(Instruction i) throws ProgramException {

        // We must preserve the IDT
        if(this.locationCounter < 0x800)
            throw new ProgramException("You are trying to put data/instructions over the IDT.");

        try {
            i.setAddress(this.locationCounter);
        } catch(Exception e) {
            throw new ProgramException("Runtime error in the assembler:" + e.getMessage());
        }

        // add() can be used only because we prevent instructions to appear before .data section,
        // so they can be directly pushed at the end of the ArrayList
        this.mMap.add(i);

        if(i.getSize() == -1)
            throw new RuntimeException("Adding an instruction with unspecified size");

        this.locationCounter += i.getSize();
    }

    public void newDriver(Integer idn, long address) {

        MemoryElement entry = Memory.getElementFromAddress(idn * 8);
        byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(address).array();
        entry.setValue(bytes);
   }

    public void addEqu(String name, Long value) throws ProgramException {
        if( this.equs.containsKey(name) )
            throw new ProgramException("An EQU with the same name has already been defined");

        this.equs.put(name, value);
    }

    public long addData(byte val) {
        long address = this.locationCounter;
        Memory.addData(address, val);
        this.locationCounter += 1;
        return address;
    }

    public long addData(short val) {
        long address = this.locationCounter;
        Memory.addData(address, val);
        this.locationCounter += 2;
        return address;
    }

    public long addData(int val) {
        long address = this.locationCounter;
        Memory.addData(address, val);
        this.locationCounter += 4;
        return address;
    }

    public long addData(long val) {
        long address = this.locationCounter;
        Memory.addData(address, val);
        this.locationCounter += 8;
        return address;
    }

    public long addData(byte[] val) {
        long address = this.locationCounter;
        Memory.addMultiByte(address, val);
        this.locationCounter += val.length;
        return address;
    }
}
