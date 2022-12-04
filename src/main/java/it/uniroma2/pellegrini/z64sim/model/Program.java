/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import it.uniroma2.pellegrini.z64sim.controller.exceptions.ProgramException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.*;
import it.uniroma2.pellegrini.z64sim.isa.operands.MemoryTarget;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

import java.util.*;


/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class Program {
    private static final Logger log = LoggerFactory.getLogger();

    private Byte[] IDT = new Byte[0x800];
    private Map<Long, Instruction> text = new Hashtable<>();
    private Deque<Byte> data = new ArrayDeque<>();

    private Map<String, MemoryTarget> labels = new Hashtable<>();
    private Map<String, Long> equs = new Hashtable<>();
    private ArrayList<RelocationEntry> relocations = new ArrayList<>();
    private long locationCounter = 0;

    public MemoryTarget _start = null;
    public long _dataEnd = -1;

    public Program() {
        for(int i = 0; i < 0x800; i++) {
            this.IDT[i] = 0;
        }
    }

    public void finalizeProgram() throws ProgramException {
        // Perform relocation of label values
        for(RelocationEntry rel : this.relocations) {
            rel.relocate();
        }

        int DataS = this.data.size();
        this._dataEnd = 0x800 + DataS;

        // This Program does not need anymore intermediate assembling
        // information, so we remove references. This is particularly useful
        // when this object is serialized to save an executable.
        this.labels.clear();
        this.labels = null;
        this.equs.clear();
        this.equs = null;
        this.relocations.clear();
        this.relocations = null;
    }

    private MemoryTarget findLabelAddress(String name) {
        return labels.get(name);
    }

    public void addRelocationEntry(long offset, String label) {
        relocations.add(new RelocationEntry(new MemoryTarget(offset), label));
    }

    // Returns false if a label with the same name already exists
    public boolean newLabel(String name, MemoryTarget address) {
        if(labels.containsKey(name)) {
            return false;
        }

        // Special handling of the main label
        if(name.equals("main")) {
            this._start = address;
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
        this.locationCounter = locationCounter;
    }

    public void finalizeData() {

        // Get the initial address of data
        long initialAddress = this.locationCounter;

        // Align instructions to 8 bytes
        if ((initialAddress & 0x07) != 0) {
            long newAddress = initialAddress;
            newAddress += 8;
            newAddress = newAddress & 0xfffffffffffffff8L;

            // Fill memory in between
            long fillSize = newAddress - initialAddress;
            for(int i = 0; i < fillSize; i++) {
                this.data.add((byte)0);
            }
        }
    }

    public void addInstruction(Instruction insn) throws ProgramException {
        // We must preserve the IDT
        if(this.locationCounter < 0x800) {
            throw new ProgramException("You are trying to put data/instructions over the IVT.");
        }

        this.text.put(this.locationCounter, insn);

        final int length = insn.getEncoding().length;
        this.locationCounter += length;
        log.trace("Found a {0}-byte instruction", length);
    }

    public void newDriver(Integer idn, long address) {
        int offset = idn * 8;
        this.IDT[offset] = (byte)(address >> 56);
        this.IDT[offset+2] = (byte)(address >> 48);
        this.IDT[offset+3] = (byte)(address >> 40);
        this.IDT[offset+4] = (byte)(address >> 32);
        this.IDT[offset+5] = (byte)(address >> 24);
        this.IDT[offset+6] = (byte)(address >> 16);
        this.IDT[offset+7] = (byte)(address >> 8);
        this.IDT[offset+8] = (byte)(address);
    }

    public void addEqu(String name, Long value) throws ProgramException {
        if (this.equs.containsKey(name)) {
            throw new ProgramException("An EQU with the same name has already been defined");
        }

        this.equs.put(name, value);
    }

    // TODO: missing size of text!!!!!!
    public long addData(byte val) {
        long addr = this.data.size();
        this.data.add(val);
        return addr;
    }

    public long addData(short val) {
        long addr = this.data.size();
        this.data.add((byte)(val >> 8));
        this.data.add((byte)val);
        return addr;
    }

    public long addData(int val) {
        long addr = this.data.size();
        this.data.add((byte)(val >> 24));
        this.data.add((byte)(val >> 16));
        this.data.add((byte)(val >> 8));
        this.data.add((byte)val);
        return addr;
    }

    public long addData(long val) {
        long addr = this.data.size();
        this.data.add((byte)(val >> 56));
        this.data.add((byte)(val >> 48));
        this.data.add((byte)(val >> 40));
        this.data.add((byte)(val >> 32));
        this.data.add((byte)(val >> 24));
        this.data.add((byte)(val >> 16));
        this.data.add((byte)(val >> 8));
        this.data.add((byte)val);
        return addr;
    }

    public long addData(byte[] val) {
        long addr = this.data.size();
        // Fill memory in between
        for(int i = 0; i < val.length; i++) {
            this.data.add(val[i]);
        }
        return addr;
    }

    /**
     * A relocation entry. Points to the initial address of the instruction. It
     * is then the role of relocate() method to account for differences in
     * the various instruction formats.
     */
    private class RelocationEntry {

        private final MemoryTarget applyTo;
        private final String label;

        public RelocationEntry(MemoryTarget applyTo, String label) {
            this.applyTo = applyTo;
            this.label = label;
        }

        private void relocateImmediate(OperandImmediate op, Instruction insn) throws ProgramException {
            // Get target address of the relocation
            MemoryTarget target = findLabelAddress(this.label);
            if (target == null) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            op.relocate(target);
        }

        private void relocateMemory(OperandMemory op, Instruction insn) throws ProgramException {
            // Get target address of the relocation
            MemoryTarget target = findLabelAddress(this.label);
            if (target == null) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            op.relocate(target);
        }

        public void relocate() throws ProgramException {

            // Get address of the instruction where relocation should be applied
            // TODO: this is wrong, we might want to relocate also the data section with label
            if (!(this.applyTo instanceof Instruction)) {
                throw new ProgramException("Relocation entry pointing to something which is not an instruction");
            }

            // Perform the relocation, depending on the instruction class
            Instruction insn = (Instruction) this.applyTo;
            Operand source, destination;
            switch (insn.getClas()) {
                case 1:
                    source = ((InstructionClass1) insn).getSource();
                    destination = ((InstructionClass1) insn).getDestination();
                    if (source instanceof OperandImmediate) {
                        relocateImmediate((OperandImmediate) source, insn);
                    }
                    if (source instanceof OperandMemory) {
                        relocateMemory((OperandMemory) source, insn);
                    }
                    if (destination != null) {
                        relocateMemory((OperandMemory) destination, insn);
                    }
                    break;
                case 2:
                    source = ((InstructionClass2) insn).getSource();
                    destination = ((InstructionClass2) insn).getDestination();
                    if (source instanceof OperandImmediate) {
                        relocateImmediate((OperandImmediate) source, insn);
                    }
                    if (source instanceof OperandMemory) {
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
