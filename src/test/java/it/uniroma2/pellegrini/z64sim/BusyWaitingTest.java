/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for busy-waiting device I/O.
 * <p>
 * Assembles busywaiting.asm and runs it with a mock device that:
 * <ul>
 *   <li>On STATUS write: starts processing (clears STATUS to 0)</li>
 *   <li>On STATUS read: returns 0 while busy, 1 when done</li>
 *   <li>On DATA_OUT read: returns the computed result</li>
 * </ul>
 * The device completes after a configurable number of STATUS polls.
 */
@DisplayName("Busy-waiting integration test — busywaiting.asm")
public class BusyWaitingTest {

    /** Known value the mock device will produce. */
    private static final long MOCK_RESULT = 0xCAFE1234L;

    /** Number of STATUS reads (polls) before the device finishes. */
    private static final int POLLS_BEFORE_DONE = 3;

    private Program program;

    /**
     * A mock device for busy-waiting. It has two I/O elements:
     * STATUS (flip-flop, readable + writable) and DATA_OUT (register, readable, 4 bytes).
     * <p>
     * When the CPU writes to STATUS, the device starts "processing" (STATUS reads
     * as 0). After {@link #POLLS_BEFORE_DONE} reads of STATUS, the device is
     * done and STATUS reads as 1. DATA_OUT returns the result when done.
     */
    static class BusyWaitDevice extends Device {
        private int pollCount = 0;
        private boolean started = false;

        BusyWaitDevice() {
            // STATUS write handler: start processing
            onWrite("STATUS", data -> {
                started = true;
                pollCount = 0;
            });

            // STATUS read handler: return 0 while busy, 1 when done
            onRead("STATUS", () -> {
                if (!started) return 0;
                pollCount++;
                return pollCount >= POLLS_BEFORE_DONE ? 1L : 0L;
            });

            // DATA_OUT read handler: return the result
            onRead("DATA_OUT", () -> MOCK_RESULT);
        }

        @Override
        public DeviceDescriptor getDescriptor() {
            return new DeviceDescriptor.Builder("BusyWaitDevice")
                    .busyWaiting()    // adds STATUS flip-flop (readable + writable)
                    .ioPort("DATA_OUT",
                            IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 4)
                    .build();
        }
    }

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        // Assemble busywaiting.asm
        InputStream is = getClass().getResourceAsStream("/busywaiting.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed without errors: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        // Register mock device (no interrupts, busy-waiting only):
        //   Port mapping: STATUS --> 0x10, DATA_OUT --> 0x11
        //   (matches .equ DEV_STATUS=0x10, DEV_REG=0x11)
        BusyWaitDevice device = new BusyWaitDevice();
        DeviceMapping mapping = new DeviceMapping(device, 0);  // IVN unused for busy-waiting
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        DeviceDescriptor desc = device.getDescriptor();
        // Map STATUS to port 0x10
        mapping.assignPort(0x10, desc.getIoPort("STATUS"));
        devices.registerPort(0x10, mapping);
        // Map DATA_OUT to port 0x11
        mapping.assignPort(0x11, desc.getIoPort("DATA_OUT"));
        devices.registerPort(0x11, mapping);

        devices.registerDevice(-1, mapping);
    }

    /**
     * Fetch and execute one instruction, returning its mnemonic.
     */
    private String stepAndGetMnemonic() throws SimulatorException {
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertNotNull(element, "Expected element at RIP=0x" + Long.toHexString(rip));
        assertInstanceOf(Instruction.class, element,
                "Expected Instruction at RIP=0x" + Long.toHexString(rip)
                + " but found " + element.getClass().getSimpleName());
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();
        return instruction.getMnemonic();
    }

    private long readQword(long address) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (Memory.getValueAt(address + i) & 0xFF)) << (i * 8);
        }
        return value;
    }

    @Test
    @DisplayName("Busy-wait loop polls device, reads result, and stores it")
    public void testBusyWaitingReadsDeviceValue() throws SimulatorException {
        // Memory layout per .org 0x800:
        //   done:   .byte at 0x800 (1 byte, initially 0)
        //   result: .long at 0x801 (4 bytes, initially 0)
        final long DONE_ADDR   = 0x800;
        final long RESULT_ADDR = 0x801;

        // Verify initial state
        assertEquals(0, Memory.getValueAt(DONE_ADDR) & 0xFF,
                "done should initially be 0");

        int maxSteps = 100;
        boolean reachedHlt = false;
        List<String> trace = new ArrayList<>();
        for (int i = 0; i < maxSteps; i++) {
            long rip = SimulatorController.getCpuState().getRIP();
            String mnemonic = stepAndGetMnemonic();
            trace.add(String.format("0x%x: %s", rip, mnemonic));
            if (mnemonic.equals("hlt")) {
                reachedHlt = true;
                break;
            }
        }

        assertTrue(reachedHlt, "Program should reach hlt within " + maxSteps
                + " steps. Trace:\n" + String.join("\n", trace));

        // Verify 'done' was set to 1
        assertEquals(1, Memory.getValueAt(DONE_ADDR) & 0xFF,
                "done should be 1 after program completion");

        // Verify 'result' contains the device's output value
        long storedResult = readQword(RESULT_ADDR) & 0xFFFFFFFFL;
        assertEquals(MOCK_RESULT, storedResult,
                "result should contain the device's output value 0x"
                + Long.toHexString(MOCK_RESULT));

        // Verify the busy-wait loop iterated (trace should contain multiple 'in' instructions)
        long inCount = trace.stream().filter(s -> s.contains(": in")).count();
        assertTrue(inCount >= POLLS_BEFORE_DONE,
                "Expected at least " + POLLS_BEFORE_DONE + " in instructions (polls), got " + inCount);
    }
}
