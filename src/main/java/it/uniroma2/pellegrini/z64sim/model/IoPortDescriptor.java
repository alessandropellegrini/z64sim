/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

/**
 * Describes a single memory element (flip-flop or register) on a device's
 * I/O interface. Flags are combined with bitwise OR.
 *
 * <p>Example:
 * <pre>
 * new IoPortDescriptor("MODE",
 *     IoPortDescriptor.FLIP_FLOP
 *   | IoPortDescriptor.READABLE
 *   | IoPortDescriptor.WRITABLE
 *   | IoPortDescriptor.CONNECTED_TO_CU)
 * </pre>
 */
public class IoPortDescriptor {

    // Element type (mutually exclusive)
    public static final int FLIP_FLOP       = 0x01;
    public static final int REGISTER        = 0x02;
    // Access flags
    public static final int READABLE        = 0x04;  // CPU can read  (device --> CPU)
    public static final int WRITABLE        = 0x08;  // CPU can write (CPU --> device)
    // Connections
    public static final int CONNECTED_TO_CU = 0x10;
    public static final int CONNECTED_TO_PU = 0x20;

    private final String name;
    private final int flags;
    private final int widthBytes;  // 1, 2, 4, or 8 (don't care for FLIP_FLOP)

    /**
     * Create a descriptor for a register element.
     *
     * @param name       element name
     * @param flags      bitmap of flags
     * @param widthBytes width in bytes (1, 2, 4, or 8)
     */
    public IoPortDescriptor(String name, int flags, int widthBytes) {
        this.name = name;
        this.flags = flags;
        this.widthBytes = widthBytes;
    }

    /**
     * Create a descriptor for a flip-flop element (widthBytes defaults to 1).
     *
     * @param name  element name
     * @param flags bitmap of flags (should include FLIP_FLOP)
     */
    public IoPortDescriptor(String name, int flags) {
        this(name, flags, 1);
    }

    public String getName() {
        return name;
    }

    public int getWidthBytes() {
        return widthBytes;
    }

    public boolean isFlipFlop() {
        return (flags & FLIP_FLOP) != 0;
    }

    public boolean isRegister() {
        return (flags & REGISTER) != 0;
    }

    public boolean isReadable() {
        return (flags & READABLE) != 0;
    }

    public boolean isWritable() {
        return (flags & WRITABLE) != 0;
    }

    public boolean isConnectedToCU() {
        return (flags & CONNECTED_TO_CU) != 0;
    }

    public boolean isConnectedToPU() {
        return (flags & CONNECTED_TO_PU) != 0;
    }
}
