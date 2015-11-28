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
public class RegisterFlags extends Register {

    // Constants to identify specific bits in FLAGS
    public static final int CF = 0b000000000000001;
    public static final int PF = 0b000000000000100;
    public static final int ZF = 0b000000001000000;
    public static final int SF = 0b000000010000000;
    public static final int IF = 0b000001000000000;
    public static final int DF = 0b000010000000000;
    public static final int OF = 0b000100000000000;

    public RegisterFlags() {
        super();
        this.value = (long)0b010; // By default, bit 1 is always set in this register
    }

    public void setBit(int bit) {
        this.value = this.value | (long)bit;
    }

    public void clearBit(int bit) {
        this.value = this.value & ~(long)bit;
    }

    public boolean isSetBit(int bit) {
        return (this.value & (long)bit) != 0;
    }

}
