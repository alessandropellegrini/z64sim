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
 * Asynchronous Output Device — interrupt-driven, 32-bit output register.
 * <p>
 * The CPU writes a 32-bit value to DATA_OUT, then writes to STATUS
 * to start the device. After a random delay the device signals
 * completion by raising an interrupt.
 * <p>
 * I/O elements:
 * <ul>
 *   <li>STATUS (flip-flop, writable) — write to start the device</li>
 *   <li>INT_REQ (flip-flop, writable) — interrupt request, managed by base class</li>
 *   <li>DATA_OUT (register, writable, 4 bytes) — the 32-bit output value</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>CPU writes the value to DATA_OUT</li>
 *   <li>CPU writes to STATUS (any value) --> device starts processing</li>
 *   <li>After a random delay, device raises INT_REQ to signal completion</li>
 *   <li>ISR clears INT_REQ via an {@code outb} to the INT_REQ port</li>
 * </ol>
 */
public class AsyncOutput extends Device {

    private static final int MIN_DELAY_MS = 200;
    private static final int MAX_DELAY_MS = 1000;

    private final Random random = new Random();

    public AsyncOutput() {
        onWrite("DATA_OUT", data -> {
            // accept the value — nothing else to do
        });

        onWrite("STATUS", data -> {
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            scheduleAfterDelay(this::raiseInterrupt, delay);
        });
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Async Output")
                .interrupts()
                .ioPort("STATUS",
                        IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE)
                .ioPort("DATA_OUT",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 4)
                .build();
    }
}
