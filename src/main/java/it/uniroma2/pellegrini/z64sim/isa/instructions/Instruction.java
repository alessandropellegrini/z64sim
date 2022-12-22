/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

public abstract class Instruction implements MemoryElement {
    private static final Logger log = LoggerFactory.getLogger();

    protected final String mnemonic;
    protected final byte clas;
    protected int size;

    public Instruction(String mnemonic, int clas) {
        this.mnemonic = mnemonic;
        this.clas = (byte) clas;
    }

    public byte getClas() {
        return this.clas;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public abstract void run() throws SimulatorException;

    @Override
    public byte[] getValue() {
        return new byte[this.size]; // TODO: here we should call on demand the future new encoder
    }

    public String getMnemonic() {
        return this.mnemonic;
    }
}
