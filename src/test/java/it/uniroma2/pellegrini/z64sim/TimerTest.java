/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.devices.Timer;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
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
 * Integration test for timer.asm using the <strong>actual</strong>
 * {@link Timer} device implementation.
 * <p>
 * The program sets a 200 ms timer, spins in a bottom-half guard
 * until the interrupt fires and sets a flag, then halts.
 * The test verifies the flag is set and that the elapsed wall-clock
 * time is consistent with the requested delay.
 */
@DisplayName("Timer integration test — timer.asm (real device)")
public class TimerTest {

    private Program program;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/timer.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        // Real Timer (IVN=1): DELAY@0x10, STATUS@0x11, INT_REQ@0x12
        Timer timer = new Timer();
        DeviceMapping mapping = new DeviceMapping(timer, 1);
        DeviceDescriptor desc = timer.getDescriptor();
        mapping.assignPort(0x10, desc.getIoPort("DELAY"));
        devices.registerPort(0x10, mapping);
        mapping.assignPort(0x11, desc.getIoPort("STATUS"));
        devices.registerPort(0x11, mapping);
        mapping.assignPort(0x12, desc.getIoPort("INT_REQ"));
        devices.registerPort(0x12, mapping);
        devices.registerDevice(1, mapping);
    }

    /**
     * Step one instruction with interrupt handling.
     * The bottom-half spin loop keeps stepping until the timer fires.
     */
    private String stepAndGetMnemonic() throws SimulatorException {
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertNotNull(element, "Expected element at RIP=0x" + Long.toHexString(rip));
        assertInstanceOf(Instruction.class, element);
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();

        // Check for pending interrupts after each instruction
        if (SimulatorController.getCpuState().getIF()
                && Devices.getInstance().isIRQPending()) {
            dispatchInterrupt();
        }

        return instruction.getMnemonic();
    }

    private void dispatchInterrupt() {
        long rsp = SimulatorController.getCpuState().getRSP();
        long savedFlags = SimulatorController.getCpuState().getFlags();
        SimulatorController.getCpuState().setIF(false);
        // Push RIP first (deeper on stack)
        rsp -= 8;
        writeQword(rsp, SimulatorController.getCpuState().getRIP());
        // Push FLAGS second (top of stack)
        rsp -= 8;
        writeQword(rsp, savedFlags);
        SimulatorController.getCpuState().setRegisterValue(Register.RSP, rsp);

        DeviceMapping winner = Devices.getInstance().pollInterrupt();
        if (winner != null) {
            int ivn = winner.getIvn();
            long handlerAddress = readQword(ivn * 8L);
            SimulatorController.getCpuState().setRIP(handlerAddress);
        }
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
    @DisplayName("Timer fires interrupt after programmed delay")
    public void testTimerFiresAfterDelay() throws SimulatorException {
        // Memory layout: fired(.byte)@0x800
        final long FIRED_ADDR = 0x800;
        final long EXPECTED_DELAY_MS = 200;

        long startTime = System.currentTimeMillis();

        int maxSteps = 5_000_000;
        boolean reachedHlt = false;
        long lastTraceRip = -1;
        List<String> trace = new ArrayList<>();

        for (int i = 0; i < maxSteps; i++) {
            long rip = SimulatorController.getCpuState().getRIP();
            String mnemonic = stepAndGetMnemonic();

            if (rip != lastTraceRip) {
                trace.add(String.format("0x%x: %s", rip, mnemonic));
                lastTraceRip = rip;
            }

            if (mnemonic.equals("hlt")) {
                reachedHlt = true;
                break;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        assertTrue(reachedHlt, "Program should reach hlt. Trace:\n"
                + String.join("\n", trace));

        // Verify fired flag
        assertEquals(1, Memory.getValueAt(FIRED_ADDR) & 0xFF,
                "fired should be 1 after timer interrupt");

        // Verify timing: elapsed should be at least the programmed delay
        assertTrue(elapsed >= EXPECTED_DELAY_MS,
                "Elapsed time (" + elapsed + " ms) should be >= "
                + EXPECTED_DELAY_MS + " ms");
    }
}
