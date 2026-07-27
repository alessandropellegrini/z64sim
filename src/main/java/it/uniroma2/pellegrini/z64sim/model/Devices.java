/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.devices.Dmac;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton registry of devices bound to IVT entries.
 * Serves as the table model for the IVT table in the GUI.
 * <p>
 * The z64 IVT has 256 entries (IVN 0–255), each holding a 64-bit pointer
 * to a driver routine. This class tracks which device (if any) is bound
 * to each IVN.
 */
public class Devices extends AbstractTableModel {
    /** Maximum number of interrupt vectors (same as x86 real mode). */
    public static final int IVT_ENTRIES = 256;

    private static volatile Devices instance = null;

    /** One slot per IVN; null means no device is bound to that vector. */
    private final DeviceMapping[] devices = new DeviceMapping[IVT_ENTRIES];

    /**
     * Global IRQ counter — incremented by {@link #raiseIRQ()},
     * decremented by {@link #clearIRQ()}. Multiple devices can have
     * pending interrupts simultaneously.
     */
    private final AtomicInteger irqCount = new AtomicInteger(0);

    /**
     * Interrupt-capable devices in daisy-chain order (registration order).
     * A future GUI table will determine the order.
     */
    private final List<DeviceMapping> interruptChain = new ArrayList<>();

    /**
     * All registered devices in table/daisy-chain order.
     * Maintained by registerDevice/unregisterDevice/clearAllDevices.
     */
    private final List<DeviceMapping> allDevicesOrdered = new ArrayList<>();

    // ---- DMAC (permanent, hardwired) ----

    private Dmac dmac;
    private DeviceMapping dmacMapping;

    private Devices() {
        initDmac();
    }

    /**
     * Initialise the DMAC coprocessor. Always present, IVN = 0,
     * ports 0x00–0x08 hardwired.
     */
    private void initDmac() {
        dmac = new Dmac();
        dmacMapping = new DeviceMapping(dmac, 0);

        // Hardwire DMAC ports at 0x00–0x08
        int port = Dmac.PORT_BASE;
        for (IoPortDescriptor desc : dmac.getDescriptor().getIoPorts()) {
            dmacMapping.assignPort(port, desc);
            portMap.put((long) port, dmacMapping);
            port++;
        }

        devices[0] = dmacMapping;
        interruptChain.add(dmacMapping);
        allDevicesOrdered.add(dmacMapping);
    }

    /**
     * Return the DMAC coprocessor instance.
     */
    public Dmac getDmac() {
        return dmac;
    }

    public static synchronized Devices getInstance() {
        if (instance == null)
            instance = new Devices();
        return instance;
    }

    /**
     * Bind a device to the given IVN.
     * Non-interrupt devices may pass {@code ivn = -1}; they are tracked
     * for lifecycle management but do not occupy an IVT slot.
     *
     * @param ivn     interrupt vector number (0–255), or -1 for non-interrupt devices
     * @param mapping the device mapping to register
     */
    public void registerDevice(int ivn, DeviceMapping mapping) {
        allDevicesOrdered.add(mapping);
        // Only interrupt-capable devices occupy IVT slots
        if (mapping.getDevice().getDescriptor().isInterruptsEnabled()) {
            if (ivn < 0 || ivn >= IVT_ENTRIES)
                throw new IllegalArgumentException("IVN out of range: " + ivn);
            devices[ivn] = mapping;
            interruptChain.add(mapping);
            fireTableRowsUpdated(ivn, ivn);
        }
    }

    /**
     * Return the device mapping bound to the given IVN, or null if none.
     */
    public DeviceMapping getDevice(int ivn) {
        if (ivn < 0 || ivn >= IVT_ENTRIES)
            throw new IllegalArgumentException("IVN out of range: " + ivn);
        return devices[ivn];
    }

    /**
     * Return all registered devices in their current daisy-chain/table order.
     */
    public List<DeviceMapping> getAllDevicesOrdered() {
        return Collections.unmodifiableList(allDevicesOrdered);
    }

    /**
     * Remove all device registrations, ports, and reset IRQ state.
     * Used by DeviceManager when confirming a new configuration.
     */
    public void clearAllDevices() {
        for (int i = 0; i < IVT_ENTRIES; i++) {
            devices[i] = null;
        }
        portMap.clear();
        interruptChain.clear();
        allDevicesOrdered.clear();
        irqCount.set(0);

        // Re-register the DMAC — it is permanent and cannot be removed
        initDmac();

        fireTableDataChanged();
    }

    // ---- I/O port-to-device mapping ----

    /** Maps I/O port addresses to DeviceMapping instances. */
    private final Map<Long, DeviceMapping> portMap = new HashMap<>();

    /**
     * Register a device mapping on a specific I/O port.
     *
     * @param port    the I/O port address
     * @param mapping the device mapping responding to this port
     */
    public void registerPort(long port, DeviceMapping mapping) {
        portMap.put(port, mapping);
    }

    /**
     * Look up the device mapping registered for the given I/O port.
     *
     * @param port the I/O port address
     * @return the DeviceMapping, or null if no device is registered on this port
     */
    public DeviceMapping getMappingForPort(long port) {
        return portMap.get(port);
    }

    // ---- Interrupt support ----

    /**
     * Called by {@link Device#raiseInterrupt()}.
     * Increments the global IRQ counter.
     */
    public void raiseIRQ() {
        irqCount.incrementAndGet();
    }

    /**
     * Called when a driver clears INT_REQ on a device.
     * Decrements the global IRQ counter.
     */
    public void clearIRQ() {
        irqCount.updateAndGet(c -> c > 0 ? c - 1 : 0);
    }

    /**
     * Check if any device has a pending interrupt.
     *
     * @return true if {@code irqCount > 0}
     */
    public boolean isIRQPending() {
        return irqCount.get() > 0;
    }

    /**
     * Poll the daisy chain for the first device with INT_REQ set.
     * Devices are queried in registration order (daisy-chain order).
     *
     * @return the first DeviceMapping with a pending interrupt, or null if none
     */
    public DeviceMapping pollInterrupt() {
        for (DeviceMapping mapping : interruptChain) {
            if (mapping.hasInterruptRequest()) {
                return mapping;
            }
        }
        return null;
    }

    // ---- Simulation lifecycle notifications ----

    /**
     * Notify all registered devices that the simulation has started.
     */
    public void notifySimulationStart() {
        for (DeviceMapping mapping : allDevicesOrdered) {
            mapping.getDevice().onSimulationStart();
        }
    }

    /**
     * Notify all registered devices that the simulation has stopped.
     */
    public void notifySimulationStop() {
        for (DeviceMapping mapping : allDevicesOrdered) {
            mapping.getDevice().onSimulationStop();
        }
    }

    // ---- AbstractTableModel implementation ----

    @Override
    public int getRowCount() {
        return IVT_ENTRIES;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int col) {
        return col == 0
                ? PropertyBroker.getMessageFromBundle("ivt.table.ivn")
                : PropertyBroker.getMessageFromBundle("ivt.table.device");
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return String.valueOf(row);
        } else {
            DeviceMapping dev = devices[row];
            return dev != null ? dev.toString() : "\u2014"; // em-dash placeholder
        }
    }
}
