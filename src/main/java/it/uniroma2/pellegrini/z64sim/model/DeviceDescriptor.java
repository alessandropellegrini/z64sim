/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a device's hardware interface: its name, protocol support,
 * and the set of I/O port elements (flip-flops and registers).
 * <p>
 * Built via the inner {@link Builder} class:
 * <pre>
 * new DeviceDescriptor.Builder("MyDevice")
 *     .busyWaiting()
 *     .ioPort("REG_IN", IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 4)
 *     .build();
 * </pre>
 */
public class DeviceDescriptor {

    private final String deviceName;
    private final boolean busyWaitingEnabled;
    private final boolean interruptsEnabled;
    private final List<IoPortDescriptor> ioPorts;

    private DeviceDescriptor(Builder builder) {
        this.deviceName = builder.deviceName;
        this.busyWaitingEnabled = builder.busyWaitingEnabled;
        this.interruptsEnabled = builder.interruptsEnabled;
        this.ioPorts = Collections.unmodifiableList(new ArrayList<>(builder.ioPorts));
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isBusyWaitingEnabled() {
        return busyWaitingEnabled;
    }

    public boolean isInterruptsEnabled() {
        return interruptsEnabled;
    }

    /**
     * Return all I/O port elements, including protocol-declared ones
     * (STATUS, INT_REQ).
     */
    public List<IoPortDescriptor> getIoPorts() {
        return ioPorts;
    }

    /**
     * Find an I/O port element by name.
     *
     * @param name element name
     * @return the descriptor, or null if not found
     */
    public IoPortDescriptor getIoPort(String name) {
        for (IoPortDescriptor p : ioPorts) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Builder for constructing {@link DeviceDescriptor} instances.
     */
    public static class Builder {
        private final String deviceName;
        private boolean busyWaitingEnabled = false;
        private boolean interruptsEnabled = false;
        private final List<IoPortDescriptor> ioPorts = new ArrayList<>();

        public Builder(String deviceName) {
            this.deviceName = deviceName;
        }

        /**
         * Declare busy-waiting support.
         * Adds a STATUS flip-flop (FLIP_FLOP | READABLE | WRITABLE | CONNECTED_TO_CU).
         * STATUS is always connected to the CU.
         */
        public Builder busyWaiting() {
            this.busyWaitingEnabled = true;
            ioPorts.add(new IoPortDescriptor("STATUS",
                    IoPortDescriptor.FLIP_FLOP
                  | IoPortDescriptor.READABLE
                  | IoPortDescriptor.WRITABLE
                  | IoPortDescriptor.CONNECTED_TO_CU));
            return this;
        }

        /**
         * Declare interrupt support.
         * Adds INT_REQ flip-flop (FLIP_FLOP | WRITABLE).
         * The IVN (Interrupt Vector Number) is hardwired at device registration
         * time and is not a CPU-accessible port.
         */
        public Builder interrupts() {
            this.interruptsEnabled = true;
            ioPorts.add(new IoPortDescriptor("INT_REQ",
                    IoPortDescriptor.FLIP_FLOP
                  | IoPortDescriptor.WRITABLE));
            return this;
        }

        /**
         * Declare a custom I/O port element (register).
         *
         * @param name       element name
         * @param flags      bitmap of flags (should include REGISTER)
         * @param widthBytes width in bytes (1, 2, 4, or 8)
         */
        public Builder ioPort(String name, int flags, int widthBytes) {
            ioPorts.add(new IoPortDescriptor(name, flags, widthBytes));
            return this;
        }

        /**
         * Declare a custom I/O port element (flip-flop, width defaults to 1).
         *
         * @param name  element name
         * @param flags bitmap of flags (should include FLIP_FLOP)
         */
        public Builder ioPort(String name, int flags) {
            ioPorts.add(new IoPortDescriptor(name, flags));
            return this;
        }

        public DeviceDescriptor build() {
            return new DeviceDescriptor(this);
        }
    }
}
