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
import it.uniroma2.pellegrini.z64sim.devices.Alarm;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for alarm.asm using the <strong>actual</strong>
 * {@link AsyncInput} and {@link Alarm} device implementations.
 * <p>
 * Only the port/IVN mapping is set up manually — the devices themselves
 * are not mocked.
 */
@DisplayName("Alarm integration test — alarm.asm (real devices)")
public class AlarmTest {

    private Program program;
    private DeviceMapping inputMapping;
    private DeviceMapping alarmMapping;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        InputStream is = getClass().getResourceAsStream("/alarm.asm");
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

        // Real Alarm: ALARM@0x20 (no IVN needed)
        Alarm alarm = new Alarm();
        alarmMapping = new DeviceMapping(alarm, 0);
        DeviceDescriptor alarmDesc = alarm.getDescriptor();
        alarmMapping.assignPort(0x20, alarmDesc.getIoPort("ALARM"));
        devices.registerPort(0x20, alarmMapping);
        devices.registerDevice(-1, alarmMapping);

        // Show the Alarm GUI when a display is available
        if (!GraphicsEnvironment.isHeadless()) {
            devices.notifySimulationStart();
        }
    }

    @AfterEach
    public void teardown() {
        if (!GraphicsEnvironment.isHeadless()) {
            Devices.getInstance().notifySimulationStop();
        }
    }

    private String stepAndGetMnemonic() throws SimulatorException {
        long rip = SimulatorController.getCpuState().getRIP();
        MemoryElement element = program.getMemoryElementAt(rip);
        assertNotNull(element, "Expected element at RIP=0x" + Long.toHexString(rip));
        assertInstanceOf(Instruction.class, element);
        Instruction instruction = (Instruction) element;
        SimulatorController.getCpuState().setRIP(rip + instruction.getSize());
        instruction.run();

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
    @DisplayName("Driver sets alarm on/off based on parity of random input value")
    public void testAlarmFollowsInputParity() throws SimulatorException {
        final long deadlineMs = System.currentTimeMillis() + 5_000;

        boolean handlerRan = false;
        long capturedInput = 0;
        long capturedAlarm = 0;
        Set<String> trace = new LinkedHashSet<>();

        while (System.currentTimeMillis() < deadlineMs) {
            long rip = SimulatorController.getCpuState().getRIP();
            String mnemonic = stepAndGetMnemonic();
            trace.add(String.format("0x%x: %s", rip, mnemonic));

            if (mnemonic.equals("iret") && !handlerRan) {
                handlerRan = true;
                capturedInput = inputMapping.read(0x12);   // DATA_IN
                capturedAlarm = alarmMapping.read(0x20);    // ALARM
                break;
            }
        }

        assertTrue(handlerRan, "Interrupt handler should run. Trace:\n"
                + String.join("\n", trace));

        boolean isOdd = (capturedInput & 1) != 0;
        assertEquals(isOdd ? 1L : 0L, capturedAlarm,
                "Alarm should be " + (isOdd ? "ON" : "OFF")
                + " for input value 0x" + Long.toHexString(capturedInput)
                + " (parity: " + (isOdd ? "odd" : "even") + ")");
    }
}
