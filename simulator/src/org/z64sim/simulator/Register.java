/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.simulator;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Register {

    // Public codes to identify registers
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

    // Array to map virtual registers to physical registers
    private static final String[][] registers
            = {
                {"%al", "%cl", "%dl", "%bl", "%spl", "%bpl", "%sil", "%dil", "%r8b", "%r9b", "%r10b", "%r11b", "%r12b", "%r13b", "%r14b", "%r15b"},
                {"%ax", "%bx", "%cx", "%dx", "%sp", "%bp", "%si", "%di", "%r8w", "%r9w", "%r10w", "%r11w", "%r12w", "%r13w", "%r14w", "%r15w"},
                {"%eax", "%ebx", "%ecx", "%edx", "%esp", "%ebp", "%esi", "%edi", "%r8d", "%r9d", "%r10d", "%r11d", "%r12d", "%r13d", "%r14d", "%r15d"},
                {"%rax", "%rbx", "%rcx", "%rdx", "%rsp", "%rbp", "%rsi", "%rdi", "%r8", "%r9", "%r10", "%r11", "%r12", "%r13", "%r14", "%r15"}
            };

    // The current value of a register
    protected Long value;

    // Constructor of the register
    public Register() {
        this.value = 0L;
    }

    // Read the value of a register
    public Long getValue() {
        return this.value;
    }

    // Update the value of a register
    public void setValue(Long value) {
        this.value = value;
    }

    /* This works only because registers are placed in the registers[][] array
     * in code order (compare that to the constants at the beginning of the
     * class.
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

        if (size != 8 && size != 16 && size != 32 && size != 64) {

        }

        switch (size) {
            case 8:
                index = 0;
                break;
            case 16:
                index = 1;
                break;
            case 32:
                index = 2;
                break;
            case 64:
                index = 3;
                break;
            default:
                throw new RuntimeException("Wrong register size in getRegisterName");

        }

        return registers[index][code];
    }
}
