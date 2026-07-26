/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

/**
 * Programmable Timer Device — interrupt-driven.
 * <p>
 * The CPU writes a delay in milliseconds to the DELAY register,
 * then writes to STATUS to start the timer.  After the specified
 * number of milliseconds the device raises an interrupt.
 * <p>
 * I/O elements:
 * <ul>
 *   <li>DELAY (register, writable, 4 bytes) — delay in milliseconds</li>
 *   <li>STATUS (flip-flop, writable) — write to start the timer</li>
 *   <li>INT_REQ (flip-flop, writable) — interrupt request, managed by base class</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>CPU writes delay (ms) to DELAY</li>
 *   <li>CPU writes to STATUS (any value) --> timer starts</li>
 *   <li>After DELAY ms, device raises INT_REQ</li>
 *   <li>ISR clears INT_REQ via {@code outb} to the INT_REQ port</li>
 * </ol>
 */
public class Timer extends Device {

    private volatile long delayMs = 0;

    public Timer() {
        onWrite("DELAY", data -> delayMs = data & 0xFFFFFFFFL);

        onWrite("STATUS", data -> {
            int delay = (int) delayMs;
            if (delay > 0) {
                scheduleAfterDelay(this::raiseInterrupt, delay);
            }
        });
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Timer")
                .interrupts()
                .ioPort("STATUS",
                        IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE)
                .ioPort("DELAY",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 4)
                .build();
    }
}
