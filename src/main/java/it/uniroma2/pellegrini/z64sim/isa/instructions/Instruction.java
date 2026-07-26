/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
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

    /**
     * Returns the Type index of this instruction within its class.
     * Each subclass must implement this by looking up the mnemonic
     * in its class-specific opcode table.
     */
    public abstract int getType();

    @Override
    public byte[] getValue() {
        return encode();
    }

    /**
     * Encodes the instruction into its binary representation.
     * Each subclass overrides this to produce the correct byte sequence.
     * The default implementation returns a zeroed array.
     */
    protected byte[] encode() {
        return new byte[this.size];
    }

    public String getMnemonic() {
        return this.mnemonic;
    }

    // =====================================================================
    // Encoding helper methods
    // =====================================================================

    /**
     * Encode the Opcode byte: Class[7:4] | Type[3:0]
     */
    protected byte encodeOpcode(int type) {
        return (byte) (((this.clas & 0x0F) << 4) | (type & 0x0F));
    }

    /**
     * Encode the Mode byte: SS[7:6] | DS[5:4] | DI[3:2] | Mem[1:0]
     */
    protected static byte encodeMode(int ss, int ds, int di, int mem) {
        return (byte) (((ss & 0x03) << 6) | ((ds & 0x03) << 4) | ((di & 0x03) << 2) | (mem & 0x03));
    }

    /**
     * Encode the SIB byte: Bp[7] | Ip[6] | Scale[5:4] | Index[3:0]
     */
    protected static byte encodeSib(int bp, int ip, int scaleEncoded, int index) {
        return (byte) (((bp & 0x01) << 7) | ((ip & 0x01) << 6) | ((scaleEncoded & 0x03) << 4) | (index & 0x0F));
    }

    /**
     * Encode the R/M byte: Source[7:4] | Dest[3:0]
     */
    protected static byte encodeRm(int src, int dst) {
        return (byte) (((src & 0x0F) << 4) | (dst & 0x0F));
    }

    /**
     * Convert an operand size in bytes (1, 2, 4, 8) to the SS/DS encoding (00, 01, 10, 11).
     */
    protected static int sizeToSsDs(int sizeInBytes) {
        switch (sizeInBytes) {
            case 1: return 0;
            case 2: return 1;
            case 4: return 2;
            case 8: return 3;
            default: return 0;
        }
    }

    /**
     * Convert a scale factor (1, 2, 4, 8) to its 2-bit encoding (00, 01, 10, 11).
     */
    protected static int scaleToEncoding(int scale) {
        switch (scale) {
            case 1: return 0;
            case 2: return 1;
            case 4: return 2;
            case 8: return 3;
            default: return 0;
        }
    }

    /**
     * Write a 32-bit value into a byte array at the given offset, in little-endian order.
     */
    protected static void writeLE32(byte[] buf, int offset, int value) {
        buf[offset]     = (byte) (value & 0xFF);
        buf[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buf[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buf[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    /**
     * Write a 64-bit value into a byte array at the given offset, in little-endian order.
     */
    protected static void writeLE64(byte[] buf, int offset, long value) {
        for (int i = 0; i < 8; i++) {
            buf[offset + i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
    }

    /**
     * Look up the type index of this instruction's mnemonic within the given table.
     * Returns -1 if not found (should not happen in a well-formed instruction).
     */
    protected int lookupType(String[] mnemonics) {
        for (int i = 0; i < mnemonics.length; i++) {
            if (mnemonics[i].equals(this.mnemonic)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Encode a standard two-operand instruction (used by Class 1 and Class 2).
     * Builds the full 8-byte or 16-byte encoding from source, destination, and type.
     */
    protected byte[] encodeTwoOperand(int type, Operand source, Operand destination) {
        byte[] buf = new byte[this.size];
        buf[0] = encodeOpcode(type);

        // Determine SS, DS
        int ss = 0, ds = 0;
        if (source != null) {
            ss = sizeToSsDs(source instanceof OperandImmediate
                    ? (destination != null ? destination.getSize() : source.getSize())
                    : source.getSize());
        }
        if (destination != null) {
            ds = sizeToSsDs(destination.getSize());
        }

        // Determine Mem
        int mem = 0;
        if (source instanceof OperandMemory) {
            mem = 2; // source is memory
        } else if (destination instanceof OperandMemory) {
            mem = 1; // destination is memory
        }

        // Determine DI
        int di = 0;
        boolean hasImmediate = source instanceof OperandImmediate;
        boolean hasDisplacement = false;

        OperandMemory memOp = null;
        if (source instanceof OperandMemory) {
            memOp = (OperandMemory) source;
        } else if (destination instanceof OperandMemory) {
            memOp = (OperandMemory) destination;
        }

        if (memOp != null && memOp.getDisplacement() != 0) {
            hasDisplacement = true;
        }

        if (hasDisplacement && hasImmediate) {
            di = 3; // both
        } else if (hasDisplacement) {
            di = 2; // displacement only
        } else if (hasImmediate) {
            di = 1; // immediate only
        }

        buf[1] = encodeMode(ss, ds, di, mem);

        // Encode SIB
        int bp = 0, ip = 0, scaleEnc = 0, indexReg = 0;
        if (memOp != null) {
            if (memOp.getBase() >= 0) bp = 1;
            if (memOp.getIndex() >= 0) {
                ip = 1;
                scaleEnc = scaleToEncoding(memOp.getScale());
                indexReg = memOp.getIndex();
            }
        }
        buf[2] = encodeSib(bp, ip, scaleEnc, indexReg);

        // Encode R/M
        int srcReg = 0, dstReg = 0;
        if (source instanceof OperandRegister) {
            srcReg = ((OperandRegister) source).getRegister();
        } else if (source instanceof OperandMemory && bp == 1) {
            // Source is memory with base register — base goes in source field
            // (for Mem=10, source field = base register)
        }
        if (destination instanceof OperandRegister) {
            dstReg = ((OperandRegister) destination).getRegister();
        }

        // For memory operands, the base register goes in the appropriate R/M field
        if (memOp != null && bp == 1) {
            int baseReg = (int) memOp.getBase();
            if (destination instanceof OperandMemory) {
                dstReg = baseReg;
            } else if (source instanceof OperandMemory) {
                srcReg = baseReg;
            }
        }

        buf[3] = encodeRm(srcReg, dstReg);

        // Encode Displacement / Short Immediate (bytes 4–7)
        if (hasDisplacement) {
            writeLE32(buf, 4, memOp.getDisplacement());
        } else if (hasImmediate && this.size == 8) {
            // Short immediate goes in the displacement field
            writeLE32(buf, 4, (int) ((OperandImmediate) source).getValue());
        }

        // Encode 64-bit Immediate (bytes 8–15) if 16-byte instruction
        if (this.size == 16 && hasImmediate) {
            writeLE64(buf, 8, ((OperandImmediate) source).getValue());
        }

        return buf;
    }
}
