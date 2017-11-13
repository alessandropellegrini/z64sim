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
import org.z64sim.program.instructions.InstructionClass1;
import org.z64sim.program.instructions.InstructionClass2;
import org.z64sim.program.instructions.InstructionClass5;
import org.z64sim.program.instructions.InstructionClass6;
import org.z64sim.program.instructions.Operand;
import org.z64sim.program.instructions.OperandImmediate;
import org.z64sim.program.instructions.OperandMemory;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Program {

    private final ArrayList<MemoryElement> mMap; // This field is needed only to serialize the object to a file for later reload
    private Map<String, Long> labels = new LinkedHashMap<>();
    private Map<String, Long> equs = new LinkedHashMap<>();
    private ArrayList<RelocationEntry> relocations = new ArrayList<>();
    private long locationCounter = 0;
    private long _start; // This field is necessary only to serialize the object.

    public final static int STACK_SIZE = 0x800;

    public Program() {

        // Only one program at a time can be loaded in memory
        Memory.wipeMemory();

        // Point to the Memory class
        this.mMap = Memory.getMemory();

        // Create element in the memory map for the IDT
        byte[] quadword = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        for (int i = 0; i < 256; i++) {
            DataElement el = new DataElement(quadword.clone());
            try {
                el.setAddress(i * 8);
                el.setSize(8);
            } catch (ProgramException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.mMap.add(el);
        }
    }

    public void finalizeProgram() throws ProgramException {

        // Perform relocation of label values
        for (RelocationEntry rel : this.relocations) {
            rel.relocate();
        }

        // Add stack space
        byte[] quadword = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        for (int i = 0; i < Program.STACK_SIZE / 8; i++) {
            DataElement el = new DataElement(quadword.clone());
            try {
                el.setAddress(this.locationCounter);
                el.setSize(8);
                this.locationCounter += 8;
            } catch (ProgramException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.mMap.add(el);
        }

        // This Program does not need anymore intermediate assemblying
        // information, so we remove references. This is particularly useful
        // when this object is serialized to save an executable.
        this.labels.clear();
        this.labels = null;
        this.equs.clear();
        this.equs = null;
        this.relocations.clear();
        this.relocations = null;
    }

    private long findLabelAddress(String name) {
        Long address = labels.get(name);
        return (address != null ? address : -1);
    }

    public void addRelocationEntry(long offset, String label) {
        relocations.add(new RelocationEntry(offset, label));
    }

    // Returns false if a label with the same name alrady exists
    public boolean newLabel(String name, long address) {
        if (labels.containsKey(name)) {
            return false;
        }

        labels.put(name, address);
        return true;
    }

    public long getLocationCounter() {
        return locationCounter;
    }

    public void setLocationCounter(long locationCounter) throws ProgramException {
        if (locationCounter < this.locationCounter) {
            throw new ProgramException("Moving backwards the location counter is not supported");
        }

        // Fill memory in between
        for (long i = this.locationCounter; i < locationCounter; i++) {
            Memory.addData(i, (byte) 0x00);
        }

        this.locationCounter = locationCounter;
    }

    public void finalizeData() {

        // Align instructions to 8 bytes
        if ((this.locationCounter & 0x07) != 0) {
            this.locationCounter += 8;
            this.locationCounter = this.locationCounter & 0xfffffffffffffff8L;
        }

        // The next address is the address of the first instruction to be executed
        this._start = this.locationCounter;
        Memory.setEntryPoint(this._start);
    }

    public void addInstructionToMemory(Instruction i) throws ProgramException {

        // We must preserve the IDT
        if (this.locationCounter < 0x800) {
            throw new ProgramException("You are trying to put data/instructions over the IDT.");
        }

        try {
            i.setAddress(this.locationCounter);
        } catch (Exception e) {
            throw new ProgramException("Runtime error in the assembler:" + e.getMessage());
        }

        // add() can be used only because we prevent instructions to appear before .data section,
        // so they can be directly pushed at the end of the ArrayList
        this.mMap.add(i);

        if (i.getSize() == -1) {
            throw new RuntimeException("Adding an instruction with unspecified size");
        }

        this.locationCounter += i.getSize();
    }

    public void newDriver(Integer idn, long address) {

        MemoryElement entry = Memory.getElementFromAddress(idn * 8);
        byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(address).array();
        entry.setValue(bytes);
    }

    public void addEqu(String name, Long value) throws ProgramException {
        if (this.equs.containsKey(name)) {
            throw new ProgramException("An EQU with the same name has already been defined");
        }

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

    /**
     * A relocation entry. Points to the initial address of the instruction. It
     * is then the role of the relocate() method to account for differences in
     * the various instruction formats.
     */
    private class RelocationEntry {

        private final long applyTo;
        private final String label;

        public RelocationEntry(long applyTo, String label) {
            this.applyTo = applyTo;
            this.label = label;
        }

        private void relocateImmediate(OperandImmediate op, Instruction insn) throws ProgramException {
            // Get target address of the relocation
            long target = findLabelAddress(this.label);
            if (target == -1) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            op.relocate(target);
        }

        private void relocateMemory(OperandMemory op, Instruction insn) throws ProgramException {
            // Get target address of the relocation
            long target = findLabelAddress(this.label);
            if (target == -1) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            // Offset is computed after the fetch phase
            long displacement = target - insn.getAddress() + insn.getSize();

            op.relocate(displacement);
        }

        public void relocate() throws ProgramException {

            // Get address of the instruction where relocation should be applied
            MemoryElement el = Memory.getElementFromAddress(this.applyTo);
            if (!(el instanceof Instruction)) {
                throw new ProgramException("Relocation entry pointing to something which is not an instruction");
            }

            // Perform the relocation, depending on the instruction class
            Instruction insn = (Instruction) el;
            Operand source, destination;
            switch (insn.getClas()) {
                case 1:
                    source = ((InstructionClass1) insn).getSource();
                    destination = ((InstructionClass1) insn).getDestination();
                    if (source != null && source instanceof OperandImmediate) {
                        relocateImmediate((OperandImmediate) source, insn);
                    }
                    if (source != null && source instanceof OperandMemory) {
                        relocateMemory((OperandMemory) source, insn);
                    }
                    if (destination != null) {
                        relocateMemory((OperandMemory) destination, insn);
                    }
                    break;
                case 2:
                    source = ((InstructionClass2) insn).getSource();
                    destination = ((InstructionClass2) insn).getDestination();
                    if (source != null && source instanceof OperandImmediate) {
                        relocateImmediate((OperandImmediate) source, insn);
                    }
                    if (source != null && source instanceof OperandMemory) {
                        relocateMemory((OperandMemory) source, insn);
                    }
                    if (destination != null) {
                        relocateMemory((OperandMemory) destination, insn);
                    }
                    break;

                case 5:
                    destination = ((InstructionClass5) insn).getTarget();
                    if (destination != null) {
                        relocateMemory((OperandMemory) destination, insn);
                    }
                    break;
                case 6:
                    destination = ((InstructionClass6) insn).getTarget();
                    if (destination != null) {
                        relocateMemory((OperandMemory) destination, insn);
                    }
                    break;
                default:
                    throw new ProgramException("Relocating an instruction which does not belong to a relocatable class");
            }

        }

    }
}
