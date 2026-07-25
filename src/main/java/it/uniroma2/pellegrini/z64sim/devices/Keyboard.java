/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Keyboard Device — interrupt-driven input via a clickable on-screen keyboard.
 * <p>
 * Displays a US ANSI keyboard GUI. When the user clicks a key with the mouse,
 * the corresponding ASCII code is stored in DATA and an interrupt is raised
 * so that the driver can acquire the character.
 * <p>
 * I/O elements:
 * <ul>
 *   <li>INT_REQ (flip-flop, writable) — interrupt request, managed by base class</li>
 *   <li>DATA (register, readable, 1 byte) — the ASCII code of the last pressed key</li>
 * </ul>
 * <p>
 * Protocol:
 * <ol>
 *   <li>User clicks a key on the GUI keyboard</li>
 *   <li>Device stores the ASCII code in DATA and raises INT_REQ</li>
 *   <li>ISR reads DATA, then clears INT_REQ via {@code outb}</li>
 * </ol>
 */
public class Keyboard extends Device {

    private volatile long data = 0;

    // ---- GUI ----

    private JFrame frame;
    private KeyboardPanel panel;

    public Keyboard() {
        onRead("DATA", () -> data);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Keyboard")
                .interrupts()
                .ioPort("DATA",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.READABLE, 1)
                .build();
    }

    /**
     * Called from the GUI when a key is clicked.
     * Stores the ASCII value and raises an interrupt.
     */
    public void keyPressed(int ascii) {
        data = ascii & 0x7F;
        raiseInterrupt();
    }

    /** Return the last key value — public for testing. */
    public long getData() {
        return data;
    }

    // ---- Simulation lifecycle ----

    @Override
    protected void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Keyboard");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setResizable(false);

            panel = new KeyboardPanel();
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

    // ====================================================================
    // Keyboard Layout and Rendering
    // ====================================================================

    /** A single key on the keyboard. */
    private static class Key {
        final String label;
        final int ascii;
        final double widthUnits;

        Key(String label, int ascii, double widthUnits) {
            this.label = label;
            this.ascii = ascii;
            this.widthUnits = widthUnits;
        }

        Key(String label, int ascii) {
            this(label, ascii, 1.0);
        }
    }

    // US ANSI layout — five rows
    private static final Key[][] LAYOUT = {
        // Row 0: number row
        {
            new Key("`", '`'), new Key("1", '1'), new Key("2", '2'), new Key("3", '3'),
            new Key("4", '4'), new Key("5", '5'), new Key("6", '6'), new Key("7", '7'),
            new Key("8", '8'), new Key("9", '9'), new Key("0", '0'), new Key("-", '-'),
            new Key("=", '='), new Key("Bksp", '\b', 2.0)
        },
        // Row 1: QWERTY
        {
            new Key("Tab", '\t', 1.5),
            new Key("q", 'q'), new Key("w", 'w'), new Key("e", 'e'), new Key("r", 'r'), new Key("t", 't'),
            new Key("y", 'y'), new Key("u", 'u'), new Key("i", 'i'), new Key("o", 'o'), new Key("p", 'p'),
            new Key("[", '['), new Key("]", ']'), new Key("\\", '\\')
        },
        // Row 2: home row
        {
            new Key("a", 'a'), new Key("s", 's'), new Key("d", 'd'), new Key("f", 'f'), new Key("g", 'g'),
            new Key("h", 'h'), new Key("j", 'j'), new Key("k", 'k'), new Key("l", 'l'),
            new Key(";", ';'), new Key("'", '\''), new Key("Enter", '\n', 2.75)
        },
        // Row 3: bottom letter row
        {
            new Key("z", 'z'), new Key("x", 'x'), new Key("c", 'c'), new Key("v", 'v'), new Key("b", 'b'),
            new Key("n", 'n'), new Key("m", 'm'), new Key(",", ','), new Key(".", '.'), new Key("/", '/')
        },
        // Row 4: space bar
        {
            new Key("Space", ' ', 8.0)
        }
    };

    // ---- Rendering constants ----

    private static final int KEY_UNIT = 44;       // pixels per key unit
    private static final int KEY_GAP  = 3;        // gap between keys
    private static final int KEY_H    = 40;       // key height
    private static final int PADDING  = 10;       // panel padding
    private static final int ARC      = 8;        // rounded corner radius

    private static final Color BG_COLOR      = new Color(0x2A, 0x2A, 0x2A);
    private static final Color KEY_COLOR      = new Color(0x3C, 0x3C, 0x3C);
    private static final Color KEY_HOVER      = new Color(0x50, 0x50, 0x50);
    private static final Color KEY_PRESSED    = new Color(0x00, 0xAA, 0xFF);
    private static final Color KEY_BORDER     = new Color(0x55, 0x55, 0x55);
    private static final Color TEXT_COLOR     = new Color(0xE0, 0xE0, 0xE0);
    private static final Font  KEY_FONT       = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    private static final Font  KEY_FONT_SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

