/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

import java.util.Random;

/**
 * Synchronous Input Device — busy-waiting, 32-bit input register.
 * <p>
 * This device produces a random 32-bit value on each activation.
 * When the CPU writes to STATUS, the device generates a new random
 * number, stores it in DATA_IN, and sets STATUS to 1 (ready).
 * <p>
 * I/O elements:
 * <ul>
 *   <li>STATUS (flip-flop, readable + writable) — 1 when data is available, 0 otherwise</li>
 *   <li>DATA_IN (register, readable, 4 bytes) — the 32-bit random value</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>CPU writes to STATUS (any value) --> device generates a random number</li>
 *   <li>CPU polls STATUS via busy-waiting until it reads 1 (data available)</li>
 *   <li>CPU reads DATA_IN to get the 32-bit value</li>
 * </ol>
 */
public class SyncInput extends Device {

    private final Random random = new Random();

    private static final int MIN_DELAY_MS = 200;
    private static final int MAX_DELAY_MS = 1000;

    private volatile long status = 0;
    private volatile long dataIn = 0;

    public SyncInput() {
        onRead("STATUS", () -> status);

        onWrite("STATUS", data -> {
            status = 0;
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            scheduleAfterDelay(() -> {
                dataIn = random.nextInt() & 0xFFFFFFFFL;
                status = 1;
            }, delay);
        });

        onRead("DATA_IN", () -> dataIn);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Sync Input")
                .busyWaiting()
                .ioPort("DATA_IN",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 4)
                .build();
    }
}
