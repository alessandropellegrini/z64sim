/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.registers;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class FlagsRegister extends Register {

    // Constants to identify specific bits in FLAGS
    public static final int CF = 0b000000000000001;
    public static final int PF = 0b000000000000100;
    public static final int ZF = 0b000000001000000;
    public static final int SF = 0b000000010000000;
    public static final int IF = 0b000001000000000;
    public static final int DF = 0b000010000000000;
    public static final int OF = 0b000100000000000;

    public FlagsRegister() {
        super();
        this.value = (long)0b010; // By default, bit 1 is always set in this register
    }

    private void setBit(int bit) {
        this.value = this.value | (long)bit;
    }

    private void clearBit(int bit) {
        this.value = this.value & ~(long)bit;
    }

    private boolean isSetBit(int bit) {
        return (this.value & (long)bit) != 0;
    }

    public boolean getCF() {
        return isSetBit(CF);
    }

    public boolean getPF() {
        return isSetBit(PF);
    }

    public boolean getZF() {
        return isSetBit(ZF);
    }

    public boolean getSF() {
        return isSetBit(SF);
    }

    public boolean getIF() {
        return isSetBit(IF);
    }

    public boolean getDF() {
        return isSetBit(DF);
    }

    public boolean getOF() {
        return isSetBit(OF);
    }

    public void setCF(boolean set) {
        if(set)
            setBit(CF);
        else
            clearBit(CF);
    }

    public void setPF(boolean set) {
        if(set)
            setBit(PF);
        else
            clearBit(PF);
    }

    public void setZF(boolean set) {
        if(set)
            setBit(ZF);
        else
            clearBit(ZF);
    }

    public void setSF(boolean set) {
        if(set)
            setBit(SF);
        else
            clearBit(SF);
    }

    public void setIF(boolean set) {
        if(set)
            setBit(IF);
        else
            clearBit(IF);
    }

    public void setDF(boolean set) {
        if(set)
            setBit(DF);
        else
            clearBit(DF);
    }

    public void setOF(boolean set) {
        if(set)
            setBit(OF);
        else
            clearBit(OF);
    }

}
