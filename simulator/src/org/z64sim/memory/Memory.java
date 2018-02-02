/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory;

import java.awt.Rectangle;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import org.openide.util.Exceptions;
import org.z64sim.memory.window.MemoryTableModel;
import org.z64sim.memory.window.MemoryTopComponent;
import org.z64sim.program.Program;
import org.z64sim.program.ProgramException;

/**
 * Kind of singleton class
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory {

    private static MemoryTopComponent window = null;
    //private static Program program = null;
    private static Program program = new Program(); // aggiunto per evitare nullPointerException

    // This class cannot be instantiated
    private Memory() {
    }

    public static Program getProgram() {
        return program;
    }

    public static void setProgram(Program program) {
        Memory.program = program;
    }
    
    public static void setWindow(MemoryTopComponent w) {
        Memory.window = w;
    }

    public static MemoryTopComponent getWindow() {
        return Memory.window;
    }

    public static void redrawMemory() {
        if (Memory.window != null) {
            Memory.window.memoryTable.setModel(new MemoryTableModel());
            int _startRow = (int) (Memory.program._start / 8);

            // Select the row corresponding to the entry point
            Memory.window.memoryTable.setRowSelectionInterval(_startRow, _startRow);

            // Scroll to that row
            Memory.window.memoryTable.scrollRectToVisible(new Rectangle(Memory.window.memoryTable.getCellRect(_startRow, 0, true)));

            // Show the panel
            Memory.window.requestVisible();
        }
        
    }

}
