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
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for device interrupt-driven I/O.
 * <p>
 * Assembles driver.asm and runs it with a mock device that:
 * <ul>
 *   <li>On STATUS write: immediately produces a known value and raises an interrupt</li>
 *   <li>On DATA_OUT read: returns the produced value (32-bit)</li>
 *   <li>On INT_REQ write: clears the interrupt (handled by Device base class)</li>
 * </ul>
 */
@DisplayName("Device driver integration test — driver.asm")
public class DeviceDriverTest {

    /** Known value the mock device will produce. */
    private static final long MOCK_VALUE = 0xDEADBEEFL;

    private Program program;

    /**
     * A minimal mock device for testing. It has three I/O elements:
     * INT_REQ (from .interrupts()), STATUS (from .busyWaiting()),
     * and DATA_OUT (custom register, readable, 4 bytes).
     * <p>
     * When the CPU writes to STATUS, the device immediately sets
     * its output register and raises an interrupt.
     */
    static class MockDevice extends Device {
        private long dataOut = 0;

        MockDevice() {
            // When CPU writes to STATUS port --> produce result and fire IRQ
            onWrite("STATUS", data -> {
                dataOut = MOCK_VALUE;
                raiseInterrupt();
            });

            // When CPU reads DATA_OUT port --> return the result
            onRead("DATA_OUT", () -> dataOut);
        }

        @Override
        public DeviceDescriptor getDescriptor() {
            return new DeviceDescriptor.Builder("MockDevice")
                    .busyWaiting()    // adds STATUS flip-flop
                    .interrupts()     // adds INT_REQ flip-flop
                    .ioPort("DATA_OUT",
                            IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 4)
                    .build();
        }
    }

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        // Assemble driver.asm
        InputStream is = getClass().getResourceAsStream("/driver.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed without errors: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        // Register mock device:
        //   IVN = 1 (matches .driver 1 in driver.asm)
        //   Port mapping: INT_REQ --> 0x0, STATUS --> 0x1, DATA_OUT --> 0x2
        //   (matches .equ DEV_IRQ=0x0, DEV_STATUS=0x1, DEV_REG=0x2)
        MockDevice mockDevice = new MockDevice();
        DeviceMapping mapping = new DeviceMapping(mockDevice, 1);
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        DeviceDescriptor desc = mockDevice.getDescriptor();
        // Map INT_REQ to port 0x0
        mapping.assignPort(0x0, desc.getIoPort("INT_REQ"));
        devices.registerPort(0x0, mapping);
        // Map STATUS to port 0x1
        mapping.assignPort(0x1, desc.getIoPort("STATUS"));
        devices.registerPort(0x1, mapping);
        // Map DATA_OUT to port 0x2
        mapping.assignPort(0x2, desc.getIoPort("DATA_OUT"));
        devices.registerPort(0x2, mapping);

        devices.registerDevice(1, mapping);
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

        // After each instruction, check for interrupts (mimics stepInstruction)
        if (SimulatorController.getCpuState().getIF()
                && Devices.getInstance().isIRQPending()) {
            // Trigger interrupt entry manually: save RFLAGS, RIP; clear IF; jump to handler
            long rsp = SimulatorController.getCpuState().getRSP();
            // Push RFLAGS
            rsp -= 8;
            writeQword(rsp, SimulatorController.getCpuState().getFlags());
            // Push RIP (return address)
            rsp -= 8;
            writeQword(rsp, SimulatorController.getCpuState().getRIP());
            SimulatorController.getCpuState().setRegisterValue(Register.RSP, rsp);
            SimulatorController.getCpuState().setIF(false);

            // Poll for the winning device
            DeviceMapping winner = Devices.getInstance().pollInterrupt();
            if (winner != null) {
                int ivn = winner.getIvn();
                long handlerAddress = readQword(ivn * 8L);
                SimulatorController.getCpuState().setRIP(handlerAddress);
            }
        }

        return instruction.getMnemonic();
    }

    private void writeQword(long address, long value) {
        for (int i = 0; i < 8; i++) {
            Memory.setValueAt(address + i, (byte) (value >> (i * 8)));
        }
    }

    private long readQword(long address) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (Memory.getValueAt(address + i) & 0xFF)) << (i * 8);
        }
        return value;
    }

    @Test
    @DisplayName("Driver reads MOCK_VALUE from device and stores it in data")
    public void testDriverStoresDeviceValue() throws SimulatorException {
        // Memory layout per .org 0x800:
        //   flag: .byte  at 0x800 (1 byte)
        //   data: .long  at 0x801 (4 bytes)
        //   sum:  .quad  at 0x805 (8 bytes)
        final long FLAG_ADDR = 0x800;
        final long DATA_ADDR = 0x801;
        final long SUM_ADDR  = 0x805;

        int maxSteps = 50;
        boolean reachedHlt = false;
        java.util.List<String> trace = new java.util.ArrayList<>();
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

        // Verify the 'data' variable contains the mock device's output value
        long storedValue = readQword(DATA_ADDR) & 0xFFFFFFFFL;  // .long = 32 bits
        assertEquals(MOCK_VALUE, storedValue,
                "data variable should contain the device's output value 0x"
                + Long.toHexString(MOCK_VALUE));

        // Verify 'sum' also received the value
        long sumValue = readQword(SUM_ADDR);
        assertEquals(MOCK_VALUE, sumValue,
                "sum should equal MOCK_VALUE after addq");

        // Verify 'flag' was set to 1
        assertEquals(1, Memory.getValueAt(FLAG_ADDR) & 0xFF,
                "flag should be 1 after driver sets it");
    }
}
