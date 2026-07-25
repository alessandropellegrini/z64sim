/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;
import it.uniroma2.pellegrini.z64sim.model.Memory;

/**
 * Direct Memory Access Controller (DMAC) coprocessor.
 * <p>
 * The DMAC transfers data between memory and I/O devices autonomously,
 * without requiring the CPU to handle each byte. It can be programmed
 * either via the {@code ins}/{@code outs} instructions (which configure
 * it from implicit operands and trigger a burst transfer) or manually
 * via {@code out} instructions to its hardwired I/O ports.
 * <p>
 * The DMAC is always present in the system, bound to IVN 0, with ports
 * hardwired at 0x00–0x08. It cannot be removed or reconfigured by the user.
 * <p>
 * All transfers are burst mode: the entire transfer completes in a single
 * simulation step. When the DMAC interacts with a target device, it
 * follows the busy-waiting protocol by writing the device's STATUS port
 * and reading/writing the data port. DMA mode is enabled on the target
 * device so that processing callbacks fire synchronously.
 */
public class Dmac extends Device {

    /** First I/O port address (CAR). */
    public static final int PORT_BASE = 0x00;
    /** One past the last I/O port address. DMAC occupies 0x00–0x07. */
    public static final int PORT_END = 0x08;

    // ---- DMAC internal registers ----

    /** Current Address Register — 24-bit effective memory address. */
    private long car = 0;
    /** Element size code: 0=byte, 1=word (2B), 2=longword (4B). */
    private long size = 0;
    /** Direction: 0=forward (increment CAR), 1=backward (decrement CAR). */
    private long dir = 0;
    /** I/O direction: 0=device-->memory (input), 1=memory-->device (output). */
    private long ioDir = 0;
    /** Target device's data I/O port address. */
    private long ioport = 0;
    /** Word count — 14-bit effective, max 16383. */
    private long wc = 0;
    /** STATUS: 1=idle, 0=busy. */
    private long status = 1;

    public Dmac() {
        // Register write handlers for DMAC configuration registers
        onWrite("CAR", data -> car = data & 0x00FFFFFFL);       // 24-bit
        onWrite("SIZE", data -> size = data & 0x03L);            // 2-bit
        onWrite("DIR", data -> dir = data & 0x01L);              // 1-bit
        onWrite("IO_DIR", data -> ioDir = data & 0x01L);         // 1-bit
        onWrite("IOPORT", data -> ioport = data & 0xFFFFL);      // 16-bit
        onWrite("WC", data -> wc = data & 0x3FFFL);              // 14-bit

        // STATUS read: returns idle/busy state
        onRead("STATUS", () -> status);

        // STATUS write: writing 0 starts the transfer (manual programming)
        onWrite("STATUS", data -> {
            if (data == 0 && status == 1) {
                executeTransfer(true);
            }
        });
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("DMAC")
                .busyWaiting()
                .interrupts()
                .ioPort("CAR",    IoPortDescriptor.REGISTER  | IoPortDescriptor.WRITABLE, 4)
                .ioPort("SIZE",   IoPortDescriptor.REGISTER  | IoPortDescriptor.WRITABLE, 1)
                .ioPort("DIR",    IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE
                                                             | IoPortDescriptor.CONNECTED_TO_CU)
                .ioPort("IO_DIR", IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE
                                                             | IoPortDescriptor.CONNECTED_TO_CU)
                .ioPort("IOPORT", IoPortDescriptor.REGISTER  | IoPortDescriptor.WRITABLE, 2)
                .ioPort("WC",     IoPortDescriptor.REGISTER  | IoPortDescriptor.WRITABLE, 2)
                .build();
    }

