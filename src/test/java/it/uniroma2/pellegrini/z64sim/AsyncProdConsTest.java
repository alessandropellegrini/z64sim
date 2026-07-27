/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.devices.AsyncInput;
import it.uniroma2.pellegrini.z64sim.devices.AsyncOutput;
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
 * Integration test for asynchronous producer/consumer I/O using the
 * <strong>actual</strong> {@link AsyncInput} and {@link AsyncOutput}
 * device implementations. Only port/IVN mappings are set up manually.
 * <p>
 * The program spins in a bottom-half guard until both interrupt handlers
 * have run. Since real devices fire after a 200–1000 ms delay, the test
 * thread steps through the spin loop until each timer fires on the EDT.
 */
@DisplayName("Async producer/consumer — async_prodcons.asm (real devices)")
public class AsyncProdConsTest {

    private Program program;
    private DeviceMapping inputMapping;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/async_prodcons.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        // Real AsyncInput (IVN=1): STATUS@0x10, INT_REQ@0x11, DATA_IN@0x12
        AsyncInput asyncInput = new AsyncInput();
        inputMapping = new DeviceMapping(asyncInput, 1);
        DeviceDescriptor inDesc = asyncInput.getDescriptor();
        inputMapping.assignPort(0x10, inDesc.getIoPort("STATUS"));
        devices.registerPort(0x10, inputMapping);
        inputMapping.assignPort(0x11, inDesc.getIoPort("INT_REQ"));
        devices.registerPort(0x11, inputMapping);
        inputMapping.assignPort(0x12, inDesc.getIoPort("DATA_IN"));
        devices.registerPort(0x12, inputMapping);
        devices.registerDevice(1, inputMapping);

        // Real AsyncOutput (IVN=2): STATUS@0x20, INT_REQ@0x21, DATA_OUT@0x22
        AsyncOutput asyncOutput = new AsyncOutput();
        DeviceMapping outMapping = new DeviceMapping(asyncOutput, 2);
        DeviceDescriptor outDesc = asyncOutput.getDescriptor();
        outMapping.assignPort(0x20, outDesc.getIoPort("STATUS"));
        devices.registerPort(0x20, outMapping);
        outMapping.assignPort(0x21, outDesc.getIoPort("INT_REQ"));
        devices.registerPort(0x21, outMapping);
        outMapping.assignPort(0x22, outDesc.getIoPort("DATA_OUT"));
        devices.registerPort(0x22, outMapping);
        devices.registerDevice(2, outMapping);
    }

    /**
     * Step one instruction with interrupt handling.
     * The spin loop in async_prodcons.asm (cmpb/jz) naturally keeps
     * stepping until an EDT timer fires and an IRQ becomes pending.
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
    @DisplayName("Value flows from AsyncInput to AsyncOutput via interrupt handlers")
    public void testAsyncProdCons() throws SimulatorException {
        // Memory layout: value(.quad)@0x800, done(.byte)@0x808
        final long VALUE_ADDR = 0x800;
        final long DONE_ADDR  = 0x808;

        // The spin loop runs until both device timers fire (up to ~2s total).
        // Allow enough iterations for the timers to complete.
        int maxSteps = 5_000_000;
        boolean reachedHlt = false;
        long lastTraceRip = -1;
        List<String> trace = new ArrayList<>();

        for (int i = 0; i < maxSteps; i++) {
            long rip = SimulatorController.getCpuState().getRIP();
            String mnemonic = stepAndGetMnemonic();

            // Only trace unique RIP values to avoid flooding with spin-loop entries
            if (rip != lastTraceRip) {
                trace.add(String.format("0x%x: %s", rip, mnemonic));
                lastTraceRip = rip;
            }

            if (mnemonic.equals("hlt")) {
                reachedHlt = true;
                break;
            }
        }

        assertTrue(reachedHlt, "Program should reach hlt. Trace:\n"
                + String.join("\n", trace));

        // Read the value that the real AsyncInput generated
        long inputValue = inputMapping.read(0x12);  // DATA_IN

        // Verify the value was stored correctly in memory
        assertEquals(inputValue, readQword(VALUE_ADDR),
                "value should contain the 64-bit input from AsyncInput");

        // Verify done flag
        assertEquals(1, Memory.getValueAt(DONE_ADDR) & 0xFF,
                "done should be 1 after output completion");
    }
}
