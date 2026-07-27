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
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
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
 * Integration test for DMAC (Direct Memory Access Controller).
 * <p>
 * Tests the {@code insb} instruction which programs the DMAC to perform a
 * burst transfer from a device data port into a memory buffer.
 * <p>
 * A mock device produces sequential byte values (0, 1, 2, ...) each time
 * it is started via its STATUS port, following the busy-waiting protocol.
 * The DMAC interacts with the device using DMA mode, which makes the
 * device process data synchronously.
 */
@DisplayName("DMAC integration test — insb burst transfer")
public class DmacTest {

    /** Number of bytes to transfer. */
    private static final int TRANSFER_COUNT = 8;

    private Program program;

    /**
     * A mock input device that produces sequential byte values.
     * <p>
     * Each time STATUS is written (device started), it produces the next
     * byte value in the sequence (0, 1, 2, ...). The value is available
     * on the DATA_OUT port after processing.
     */
    static class SequentialInputDevice extends Device {
        private long dataOut = 0;
        private int nextValue = 0;

        SequentialInputDevice() {
            // STATUS write: start device --> produce next value
            onWrite("STATUS", data -> scheduleAfterDelay(() -> {
                dataOut = nextValue & 0xFF;
                nextValue++;
            }, 100));

            // STATUS read: always ready (1) — we process synchronously in DMA mode
            onRead("STATUS", () -> 1L);

            // DATA_OUT read: return current value
            onRead("DATA_OUT", () -> dataOut);
        }

        @Override
        public DeviceDescriptor getDescriptor() {
            return new DeviceDescriptor.Builder("SequentialInputDevice")
                    .busyWaiting()
                    .ioPort("DATA_OUT",
                            IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 1)
                    .build();
        }
    }

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        // Assemble dmac.asm
        InputStream is = getClass().getResourceAsStream("/dmac.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed without errors: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        // Register mock device:
        //   Port 0x20 --> STATUS, Port 0x21 --> DATA_OUT
        //   (matches .equ DEV_DATA=0x21 in dmac.asm; STATUS at 0x20)
        SequentialInputDevice device = new SequentialInputDevice();
        DeviceMapping mapping = new DeviceMapping(device, -1);
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        DeviceDescriptor desc = device.getDescriptor();
        mapping.assignPort(0x20, desc.getIoPort("STATUS"));
        devices.registerPort(0x20, mapping);
        mapping.assignPort(0x21, desc.getIoPort("DATA_OUT"));
        devices.registerPort(0x21, mapping);

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
                "Expected Instruction at RIP=0x" + Long.toHexString(rip));
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();
        return instruction.getMnemonic();
    }

    @Test
    @DisplayName("insb transfers sequential bytes from device to memory buffer")
    public void testInsbTransfersDataToMemory() throws SimulatorException {
        final long BUFFER_ADDR = 0x800;

        // Verify buffer is initially zeroed
        for (int i = 0; i < TRANSFER_COUNT; i++) {
            assertEquals(0, Memory.getValueAt(BUFFER_ADDR + i) & 0xFF,
                    "Buffer byte " + i + " should initially be 0");
        }

        // Run program until hlt
        int maxSteps = 50;
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

        assertTrue(reachedHlt, "Program should reach hlt. Trace:\n"
                + String.join("\n", trace));

        // Verify the buffer contains sequential bytes (0, 1, 2, ..., 7)
        for (int i = 0; i < TRANSFER_COUNT; i++) {
            assertEquals(i, Memory.getValueAt(BUFFER_ADDR + i) & 0xFF,
                    "Buffer byte " + i + " should be " + i
                    + " after insb transfer");
        }

        // Verify %rdi was advanced by TRANSFER_COUNT
        long rdi = SimulatorController.getCpuState().getRegisterValue(Register.RDI);
        assertEquals(BUFFER_ADDR + TRANSFER_COUNT, rdi,
                "%rdi should be buffer + " + TRANSFER_COUNT + " after forward insb");

        // Verify %rcx was zeroed
        long rcx = SimulatorController.getCpuState().getRegisterValue(Register.RCX);
        assertEquals(0, rcx, "%rcx should be 0 after insb transfer");
    }

    @Test
    @DisplayName("DMAC is always registered at IVN 0 with hardwired ports")
    public void testDmacAlwaysPresent() {
        Devices devices = Devices.getInstance();

        // DMAC should be at IVN 0
        DeviceMapping dmacMapping = devices.getDevice(0);
        assertNotNull(dmacMapping, "DMAC should be registered at IVN 0");
        assertEquals("DMAC", dmacMapping.toString(),
                "Device at IVN 0 should be the DMAC");

        // DMAC ports 0x00–0x07 should be mapped
        for (int port = 0x00; port <= 0x07; port++) {
            DeviceMapping m = devices.getMappingForPort(port);
            assertNotNull(m, "Port 0x" + Integer.toHexString(port)
                    + " should be mapped to DMAC");
            assertEquals("DMAC", m.toString(),
                    "Port 0x" + Integer.toHexString(port)
                    + " should map to DMAC");
        }

        // getDmac() should return the DMAC instance
        assertNotNull(devices.getDmac(), "getDmac() should return the DMAC");
    }

    @Test
    @DisplayName("clearAllDevices preserves DMAC registration")
    public void testClearPreservesDmac() {
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        // DMAC should still be at IVN 0
        DeviceMapping dmacMapping = devices.getDevice(0);
        assertNotNull(dmacMapping, "DMAC should survive clearAllDevices");
        assertEquals("DMAC", dmacMapping.toString());

        // DMAC ports should still be mapped
        for (int port = 0x00; port <= 0x07; port++) {
            assertNotNull(devices.getMappingForPort(port),
                    "DMAC port 0x" + Integer.toHexString(port)
                    + " should survive clearAllDevices");
        }
    }
}
