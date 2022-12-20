/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

import java.nio.ByteBuffer;

public abstract class Instruction implements MemoryElement {
    private static final Logger log = LoggerFactory.getLogger();

    protected final String mnemonic;
    protected final byte clas;
    protected byte type;
    protected int size;
    protected byte[] encoding;


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

    public byte[] getEncoding() {
        return encoding;
    }

    public void setEncoding(byte[] encoding) {
        this.encoding = encoding;
    }

    public abstract void run();

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for(int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        ByteBuffer bb = ByteBuffer.allocate(b.length);
        bb.put(b);
        return bb.getLong();
    }

    public static byte byteToBits(byte b, int start, int end) throws DisassembleException {
        byte mask = 0;
        if(start < end || start > 7 || end < 0) throw new DisassembleException("No valid start || end");

        for(int i = 7 - start; i <= 7 - end; i++) {
            mask += 1 << 7 - i;
        }
        byte ret = (byte) (mask & b);
        for(int i = 0; i < end; i++) {
            ret /= 2;
        }
        if(ret < 0) {
            ret += Math.pow(2, start - end + 1);
        }
        return ret;
    }

    @Override
    public byte[] getValue() {
        return this.encoding;
    }

    public String getMnemonic() {
        return this.mnemonic;
    }
}
