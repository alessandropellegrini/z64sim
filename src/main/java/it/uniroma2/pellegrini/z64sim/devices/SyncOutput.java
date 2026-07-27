/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

/**
 * Synchronous Output Device — busy-waiting, 64-bit output register.
 * <p>
 * The CPU writes a 64-bit value to DATA_OUT and then writes to STATUS
 * to start the device. The device accepts the value immediately
 * (STATUS reads as 1 right away).
 * <p>
 * I/O elements:
 * <ul>
 *   <li>STATUS (flip-flop, readable + writable) — always 1 (ready)</li>
 *   <li>DATA_OUT (register, writable, 8 bytes) — the 64-bit output value</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>CPU writes the value to DATA_OUT</li>
 *   <li>CPU writes to STATUS (any value) --> device accepts the value</li>
 *   <li>CPU polls STATUS via busy-waiting — always reads 1 (done)</li>
 * </ol>
 */
public class SyncOutput extends Device {

    private static final Logger log = LoggerFactory.getLogger();

    private static final int MIN_DELAY_MS = 200;
    private static final int MAX_DELAY_MS = 1000;

    private final java.util.Random random = new java.util.Random();
    private volatile long status = 1;  // 1 = ready, 0 = busy
    private long dataOut = 0;

    public SyncOutput() {
        onRead("STATUS", () -> status);

        onWrite("STATUS", data -> {
            status = 0;  // busy
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            scheduleAfterDelay(() -> status = 1, delay);
        });

        onWrite("DATA_OUT", data -> {
            dataOut = data;
            if (dataOut == 42) {
                log.info("What is the question?");
            }
        });
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Sync Output")
                .busyWaiting()
                .ioPort("DATA_OUT",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 8)
                .build();
    }
}
