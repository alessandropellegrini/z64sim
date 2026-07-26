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
 * Asynchronous Input Device — interrupt-driven, 64-bit input register.
 * <p>
 * When the CPU writes to STATUS, the device starts processing.
 * After a random delay it produces a random 64-bit value in DATA_IN
 * and raises an interrupt to notify the CPU.
 * <p>
 * I/O elements:
 * <ul>
 *   <li>STATUS (flip-flop, writable) — write to start the device</li>
 *   <li>INT_REQ (flip-flop, writable) — interrupt request, managed by base class</li>
 *   <li>DATA_IN (register, readable, 8 bytes) — the 64-bit input value</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>CPU writes to STATUS (any value) --> device starts</li>
 *   <li>After a random delay, device stores a random value in DATA_IN
 *       and raises INT_REQ</li>
 *   <li>ISR reads DATA_IN, then clears INT_REQ via an {@code outb} to the
 *       INT_REQ port</li>
 * </ol>
 */
public class AsyncInput extends Device {

    private static final int MIN_DELAY_MS = 200;
    private static final int MAX_DELAY_MS = 1000;

    private final Random random = new Random();
    private volatile long dataIn = 0;

    public AsyncInput() {
        onWrite("STATUS", data -> {
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            scheduleAfterDelay(() -> {
                dataIn = random.nextLong();
                raiseInterrupt();
            }, delay);
        });

        onRead("DATA_IN", () -> dataIn);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Async Input")
                .interrupts()
                .ioPort("STATUS",
                        IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE)
                .ioPort("DATA_IN",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 8)
                .build();
    }
}