    /**
     * Configure and execute a transfer, called by {@code ins}/{@code outs} instructions.
     * The transfer completes synchronously and does not raise an interrupt.
     *
     * @param car       starting memory address (24-bit effective)
     * @param ioport    target device's data port address
     * @param wc        number of elements to transfer
     * @param sizeCode  element size: 1=byte, 2=word, 4=longword
     * @param dir       direction flag: false=forward, true=backward
     * @param ioDir     I/O direction: 0=device-->memory (input), 1=memory-->device (output)
     */
    public void transfer(long car, long ioport, long wc, int sizeCode, boolean dir, int ioDir) {
        this.car = car & 0x00FFFFFFL;
        this.ioport = ioport & 0xFFFFL;
        this.wc = wc & 0x3FFFL;
        // Convert transfer size in bytes to size code: 1-->0, 2-->1, 4-->2
        switch (sizeCode) {
            case 1: this.size = 0; break;
            case 2: this.size = 1; break;
            case 4: this.size = 2; break;
            default: this.size = 0; break;
        }
        this.dir = dir ? 1 : 0;
        this.ioDir = ioDir;

        executeTransfer(false);
    }

    /**
     * Execute the DMA burst transfer using the currently configured registers.
     * <p>
     * For each word, the DMAC interacts with the target device following the
     * busy-waiting protocol. The target device's STATUS port is resolved via
     * {@link DeviceMapping#getPortForElement(String)}, which emulates the
     * hardware WAIT blocking signal — in real hardware, the WAIT line is a
     * direct wire from the device's STATUS flip-flop to the DMAC's CU.
     *
     * @param raiseIrq if true, raise an interrupt on completion (manual programming)
     */
    private void executeTransfer(boolean raiseIrq) {
        status = 0;  // busy

        DeviceMapping targetMapping = Devices.getInstance().getMappingForPort(ioport);
        if (targetMapping == null) {
            status = 1;
            return;
        }

        Device targetDevice = targetMapping.getDevice();
        targetDevice.setDmaMode(true);

        int elementSize = 1 << (int) size;  // 0-->1B, 1-->2B, 2-->4B

        // Resolve the target device's STATUS port address.
        // This emulates the hardware WAIT blocking protocol: in real hardware,
        // the WAIT signal is a direct wire from the device's STATUS flip-flop
        // to the DMAC's CU. In the simulator, we resolve the STATUS port by
        // name and interact through the standard I/O mechanism.
        Long statusPort = targetMapping.getPortForElement("STATUS");

        long currentCar = car;
        int count = (int) wc;

        for (int i = 0; i < count; i++) {
            if (ioDir == 0) {
                // Device --> memory (input)
                // Start the device — in dmaMode, processing fires immediately
                if (statusPort != null) {
                    targetMapping.write(statusPort, 0);
                }
                // Read data from the device's data port
                long value = targetMapping.read(ioport);
                // Write to memory
                writeToMemory(currentCar, value, elementSize);
            } else {
                // Memory --> device (output)
                // Read data from memory
                long value = readFromMemory(currentCar, elementSize);
                // Write data to the device's data port
                targetMapping.write(ioport, value);
                // Start the device to consume the data
                if (statusPort != null) {
                    targetMapping.write(statusPort, 0);
                }
            }
            currentCar += dir == 1 ? -elementSize : elementSize;
        }

        // Update CAR to reflect final position
        car = currentCar & 0x00FFFFFFL;

        targetDevice.setDmaMode(false);
        status = 1;  // idle

        if (raiseIrq) {
            raiseInterrupt();
        }
    }

    /**
     * Write a value to memory in little-endian byte order.
     */
    private void writeToMemory(long address, long value, int numBytes) {
        for (int i = 0; i < numBytes; i++) {
            Memory.setValueAt(address + i, (byte) (value >> (i * 8)));
        }
    }

    /**
     * Read a value from memory in little-endian byte order.
     */
    private long readFromMemory(long address, int numBytes) {
        long value = 0;
        for (int i = 0; i < numBytes; i++) {
            value |= ((long) (Memory.getValueAt(address + i) & 0xFF)) << (i * 8);
        }
        return value;
    }
}
