/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Binds a {@link Device} to an IVN and maps I/O port addresses to specific
 * elements on the device's interface.
 * <p>
 * The mapping has two levels:
 * <ol>
 *   <li>{@link Devices} maps a port address to a {@code DeviceMapping}</li>
 *   <li>{@code DeviceMapping} maps the port to the specific {@link IoPortDescriptor}
 *       element and delegates to the {@link Device}</li>
 * </ol>
 */
public class DeviceMapping {

    private final Device device;
    private final int ivn;
    private final Map<Long, IoPortDescriptor> portBindings = new HashMap<>();

    /**
     * Create a mapping for a device bound to an IVN.
     *
     * @param device the device implementation
     * @param ivn    interrupt vector number (0–255)
     */
    public DeviceMapping(Device device, int ivn) {
        this.device = device;
        this.ivn = ivn;
    }

    /**
     * Bind an I/O port address to a device element.
     *
     * @param port    the I/O port address
     * @param element the element descriptor this port maps to
     */
    public void assignPort(long port, IoPortDescriptor element) {
        portBindings.put(port, element);
    }

    /**
     * Called on an {@code in} instruction. Dispatches to the device
     * by element name.
     *
     * @param port the I/O port address
     * @return the value read from the device element, or 0 if unmapped
     */
    public long read(long port) {
        IoPortDescriptor element = portBindings.get(port);
        if (element == null) return 0;
        return device.readPort(element.getName());
    }

    /**
     * Called on an {@code out} instruction. Dispatches to the device
     * by element name.
     *
     * @param port the I/O port address
     * @param data the value to write
     */
    public void write(long port, long data) {
        IoPortDescriptor element = portBindings.get(port);
        if (element == null) return;
        device.writePort(element.getName(), data);
    }

    public Device getDevice() {
        return device;
    }

    public int getIvn() {
        return ivn;
    }

    public Map<Long, IoPortDescriptor> getPortBindings() {
        return portBindings;
    }

    /**
     * Find the I/O port address assigned to a named element on this device.
     * <p>
     * This is used by the DMAC to emulate the hardware WAIT blocking protocol:
     * in real hardware, the WAIT signal is a direct wire from the device's STATUS
     * flip-flop to the DMAC's CU. In the simulator, the DMAC resolves the STATUS
     * port address via this method and interacts through the standard I/O mechanism.
     *
     * @param elementName element name (e.g., "STATUS", "DATA_IN")
     * @return the port address, or null if this element is not port-mapped
     */
    public Long getPortForElement(String elementName) {
        for (Map.Entry<Long, IoPortDescriptor> entry : portBindings.entrySet()) {
            if (entry.getValue().getName().equals(elementName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Check if this device has a pending interrupt request.
     * Used by {@link Devices#pollInterrupt()} for daisy-chain polling.
     *
     * @return true if the device's INT_REQ is set
     */
    public boolean hasInterruptRequest() {
        return device.getIntReq() != 0;
    }

    @Override
    public String toString() {
        return device.getDescriptor().getDeviceName();
    }
}
