/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/**
 * Abstract base class for device plugins.
 * <p>
 * Subclasses announce their hardware interface via {@link #getDescriptor()}
 * and register per-element read/write handlers in their constructor using
 * {@link #onRead(String, LongSupplier)} and {@link #onWrite(String, LongConsumer)}.
 * <p>
 * INT_REQ is managed by this base class. Subclasses call {@link #raiseInterrupt()}
 * to signal completion; the driver clears INT_REQ via an {@code out} instruction.
 * <p>
 * Example:
 * <pre>
 * public class MyDevice extends Device {
 *     private long status = 1;
 *     private long dataOut = 0;
 *
 *     public MyDevice() {
 *         onRead("STATUS", () -&gt; status);
 *         onRead("DATA_OUT", () -&gt; dataOut);
 *         onWrite("STATUS", data -&gt; {
 *             status = 0;
 *             scheduleAfterDelay(() -&gt; { process(); status = 1; raiseInterrupt(); }, 500);
 *         });
 *     }
 * }
 * </pre>
 */
public abstract class Device {

    private final Map<String, LongSupplier> readHandlers = new HashMap<>();
    private final Map<String, LongConsumer> writeHandlers = new HashMap<>();

    // ---- INT_REQ managed by base class ----

    /** Internal INT_REQ state. Set by raiseInterrupt(), cleared by driver via I/O write. */
    private volatile long intReq = 0;

    /**
     * Constructor auto-registers INT_REQ read/write handlers.
     * These handlers are only reachable if the descriptor declares
     * {@code .interrupts()}, which creates the IoPortDescriptor that
     * can be port-mapped. Without it, no port maps to "INT_REQ" and
     * the handlers are never invoked.
     */
    protected Device() {
        readHandlers.put("INT_REQ", () -> intReq);
        writeHandlers.put("INT_REQ", data -> {
            if (intReq != 0) {
                intReq = 0;
                Devices.getInstance().clearIRQ();
                onIntReqCleared();
            }
        });
    }

    /** Announce the device's hardware interface. */
    public abstract DeviceDescriptor getDescriptor();

    /**
     * Signal an interrupt request. Sets the internal INT_REQ flip-flop
     * and increments the global IRQ counter. Single call — mirrors
     * the hardware wired-OR of all INT_REQ outputs onto the IRQ line.
     */
    protected void raiseInterrupt() {
        intReq = 1;
        Devices.getInstance().raiseIRQ();
    }

    /**
     * Query the INT_REQ state. Public for future plugin JAR compatibility
     * (external packages cannot access package-private methods).
     *
     * @return 1 if an interrupt is pending, 0 otherwise
     */
    public long getIntReq() {
        return intReq;
    }

    /**
     * Called when the driver clears INT_REQ (writes 0 via I/O port).
     * Override for mode 2 devices where clearing INT_REQ auto-restarts
     * the device. Default implementation does nothing.
     */
    protected void onIntReqCleared() {
    }

    // ---- Per-element handler registration ----

    /**
     * Register a read handler for a named element.
     *
     * @param name    element name (must match a name in the descriptor)
     * @param handler supplies the current value when the CPU reads this element
     */
    protected void onRead(String name, LongSupplier handler) {
        readHandlers.put(name, handler);
    }

    /**
     * Register a write handler for a named element.
     *
     * @param name    element name (must match a name in the descriptor)
     * @param handler consumes the value when the CPU writes to this element
     */
    protected void onWrite(String name, LongConsumer handler) {
        writeHandlers.put(name, handler);
    }

    /**
     * Internal dispatch — called by {@link DeviceMapping}.
     * Returns 0 if no handler is registered for this element.
     */
    long readPort(String name) {
        LongSupplier h = readHandlers.get(name);
        return h != null ? h.getAsLong() : 0;
    }

    /**
     * Internal dispatch — called by {@link DeviceMapping}.
     * No-op if no handler is registered for this element.
     */
    void writePort(String name, long data) {
        LongConsumer h = writeHandlers.get(name);
        if (h != null) h.accept(data);
    }

    // ---- DMA mode support ----

    /**
     * When true, {@link #scheduleAfterDelay(Runnable, int)} executes the action
     * synchronously instead of on a timer. This allows the DMAC to follow the
     * device's busy-waiting protocol within a single simulation step: each
     * STATUS write triggers immediate data production, emulating the hardware
     * behaviour where the DMAC waits for the WAIT signal to clear before
     * proceeding to the next word.
     */
    private volatile boolean dmaMode = false;

    /**
     * Enable or disable DMA mode on this device.
     * Called by the DMAC before/after a burst transfer.
     *
     * @param dma true to enable synchronous processing, false for normal async
     */
    public void setDmaMode(boolean dma) {
        this.dmaMode = dma;
    }

    // ---- Utilities ----

    /**
     * Schedule a delayed action on the EDT. Useful for simulating device
     * processing time in busy-waiting scenarios.
     * <p>
     * The action fires on the EDT after {@code delayMs} milliseconds.
     * During the delay, the CPU's polling loop sees the device as busy.
     * <p>
     * When DMA mode is active, the action executes immediately (synchronously)
     * to allow the DMAC to complete a burst transfer in a single simulation step.
     *
     * @param action  the action to execute when the delay expires
     * @param delayMs delay in milliseconds
     */
    protected void scheduleAfterDelay(Runnable action, int delayMs) {
        if (dmaMode) {
            action.run();
            return;
        }
        javax.swing.Timer timer = new javax.swing.Timer(delayMs, e -> action.run());
        timer.setRepeats(false);
        timer.start();
    }

    // ---- Simulation lifecycle ----

    /**
     * Called when the simulation starts. Override to create a device GUI
     * (e.g., an actuator display, LED panel, screen).
     * <p>
     * Default implementation does nothing.
     */
    protected void onSimulationStart() {
    }

    /**
     * Called when the simulation stops (halt, error, or user stop).
     * Override to dispose the device GUI.
     * <p>
     * Default implementation does nothing.
     */
    protected void onSimulationStop() {
    }

    @Override
    public String toString() {
        return getDescriptor().getDeviceName();
    }
}
