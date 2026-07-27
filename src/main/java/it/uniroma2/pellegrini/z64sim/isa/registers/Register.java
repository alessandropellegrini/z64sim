/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.registers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class Register {

    // TODO: migrate to an enum
    // Register encoding
    public static final int RAX = 0;
    public static final int RCX = 1;
    public static final int RDX = 2;
    public static final int RBX = 3;
    public static final int RSP = 4;
    public static final int RBP = 5;
    public static final int RSI = 6;
    public static final int RDI = 7;
    public static final int R8 = 8;
    public static final int R9 = 9;
    public static final int R10 = 10;
    public static final int R11 = 11;
    public static final int R12 = 12;
    public static final int R13 = 13;
    public static final int R14 = 14;
    public static final int R15 = 15;

    // TODO: migrate to ISA
    // Array to map virtual registers to physical registers
    private static final String[][] registers
            = {
            {"%al", "%cl", "%dl", "%bl", "%spl", "%bpl", "%sil", "%dil", "%r8b", "%r9b", "%r10b", "%r11b", "%r12b", "%r13b", "%r14b", "%r15b"},
            {"%ax", "%cx", "%dx", "%bx", "%sp", "%bp", "%si", "%di", "%r8w", "%r9w", "%r10w", "%r11w", "%r12w", "%r13w", "%r14w", "%r15w"},
            {"%eax", "%ecx", "%edx", "%ebx", "%esp", "%ebp", "%esi", "%edi", "%r8d", "%r9d", "%r10d", "%r11d", "%r12d", "%r13d", "%r14d", "%r15d"},
            {"%rax", "%rcx", "%rdx", "%rbx", "%rsp", "%rbp", "%rsi", "%rdi", "%r8", "%r9", "%r10", "%r11", "%r12", "%r13", "%r14", "%r15"}
    };

    // The current value of a register
    protected volatile Long value;

    // Observable support for model-view decoupling
    protected final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    // Constructor of the register
    public Register() {
        this.value = 0L;
    }

    // Read the value of a register
    public long getQuadword() {
        return this.value;
    }

    // Returns the old value of the register, before updating
    public void setQuadword(long val) {
        long oldValue = this.value;
        this.value = val;
        pcs.firePropertyChange("value", oldValue, val);
    }

    /* This works only because registers are placed in the registers[][] array
     * in code order (compare that to the constants at the beginning of the
     * class).
     */
    private static int scanRegister(int size, String name) {
        int i;

        for (i = 0; i < 16; i++) {
            if (name.equals(registers[size][i])) {
                break;
            }
        }

        // No error checking here: I assume that the grammar is correct and issues an error if a wrong token is found
        return i;
    }

    public static int getRegister8(String name) {
        return scanRegister(0, name);
    }

    public static int getRegister16(String name) {
        return scanRegister(1, name);
    }

    public static int getRegister32(String name) {
        return scanRegister(2, name);
    }

    public static int getRegister64(String name) {
        return scanRegister(3, name);
    }

    public static String getRegisterName(int code, int size) {
        int index;

        switch (size) {
            case 1:
                index = 0;
                break;
            case 2:
                index = 1;
                break;
            case 4:
                index = 2;
                break;
            case 8:
                index = 3;
                break;
            default:
                throw new RuntimeException("Wrong register size in getRegisterName");

        }

        return registers[index][code];
    }
}
