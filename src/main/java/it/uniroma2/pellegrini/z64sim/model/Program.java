/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.ProgramException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.*;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;


/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class Program {
    private static final Logger log = LoggerFactory.getLogger();

    final Map<Integer, MemoryElement> binary = new Hashtable<>();

    private Map<String, MemoryPointer> labels = new Hashtable<>();
    private Map<String, Long> equs = new Hashtable<>();
    private ArrayList<RelocationEntry> relocations = new ArrayList<>();
    private Integer locationCounter = 0;

    public MemoryPointer text = new MemoryPointer(0);
    public MemoryPointer _start = null;

    public Program() {
        // Initialize an empty IDT
        this.addData(new byte[0x800]);
    }

    public void finalizeProgram() throws ProgramException {
        // Check if main was set
        if(this._start == null)
            throw new ProgramException("No main function found");

        // Perform relocation of label values
        for(RelocationEntry rel : this.relocations) {
            rel.relocate(this);
        }

        // Add stack space
        this.addData(new byte[0x400]); // 1KB of stack should be enough for most programs

        // This program does not need anymore intermediate assembling information
        this.labels.clear();
        this.labels = null;
        this.equs.clear();
        this.equs = null;
        this.relocations.clear();
        this.relocations = null;
    }

    public void textSectionStart(Integer address) {
        this.text.setTarget(address);
    }

    private MemoryPointer findLabelAddress(String name) {
        return labels.get(name);
    }

    public void addRelocationEntry(Integer offset, String label) {
        relocations.add(new RelocationEntry(new MemoryPointer(offset), label));
    }

    // Returns false if a label with the same name already exists
    public boolean newLabel(String name, MemoryPointer address) throws ParseException {
        if(labels.containsKey(name)) {
            throw new ParseException("Label " + name + " already defined");
        }

        // Special handling of the main label
        if(name.equals("main")) {
            this._start = address;
        }

        labels.put(name, address);
        return true;
    }

    public Long getLabelAddress(String name) throws ParseException {
        final MemoryPointer mp = labels.get(name);
        if(mp == null)
            return null;
        return mp.getTarget();
    }

    public Integer getLocationCounter() {
        return locationCounter;
    }

    public MemoryElement getMemoryElementAt(Long address) {
        return binary.get(address.intValue());
    }

    public void setLocationCounter(Integer locationCounter) throws ProgramException {
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

            addData(new byte[(int) (newAddress - initialAddress)]);
        }
    }

    public void addInstruction(Instruction insn) {
        // We must preserve the IDT
        this.binary.put(this.locationCounter, insn);

        final int length = insn.getSize();
        this.locationCounter += length;
        log.trace("Found a {0}-byte instruction", length);
    }

    public void newDriver(Integer idn, long address) {
        int offset = idn * 8;
        this.binary.put(offset, new MemoryData((byte) (address >> 56)));
        this.binary.put(offset+1, new MemoryData((byte) (address >> 48)));
        this.binary.put(offset+2, new MemoryData((byte) (address >> 40)));
        this.binary.put(offset+3, new MemoryData((byte) (address >> 32)));
        this.binary.put(offset+4, new MemoryData((byte) (address >> 24)));
        this.binary.put(offset+5, new MemoryData((byte) (address >> 16)));
        this.binary.put(offset+6, new MemoryData((byte) (address >> 8)));
        this.binary.put(offset+7, new MemoryData((byte) (address)));
    }

    public void addEqu(String name, Long value) throws ProgramException {
        if (this.equs.containsKey(name)) {
            throw new ProgramException("An EQU with the same name has already been defined");
        }

        this.equs.put(name, value);
    }

    public Long getEqu(String name) {
        return this.equs.get(name);
    }

    public int addData(@NotNull byte[] val) {
        int addr = this.locationCounter;
        for (byte b : val) {
            this.binary.put(this.locationCounter++, new MemoryData(b));
        }
        return addr;
    }

    public int getLargestAddress() {
        return this.binary.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    /**
     * A relocation entry. Points to the initial address of the instruction. It
     * is then the role of relocate() method to account for differences in
     * the various instruction formats.
     */
    private class RelocationEntry {

        private final MemoryPointer applyTo;
        private final String label;

        public RelocationEntry(MemoryPointer applyTo, String label) {
            this.applyTo = applyTo;
            this.label = label;
        }

        private void relocateImmediate(OperandImmediate op, Instruction insn) throws ProgramException {
            // Get target address of the relocation
            MemoryPointer target = findLabelAddress(this.label);
            if (target == null) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            op.relocate(target);
        }

        private void relocateMemory(OperandMemory op, Instruction insn) throws ProgramException {
            // Get target address of the relocation
            MemoryPointer target = findLabelAddress(this.label);
            if (target == null) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            op.relocate(target);
        }

        private void relocateData(Program program) throws ProgramException {
            final int dataOffset = (int)(this.applyTo.getTarget());
            final MemoryPointer labelAddress = findLabelAddress(this.label);
            if(labelAddress == null) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }
            long target = labelAddress.getTarget();
            for(int i = 0; i < 8; i++) { // TODO: 8 bytes are common, but relocation should be more flexible
                byte currByte = (byte)((target >> ((8 - i) * 8)) & 0xFF);
                program.binary.put(dataOffset + i, new MemoryData(currByte));
            }
        }

        public void relocate(Program program) throws ProgramException {

            final MemoryPointer labelAddress = findLabelAddress(this.label);
            if(labelAddress == null) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            if(this.applyTo.getTarget() < program.text.getTarget()) {
                relocateData(program);
                return;
            }

            // Perform the relocation, depending on the instruction class
            Instruction insn = (Instruction) program.binary.get((int)this.applyTo.getTarget());
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
                    if (destination instanceof OperandMemory) {
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
