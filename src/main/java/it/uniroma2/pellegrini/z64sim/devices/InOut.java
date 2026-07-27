/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

import java.util.Random;

/**
 * Synchronous InOut Device — busy-waiting with MODE flip-flop.
 * <p>
 * This device has a single DATA register that can be both read and written.
 * A MODE flip-flop, connected to the CU, determines the device's behaviour:
 * <ul>
 *   <li>MODE = 0 (input mode): when STATUS is written, the device produces
 *       a new random 32-bit value in DATA. The CPU can then read DATA.</li>
 *   <li>MODE = 1 (output mode): the CPU writes a value to DATA, then writes
 *       STATUS to start the device. The device accepts the value.</li>
 * </ul>
 * <p>
 * I/O elements:
 * <ul>
 *   <li>STATUS (flip-flop, readable + writable) — 1 = ready, 0 = busy</li>
 *   <li>MODE (flip-flop, writable, connected to CU) — 0 = input, 1 = output</li>
 *   <li>DATA (register, readable + writable, 4 bytes) — the data register</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>CPU sets MODE via {@code out} to configure input or output</li>
 *   <li>In input mode: CPU writes STATUS → device produces value → CPU reads DATA</li>
 *   <li>In output mode: CPU writes DATA → CPU writes STATUS → device consumes value</li>
 * </ol>
 */
public class InOut extends Device {

    private static final int MIN_DELAY_MS = 200;
    private static final int MAX_DELAY_MS = 1000;

    private final Random random = new Random();

    /** STATUS: 1 = ready/idle, 0 = busy. */
    private volatile long status = 1;
    /** MODE: 0 = input, 1 = output. */
    private long mode = 0;
    /** DATA: the 32-bit data register. */
    private long data = 0;

    public InOut() {
        onRead("STATUS", () -> status);

        onWrite("STATUS", val -> {
            status = 0;  // busy
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            scheduleAfterDelay(() -> {
                if (mode == 0) {
                    // Input mode: produce a random value
                    do {
                        data = random.nextInt() & 0xFFFFFFFFL;
                    } while (data == 0);
                }
                // Output mode: value was already written to DATA — nothing to do
                status = 1;  // ready
            }, delay);
        });

        onWrite("MODE", val -> mode = val & 0x01L);

        onRead("DATA", () -> data);

        onWrite("DATA", val -> data = val & 0xFFFFFFFFL);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("InOut")
                .busyWaiting()
                .ioPort("MODE",
                        IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE
                      | IoPortDescriptor.CONNECTED_TO_CU)
                .ioPort("DATA",
                        IoPortDescriptor.REGISTER  | IoPortDescriptor.READABLE
                      | IoPortDescriptor.WRITABLE, 4)
                .build();
    }
}
