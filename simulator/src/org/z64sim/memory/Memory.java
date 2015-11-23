/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.util.Exceptions;
import org.z64sim.memory.window.MemoryTableModel;
import org.z64sim.memory.window.MemoryTopComponent;
import org.z64sim.program.ProgramException;

/**
 * Kind of singleton class
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory {

    private static final ArrayList<MemoryElement> memoryMap = new ArrayList<>();
    private static MemoryTopComponent window = null;
    private static long _start = 0; // This is the _start address of the program

    // This class cannot be instantiated
    private Memory() {}

    public static void setEntryPoint(long entry) {
        Memory._start = entry;
    }

    public static void setWindow(MemoryTopComponent w) {
        Memory.window = w;
    }

    public static MemoryTopComponent getWindow() {
        return Memory.window;
    }

    public static ArrayList<MemoryElement> getMemory() {
        return Memory.memoryMap;
    }

    public static void wipeMemory() {
        Memory.memoryMap.clear();
    }

    public static void redrawMemory() {
        if(Memory.window != null) {
            Memory.window.memoryTable.setModel(new MemoryTableModel());
            Memory.window.requestVisible();
        }
    }

    public static synchronized MemoryElement getElementFromAddress(long address) {
        MemoryElement el;

        // Try a fast binary search. This could fail if some instruction accesses
        // a subportion of a data element (e.g., a quadword was declared, but
        // it's now accessed on a byte basis.
        int index = Collections.binarySearch(memoryMap, new MemoryElementSearchable(address) );

        if(index >= 0) {
            el = memoryMap.get(index);
        } else {
            // In this case, the returned value is (-(insertion point) - 1).
            // Therefore, we can immediately access the element that should be
            // keeping the required data, if present.
            el = memoryMap.get(-index);

            if(!(el.getAddress() < address && el.getAddress() + el.getSize() > address)) {
                // No element found. Create a new one, add it to the list,
                // redraw the window. Here we assume that we're adding new data
                // elements, so no mutagen code. Additionally, the assumption
                // is that we're creating a quadword.
                el = new DataElement(new byte[8]);
                el.setSize(8);

                try {
                    el.setAddress(address);
                } catch (ProgramException ex) {
                    // From the construction of the program, this should not be possible
                    Exceptions.printStackTrace(ex);
                }

                memoryMap.add(-index, el);
            }
        }
        return el;
    }

    public static synchronized void addData(long address, byte val) {
        MemoryElement mEl = getElementFromAddress(address);

        if(!(mEl instanceof DataElement)) {
            throw new RuntimeException("Adding data on top of an instruction?");
        }

        DataElement el = (DataElement)mEl;

        int offset = 0;
        if(el.getAddress() != address) {
            offset = (int)(address - el.getAddress());
        }

        el.getValue()[offset] = (byte)val;
    }

    public static void addMultiByte(long address, byte[] bytes) {
        for(int i = 0; i < bytes.length; i++) {
            addData(address++, bytes[i]);
        }
    }

    // We have to deal with misalignment here, so this is why we call multiple
    // time the byte version of addData
    public static synchronized void addData(long address, short val) {
        byte[] bytes = ByteBuffer.allocate(Short.SIZE / Byte.SIZE).putShort(val).array();
        addMultiByte(address, bytes);
    }

    public static synchronized void addData(long address, int val) {
        byte[] bytes = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(val).array();
        addMultiByte(address, bytes);
    }

    public static synchronized void addData(long address, long val) {
        byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(val).array();
        addMultiByte(address, bytes);
    }

    public static synchronized void updateMemory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
