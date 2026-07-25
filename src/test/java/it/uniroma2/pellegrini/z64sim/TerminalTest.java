/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.devices.Terminal;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
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
 * Integration tests for the Terminal device.
 * <p>
 * Tests character output, line wrapping, and scroll-shift behaviour.
 * Uses DMA mode for synchronous callbacks so busy-wait loops terminate
 * immediately.
 */
@DisplayName("Terminal device integration tests")
public class TerminalTest {

    private static final long PORT_STATUS = 0x20;
    private static final long PORT_DATA   = 0x21;

    private Program program;
    private Terminal terminal;
    private DeviceMapping mapping;

    @BeforeEach
    public void setup() throws ParseException {
        SimulatorController.init();

        // Assemble terminal.asm
        InputStream is = getClass().getResourceAsStream("/terminal.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        assertTrue(a.getSyntaxErrors().isEmpty(),
                "Assembly should succeed without errors: " + a.getSyntaxErrors());

        program = a.getProgram();
        SimulatorController.loadProgram(program);

        // Register Terminal device:
        //   Port 0x20 --> STATUS, Port 0x21 --> DATA
        terminal = new Terminal();
        terminal.setDmaMode(true);  // synchronous for testing
        mapping = new DeviceMapping(terminal, -1);
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        DeviceDescriptor desc = terminal.getDescriptor();
        mapping.assignPort(PORT_STATUS, desc.getIoPort("STATUS"));
        devices.registerPort(PORT_STATUS, mapping);
        mapping.assignPort(PORT_DATA, desc.getIoPort("DATA"));
        devices.registerPort(PORT_DATA, mapping);

        devices.registerDevice(-1, mapping);
    }

    /**
     * Write a single character to the terminal and start the device.
     * Uses DeviceMapping.write() to go through the standard I/O path.
     */
    private void writeChar(char ch) {
        mapping.write(PORT_DATA, ch & 0xFF);
        mapping.write(PORT_STATUS, 0);
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

    private void runToHlt() throws SimulatorException {
        int maxSteps = 500;
        List<String> trace = new ArrayList<>();
        boolean reachedHlt = false;

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
    }

    @Test
    @DisplayName("Hello World: characters appear in buffer at correct position")
    public void testOutputCharacters() throws SimulatorException {
        runToHlt();

        char[][] buf = terminal.getBuffer();
        String row0 = new String(buf[0]).trim();
        assertEquals("Hello, World!", row0,
                "First row should contain 'Hello, World!'");

        // After 13 characters, cursor should be at row 0, col 13
        assertEquals(0, terminal.getCursorRow(),
                "Cursor row should be 0 after 13 chars");
        assertEquals(13, terminal.getCursorCol(),
                "Cursor col should be 13 after 13 chars");
    }

    @Test
    @DisplayName("Line wrap: 81st character wraps to second row")
    public void testLineWrap() {
        // Write 81 'A' characters via the I/O mapping
        for (int i = 0; i < 81; i++) {
            writeChar('A');
        }

        char[][] buf = terminal.getBuffer();
        // First row should be all 'A's
        for (int c = 0; c < Terminal.COLS; c++) {
            assertEquals('A', buf[0][c],
                    "Row 0, col " + c + " should be 'A'");
        }
        // 81st character wraps to row 1, col 0
        assertEquals('A', buf[1][0],
                "Row 1, col 0 should be 'A' (wrapped character)");
        assertEquals(1, terminal.getCursorRow(),
                "Cursor should be on row 1 after wrap");
        assertEquals(1, terminal.getCursorCol(),
                "Cursor should be at col 1 after wrap");
    }

    @Test
    @DisplayName("Scroll shift: filling all rows then adding a line shifts top row out")
    public void testScrollShift() {
        // Fill all 24 rows with distinct characters.
        // Each row gets a single character: row 0 = 'A', row 1 = 'B', etc.
        for (int r = 0; r < Terminal.ROWS; r++) {
            char ch = (char) ('A' + r);
            writeChar(ch);
            writeChar('\n');
        }

        // After 24 newlines, the cursor has scrolled:
        // Original row 0 ('A') should have been shifted out.

        // Now write 'Z' on the current (last) row
        writeChar('Z');

        char[][] buf = terminal.getBuffer();

        // Row 0 should now be what was originally row 1 ('B')
        assertEquals('B', buf[0][0],
                "After scroll, row 0 should contain 'B' (originally row 1)");

        // The last row should have 'Z'
        assertEquals('Z', buf[Terminal.ROWS - 1][0],
                "Last row should contain 'Z'");

        // 'A' should be gone — row 0 col 0 is 'B', not 'A'
        assertNotEquals('A', buf[0][0],
                "'A' should have been shifted out");
    }

    @Test
    @DisplayName("Backspace clears previous character and moves cursor left")
    public void testBackspace() {
        // Write "AB"
        writeChar('A');
        writeChar('B');

        assertEquals(0, terminal.getCursorRow());
        assertEquals(2, terminal.getCursorCol());

        // Backspace
        writeChar('\b');

        assertEquals(0, terminal.getCursorRow());
        assertEquals(1, terminal.getCursorCol());
        assertEquals(' ', terminal.getBuffer()[0][1],
                "Backspace should clear the cell");
        assertEquals('A', terminal.getBuffer()[0][0],
                "'A' should remain");
    }

    @Test
    @DisplayName("Carriage return moves cursor to column 0 without advancing row")
    public void testCarriageReturn() {
        // Write "Hello"
        for (char ch : "Hello".toCharArray()) {
            writeChar(ch);
        }

        assertEquals(0, terminal.getCursorRow());
        assertEquals(5, terminal.getCursorCol());

        // Carriage return
        writeChar('\r');

        assertEquals(0, terminal.getCursorRow(),
                "\\r should not change row");
        assertEquals(0, terminal.getCursorCol(),
                "\\r should reset column to 0");

        // "Hello" should still be in the buffer
        assertEquals('H', terminal.getBuffer()[0][0]);
    }
}
