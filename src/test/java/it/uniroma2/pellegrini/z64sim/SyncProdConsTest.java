/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
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
 * Integration test for synchronous producer/consumer I/O.
 * <p>
 * Assembles sync_prodcons.asm with a mock SyncInput (producer) that
 * returns a known value, and a mock SyncOutput (consumer) that
 * captures the written value. Verifies the value flows through.
 */
@DisplayName("Sync producer/consumer — sync_prodcons.asm")
public class SyncProdConsTest {

    private static final long MOCK_VALUE = 0xBAADF00DL;

    private Program program;
    private MockSyncOutput mockOutput;

    // --- Mock devices (test-only) ---

    static class MockSyncInput extends Device {
        private long status = 0;

        MockSyncInput() {
            onRead("STATUS", () -> status);
            onWrite("STATUS", data -> status = 1);
            onRead("DATA_IN", () -> MOCK_VALUE);
        }

        @Override
        public DeviceDescriptor getDescriptor() {
            return new DeviceDescriptor.Builder("MockSyncInput")
                    .busyWaiting()
                    .ioPort("DATA_IN",
                            IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 4)
                    .build();
        }
    }

    static class MockSyncOutput extends Device {
        private long status = 0;
        long capturedValue = 0;

        MockSyncOutput() {
            onRead("STATUS", () -> status);
            onWrite("STATUS", data -> status = 1);
            onWrite("DATA_OUT", data -> capturedValue = data);
        }

        @Override
        public DeviceDescriptor getDescriptor() {
            return new DeviceDescriptor.Builder("MockSyncOutput")
                    .busyWaiting()
                    .ioPort("DATA_OUT",
                            IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 4)
                    .build();
        }
    }

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/sync_prodcons.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        // Register mock devices
        MockSyncInput mockInput = new MockSyncInput();
        mockOutput = new MockSyncOutput();

        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        // Input device: STATUS@0x10, DATA_IN@0x11
        DeviceMapping inMapping = new DeviceMapping(mockInput, 0);
        DeviceDescriptor inDesc = mockInput.getDescriptor();
        inMapping.assignPort(0x10, inDesc.getIoPort("STATUS"));
        devices.registerPort(0x10, inMapping);
        inMapping.assignPort(0x11, inDesc.getIoPort("DATA_IN"));
        devices.registerPort(0x11, inMapping);
        devices.registerDevice(-1, inMapping);

        // Output device: STATUS@0x20, DATA_OUT@0x21
        DeviceMapping outMapping = new DeviceMapping(mockOutput, 0);
        DeviceDescriptor outDesc = mockOutput.getDescriptor();
        outMapping.assignPort(0x20, outDesc.getIoPort("STATUS"));
        devices.registerPort(0x20, outMapping);
        outMapping.assignPort(0x21, outDesc.getIoPort("DATA_OUT"));
        devices.registerPort(0x21, outMapping);
        devices.registerDevice(-1, outMapping);
    }

    private String stepAndGetMnemonic() throws SimulatorException {
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertNotNull(element, "Expected element at RIP=0x" + Long.toHexString(rip));
        assertInstanceOf(Instruction.class, element);
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();
        return instruction.getMnemonic();
    }

    private long readDword(long address) {
        long value = 0;
        for (int i = 0; i < 4; i++) {
            value |= ((long) (Memory.getValueAt(address + i) & 0xFF)) << (i * 8);
        }
        return value;
    }

    @Test
    @DisplayName("Value flows from SyncInput producer to SyncOutput consumer")
    public void testSyncProdCons() throws SimulatorException {
        // Memory layout: value(.long)@0x800, done(.byte)@0x804
        final long VALUE_ADDR = 0x800;
        final long DONE_ADDR  = 0x804;

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

        assertTrue(reachedHlt, "Program should reach hlt. Trace:\n"
                + String.join("\n", trace));

        // Verify stored value
        assertEquals(MOCK_VALUE, readDword(VALUE_ADDR),
                "value should contain the producer's output");

        // Verify done flag
        assertEquals(1, Memory.getValueAt(DONE_ADDR) & 0xFF,
                "done should be 1");

        // Verify the output device received the correct value
        assertEquals(MOCK_VALUE, mockOutput.capturedValue,
                "SyncOutput should have received the value from SyncInput");
    }
}
