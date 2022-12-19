/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

public class MemoryData implements MemoryElement {
    private byte value;

    public MemoryData(byte value) {
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return new byte[]{this.value};
    }

    @Override
    public int getSize() {
        return 1;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%02x", this.value);
    }
}
