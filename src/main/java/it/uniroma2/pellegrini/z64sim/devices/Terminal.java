/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

import javax.swing.*;
import java.awt.*;

/**
 * Terminal Device — character-at-a-time output with internal shift memory.
 * <p>
 * The CPU writes one ASCII byte to DATA, then writes STATUS to start the
 * device. The device processes the character (inserting it into the internal
 * buffer at the cursor position) and redraws the terminal GUI.
 * <p>
 * The internal buffer is a 24×80 grid of characters (classic VT100 size).
 * When the cursor moves past the last row, all rows shift up by one
 * (the top row is lost, the bottom row is cleared) — this is the
 * "shift memory" behaviour.
 * <p>
 * Supported control characters:
 * <ul>
 *   <li>{@code \n} (0x0A) — move to column 0 of the next row</li>
 *   <li>{@code \r} (0x0D) — move to column 0 of the current row</li>
 *   <li>{@code \b} (0x08) — move one position left and clear the cell</li>
 * </ul>
 * <p>
 * I/O elements:
 * <ul>
 *   <li>STATUS (flip-flop, readable + writable) — busy-waiting protocol</li>
 *   <li>DATA (register, writable, 1 byte) — the ASCII byte to display</li>
 * </ul>
 */
public class Terminal extends Device {

    /** Number of rows in the terminal buffer. */
    public static final int ROWS = 24;
    /** Number of columns in the terminal buffer. */
    public static final int COLS = 80;

    private static final int DELAY_MS = 10;  // very short — character output is fast

    // ---- Internal state ----

    private final char[][] buffer = new char[ROWS][COLS];
    private int cursorRow = 0;
    private int cursorCol = 0;
    private volatile long status = 1;  // 1 = ready, 0 = busy
    private long data = 0;

    // ---- GUI ----

    private JFrame frame;
    private TerminalPanel panel;

    public Terminal() {
        clearBuffer();

        onRead("STATUS", () -> status);

        onWrite("STATUS", val -> {
            status = 0;  // busy
            scheduleAfterDelay(() -> {
                processChar((char) (data & 0x7F));
                status = 1;  // ready
                if (panel != null) {
                    SwingUtilities.invokeLater(panel::repaint);
                }
            }, DELAY_MS);
        });

        onWrite("DATA", val -> data = val & 0xFFL);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Terminal")
                .busyWaiting()
                .ioPort("DATA",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 1)
                .build();
    }

    // ---- Character processing ----

    private void processChar(char ch) {
        switch (ch) {
            case '\n':  // newline: advance to next row, column 0
                cursorCol = 0;
                cursorRow++;
                if (cursorRow >= ROWS) {
                    shiftUp();
                    cursorRow = ROWS - 1;
                }
                break;

            case '\r':  // carriage return: column 0, same row
                cursorCol = 0;
                break;

            case '\b':  // backspace: move left and clear
                if (cursorCol > 0) {
                    cursorCol--;
                    buffer[cursorRow][cursorCol] = ' ';
                }
                break;

            default:
                if (ch >= 0x20 && ch <= 0x7E) {  // printable ASCII
                    buffer[cursorRow][cursorCol] = ch;
                    cursorCol++;
                    if (cursorCol >= COLS) {
                        cursorCol = 0;
                        cursorRow++;
                        if (cursorRow >= ROWS) {
                            shiftUp();
                            cursorRow = ROWS - 1;
                        }
                    }
                }
                // non-printable characters are silently ignored
                break;
        }
    }

    /**
     * Shift all rows up by one. The top row is discarded and the
     * bottom row is cleared to spaces.
     */
    private void shiftUp() {
        for (int r = 1; r < ROWS; r++) {
            System.arraycopy(buffer[r], 0, buffer[r - 1], 0, COLS);
        }
        java.util.Arrays.fill(buffer[ROWS - 1], ' ');
    }

    private void clearBuffer() {
        for (int r = 0; r < ROWS; r++) {
            java.util.Arrays.fill(buffer[r], ' ');
        }
    }

    // ---- Public accessors for testing ----

    /** Return the internal character buffer (24×80 grid). */
    public char[][] getBuffer() {
        return buffer;
    }

    /** Return the current cursor row. */
    public int getCursorRow() {
        return cursorRow;
    }

    /** Return the current cursor column. */
    public int getCursorCol() {
        return cursorCol;
    }

    // ---- Simulation lifecycle ----

    @Override
    protected void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Terminal");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setResizable(false);

            panel = new TerminalPanel();
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    @Override
    protected void onSimulationStop() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
                panel = null;
            }
        });
    }

    // ---- Terminal rendering panel ----

    private static final Color BG_COLOR   = new Color(0x1A, 0x1A, 0x2E);
    private static final Color TEXT_COLOR  = new Color(0x00, 0xFF, 0x41);
    private static final Color CURSOR_COLOR = new Color(0x00, 0xFF, 0x41, 0xA0);
    private static final Font TERM_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private static final int PADDING = 8;

    /**
     * Panel that renders the terminal buffer as a monospace character grid
     * with a block cursor.
     */
    private class TerminalPanel extends JPanel {

        private final int cellWidth;
        private final int cellHeight;
        private boolean cursorVisible = true;
        private final javax.swing.Timer blinkTimer;

        TerminalPanel() {
            setBackground(BG_COLOR);

            // Compute cell dimensions from font metrics
            FontMetrics fm = getFontMetrics(TERM_FONT);
            cellWidth = fm.charWidth('M');
            cellHeight = fm.getHeight();

            int panelWidth  = COLS * cellWidth  + 2 * PADDING;
            int panelHeight = ROWS * cellHeight + 2 * PADDING;
            setPreferredSize(new Dimension(panelWidth, panelHeight));

            // Cursor blink timer
            blinkTimer = new javax.swing.Timer(500, e -> {
                cursorVisible = !cursorVisible;
                repaint();
            });
            blinkTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(TERM_FONT);

            FontMetrics fm = g2.getFontMetrics();
            int ascent = fm.getAscent();

            // Draw characters
            g2.setColor(TEXT_COLOR);
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    char ch = buffer[r][c];
                    if (ch != ' ') {
                        int x = PADDING + c * cellWidth;
                        int y = PADDING + r * cellHeight + ascent;
                        g2.drawString(String.valueOf(ch), x, y);
                    }
                }
            }

            // Draw cursor
            if (cursorVisible && cursorRow < ROWS && cursorCol < COLS) {
                g2.setColor(CURSOR_COLOR);
                int cx = PADDING + cursorCol * cellWidth;
                int cy = PADDING + cursorRow * cellHeight;
                g2.fillRect(cx, cy, cellWidth, cellHeight);

                // Redraw the character under the cursor in background color
                char under = buffer[cursorRow][cursorCol];
                if (under != ' ') {
                    g2.setColor(BG_COLOR);
                    g2.drawString(String.valueOf(under),
                            cx, cy + ascent);
                }
            }

            g2.dispose();
        }

        /** Stop the blink timer when the panel is removed. */
        @Override
        public void removeNotify() {
            blinkTimer.stop();
            super.removeNotify();
        }
    }
}
