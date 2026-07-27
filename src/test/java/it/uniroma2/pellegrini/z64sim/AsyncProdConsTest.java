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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for asynchronous producer/consumer I/O using the
 * <strong>actual</strong> {@link AsyncInput} and {@link AsyncOutput}
 * device implementations. Only port/IVN mappings are set up manually.
 * <p>
 * The program spins in a bottom-half guard until both interrupt handlers
 * have run. The device timers fire on a daemon thread after a 200–1000 ms
 * delay; the test steps through the spin loop until the {@code done}
 * memory flag is set.
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
        rsp -= 8;
        writeQword(rsp, SimulatorController.getCpuState().getRIP());
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
        final long VALUE_ADDR = 0x800;
        final long DONE_ADDR  = 0x808;
        final long deadlineMs = System.currentTimeMillis() + 10_000;

        boolean done = false;
        Set<String> trace = new LinkedHashSet<>();

        while (System.currentTimeMillis() < deadlineMs) {
            long rip = SimulatorController.getCpuState().getRIP();
            String mnemonic = stepAndGetMnemonic();
            trace.add(String.format("0x%x: %s", rip, mnemonic));

            if ((Memory.getValueAt(DONE_ADDR) & 0xFF) == 1) {
                done = true;
                break;
            }
        }

        assertTrue(done, "Both interrupt handlers should complete. Trace:\n"
                + String.join("\n", trace));

        long inputValue = inputMapping.read(0x12);
        assertEquals(inputValue, readQword(VALUE_ADDR),
                "value should contain the 64-bit input from AsyncInput");
    }
}
