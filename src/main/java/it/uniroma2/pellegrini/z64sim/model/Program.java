/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.ProgramException;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class Program {
    private static final Logger log = LoggerFactory.getLogger();

    private Byte[] IDT = new Byte[0x800];
    private Deque<Byte> text = new ArrayDeque<Byte>();
    private Deque<Byte> data = new ArrayDeque<Byte>();

    private Map<String, Long> labels = new LinkedHashMap<>();
    private Map<String, Long> equs = new LinkedHashMap<>();
    private ArrayList<RelocationEntry> relocations = new ArrayList<>();
    private long locationCounter = 0;

    public long _start = -1;
    public long _dataStart = -1;
    public Byte[] program = null;


    public final static int STACK_SIZE = 0x800;

    public Program() {
        for(int i = 0; i < 0x800; i++) {
            this.IDT[i] = 0;
        }
    }

    public void finalizeProgram() throws ProgramException {

        // Perform relocation of label values
        for (RelocationEntry rel : this.relocations) {
            rel.relocate();
        }

        int TextS=this.text.size();
        int DataS=this.data.size();

        this.program = new Byte[TextS + DataS + Program.STACK_SIZE];

        Byte[] text = this.text.toArray(new Byte[this.text.size()]);
        Byte[] data = this.data.toArray(new Byte[this.data.size()]);

        System.arraycopy(text, 0, this.program, 0, TextS);
        System.arraycopy(this.IDT, 0, this.program, 0, this.IDT.length);
        System.arraycopy(data, 0, this.program, TextS, DataS);

        this._dataStart = TextS;

        for(int i = 0; i < Program.STACK_SIZE; i++) {
            this.program[TextS + DataS + i] = 0;
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
        this.IDT = null;
        this.text.clear();
        this.text = null;
        this.data.clear();
        this.data = null;

        // Register this as the currently assembled program
//        Memory.setProgram(this);
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
        long fillSize = locationCounter - this.locationCounter;
        for(int i = 0; i < fillSize; i++) {
            this.text.add((byte)0);
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

    public void addInstructionToMemory(Instruction insn) throws ProgramException {

        // Take the first instruction as the entry point
        if(this._start == -1)
            this._start = this.locationCounter;

        // We must preserve the IDT
        if (this.locationCounter < 0x800) {
            throw new ProgramException("You are trying to put data/instructions over the IDT.");
        }

        byte[] bytes = insn.getEncoding();

        log.trace("Found a {}-byte instruction", bytes.length);

        for(int i = 0; i < bytes.length; i++) {
           /* if(i>=bytes.length)
            {
                this.text.add(bytes[i]);
            }*/
            //if(i <= bytes.length){
            this.text.add(bytes[i]);
           /*}
           else{
                this.text.add((byte)0x00);
            }*/

        }
        this.locationCounter += bytes.length;
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
            // TODO: da implementare

            //throw new ProgramException("Da implementare!");
            
            /*
            // Get target address of the relocation
            long target = findLabelAddress(this.label);
            if (target == -1) {
                throw new ProgramException("Label " + this.label + " was not defined in the program");
            }

            // Offset is computed after the fetch phase
            long displacement = target - insn.getAddress() + insn.getSize();

            op.relocate(displacement);
            */
        }

        public void relocate() throws ProgramException {
            //throw new ProgramException("Da implementare!");
            
            /*
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
            */

        }

    }
}