    /**
     * Panel that renders and handles clicks on the keyboard layout.
     */
    private class KeyboardPanel extends JPanel {

        /** Per-key bounding rectangles, parallel to LAYOUT. */
        private final Rectangle[][] keyRects;
        private int hoverRow = -1, hoverCol = -1;
        private int pressRow = -1, pressCol = -1;

        KeyboardPanel() {
            setBackground(BG_COLOR);

            // Compute layout dimensions
            double maxRowUnits = 0;
            for (Key[] row : LAYOUT) {
                double units = 0;
                for (Key k : row) units += k.widthUnits;
                units += (row.length - 1) * ((double) KEY_GAP / KEY_UNIT);
                if (units > maxRowUnits) maxRowUnits = units;
            }

            int panelW = (int) (maxRowUnits * KEY_UNIT) + 2 * PADDING;
            int panelH = LAYOUT.length * (KEY_H + KEY_GAP) - KEY_GAP + 2 * PADDING;
            setPreferredSize(new Dimension(panelW, panelH));

            // Pre-compute key rectangles
            keyRects = new Rectangle[LAYOUT.length][];
            for (int r = 0; r < LAYOUT.length; r++) {
                keyRects[r] = new Rectangle[LAYOUT[r].length];
                // Centre each row
                double rowUnits = 0;
                for (Key k : LAYOUT[r]) rowUnits += k.widthUnits;
                rowUnits += (LAYOUT[r].length - 1) * ((double) KEY_GAP / KEY_UNIT);
                int rowPixelWidth = (int) (rowUnits * KEY_UNIT);
                int xStart = PADDING + (panelW - 2 * PADDING - rowPixelWidth) / 2;
                int x = xStart;
                int y = PADDING + r * (KEY_H + KEY_GAP);
                for (int c = 0; c < LAYOUT[r].length; c++) {
                    int kw = (int) (LAYOUT[r][c].widthUnits * KEY_UNIT) - KEY_GAP;
                    keyRects[r][c] = new Rectangle(x, y, kw, KEY_H);
                    x += kw + KEY_GAP;
                }
            }

            // Mouse interaction
            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int[] hit = hitTest(e.getX(), e.getY());
                    if (hit != null) {
                        Key key = LAYOUT[hit[0]][hit[1]];
                        pressRow = hit[0];
                        pressCol = hit[1];
                        repaint();

                        if (key.ascii != 0) {
                            keyPressed(key.ascii);
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressRow = -1;
                    pressCol = -1;
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    int[] hit = hitTest(e.getX(), e.getY());
                    int newR = hit != null ? hit[0] : -1;
                    int newC = hit != null ? hit[1] : -1;
                    if (newR != hoverRow || newC != hoverCol) {
                        hoverRow = newR;
                        hoverCol = newC;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverRow = -1;
                    hoverCol = -1;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private int[] hitTest(int mx, int my) {
            for (int r = 0; r < keyRects.length; r++) {
                for (int c = 0; c < keyRects[r].length; c++) {
                    if (keyRects[r][c].contains(mx, my)) {
                        return new int[]{r, c};
                    }
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for (int r = 0; r < LAYOUT.length; r++) {
                for (int c = 0; c < LAYOUT[r].length; c++) {
                    drawKey(g2, r, c);
                }
            }

            g2.dispose();
        }

        private void drawKey(Graphics2D g2, int r, int c) {
            Key key = LAYOUT[r][c];
            Rectangle rect = keyRects[r][c];

            // Determine fill colour
            Color fill;
            if (r == pressRow && c == pressCol) {
                fill = KEY_PRESSED;
            } else if (r == hoverRow && c == hoverCol) {
                fill = KEY_HOVER;
            } else {
                fill = KEY_COLOR;
            }

            // Key body
            RoundRectangle2D rr = new RoundRectangle2D.Double(
                    rect.x, rect.y, rect.width, rect.height, ARC, ARC);
            g2.setColor(fill);
            g2.fill(rr);
            g2.setColor(KEY_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(rr);

            // Label
            String label = key.label;
            if (key.widthUnits > 1.2) {
                g2.setFont(KEY_FONT_SMALL);
            } else {
                g2.setFont(KEY_FONT);
            }

            g2.setColor(TEXT_COLOR);
            FontMetrics fm = g2.getFontMetrics();
            int tx = rect.x + (rect.width - fm.stringWidth(label)) / 2;
            int ty = rect.y + (rect.height + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(label, tx, ty);
        }
    }
}
