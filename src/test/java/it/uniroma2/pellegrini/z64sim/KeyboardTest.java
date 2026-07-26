/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.devices.Keyboard;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
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
 * Integration tests for the Keyboard device.
 * <p>
 * The keyboard is interrupt-driven: a simulated keypress stores the ASCII
 * code in DATA and raises INT_REQ. The ISR reads DATA and clears INT_REQ.
 */
@DisplayName("Keyboard device integration tests")
public class KeyboardTest {

    private static final long PORT_IRQ  = 0x20;
    private static final long PORT_DATA = 0x21;

    private Program program;
    private Keyboard keyboard;
    private DeviceMapping mapping;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/keyboard.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed without errors: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        keyboard = new Keyboard();
        mapping = new DeviceMapping(keyboard, 1);
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        DeviceDescriptor desc = keyboard.getDescriptor();
        mapping.assignPort(PORT_IRQ, desc.getIoPort("INT_REQ"));
        devices.registerPort(PORT_IRQ, mapping);
        mapping.assignPort(PORT_DATA, desc.getIoPort("DATA"));
        devices.registerPort(PORT_DATA, mapping);

        devices.registerDevice(1, mapping);
    }

    private String stepAndGetMnemonic() throws SimulatorException {
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertNotNull(element, "Expected element at RIP=0x" + Long.toHexString(rip));
        assertInstanceOf(Instruction.class, element,
                "Expected Instruction at RIP=0x" + Long.toHexString(rip));
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();

        // After each instruction, check for pending interrupts (mimics stepInstruction)
        if (SimulatorController.getCpuState().getIF()
                && Devices.getInstance().isIRQPending()) {
            long rsp = SimulatorController.getCpuState().getRSP();
            long savedFlags = SimulatorController.getCpuState().getFlags();
            SimulatorController.getCpuState().setIF(false);
            // Push RIP first (deeper on stack)
            rsp -= 8;
            writeQword(rsp, SimulatorController.getCpuState().getRIP());
            // Push FLAGS second (top of stack)
            rsp -= 8;
            writeQword(rsp, savedFlags);
            SimulatorController.getCpuState().setRegisterValue(
                    it.uniroma2.pellegrini.z64sim.isa.registers.Register.RSP, rsp);

            // Poll for the winning device and jump to its ISR
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
    @DisplayName("Keypress raises interrupt and ISR reads the ASCII character")
    public void testKeypressInterrupt() throws SimulatorException {
        // Step through: sti, outb (IVN), then enter the cmpb/jz wait loop.
        // After a few steps of the wait loop, simulate a keypress.
        int maxSteps = 200;
        List<String> trace = new ArrayList<>();
        boolean reachedHlt = false;
        boolean keypressSent = false;
        int waitLoopCount = 0;

        for (int i = 0; i < maxSteps; i++) {
            long rip = SimulatorController.getCpuState().getRIP();
            String mnemonic = stepAndGetMnemonic();
            trace.add(String.format("0x%x: %s", rip, mnemonic));

            if (mnemonic.equals("hlt")) {
                reachedHlt = true;
                break;
            }

            // After seeing the jz (wait loop), inject a keypress
            if (mnemonic.equals("jz") && !keypressSent) {
                waitLoopCount++;
                if (waitLoopCount >= 2) {
                    // Simulate pressing 'A' (ASCII 65)
                    keyboard.keyPressed('A');
                    keypressSent = true;
                }
            }
        }

        assertTrue(keypressSent, "Keypress should have been sent");
        assertTrue(reachedHlt, "Program should reach hlt after ISR processes key. Trace:\n"
                + String.join("\n", trace));

        // Verify that key_char in memory contains 'A'
        // key_char is at 0x800 (first byte in .data after .org 0x800)
        long keyCharAddr = 0x800;
        byte storedChar = Memory.getValueAt(keyCharAddr);
        assertEquals('A', storedChar & 0xFF,
                "key_char should contain 'A' (65)");

        // DATA register should also hold 'A'
        assertEquals('A', keyboard.getData(),
                "DATA register should hold 'A'");
    }

    @Test
    @DisplayName("Keyboard descriptor has correct structure")
    public void testDescriptor() {
        DeviceDescriptor desc = keyboard.getDescriptor();

        assertTrue(desc.isInterruptsEnabled(),
                "Keyboard should support interrupts");
        assertFalse(desc.isBusyWaitingEnabled(),
                "Keyboard should not use busy-waiting");

        assertNotNull(desc.getIoPort("INT_REQ"), "INT_REQ port should exist");
        assertNull(desc.getIoPort("IVN"),
                "IVN should not exist as a port (hardwired at registration)");

        assertNotNull(desc.getIoPort("DATA"), "DATA port should exist");
        assertTrue(desc.getIoPort("DATA").isRegister());
        assertTrue(desc.getIoPort("DATA").isReadable());
        assertFalse(desc.getIoPort("DATA").isWritable(),
                "DATA should be read-only (device→CPU)");
        assertEquals(1, desc.getIoPort("DATA").getWidthBytes());
    }

    @Test
    @DisplayName("Multiple keypresses update DATA register")
    public void testMultipleKeypresses() {
        keyboard.keyPressed('x');
        assertEquals('x', keyboard.getData());

        keyboard.keyPressed('Z');
        assertEquals('Z', keyboard.getData());

        keyboard.keyPressed(' ');
        assertEquals(' ', keyboard.getData());

        keyboard.keyPressed('\n');
        assertEquals('\n', keyboard.getData());
    }
}
