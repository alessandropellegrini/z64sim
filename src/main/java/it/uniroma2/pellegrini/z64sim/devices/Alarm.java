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
 * Alarm Device — a simple visual indicator with a single flip-flop.
 * <p>
 * The CPU writes 1 to turn the alarm on, 0 to turn it off.
 * A small window displays a red light that reflects the current state.
 * <p>
 * I/O elements:
 * <ul>
 *   <li>ALARM (flip-flop, writable) — 0 = off, 1 = on</li>
 * </ul>
 */
public class Alarm extends Device {

    private volatile boolean alarmOn = false;
    private JFrame frame;
    private AlarmPanel panel;

    public Alarm() {
        onWrite("ALARM", data -> {
            alarmOn = (data != 0);
            if (panel != null) {
                SwingUtilities.invokeLater(panel::repaint);
            }
        });

        onRead("ALARM", () -> alarmOn ? 1L : 0L);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Alarm")
                .ioPort("ALARM",
                        IoPortDescriptor.FLIP_FLOP
                      | IoPortDescriptor.WRITABLE
                      | IoPortDescriptor.READABLE)
                .build();
    }

    @Override
    protected void onSimulationStart() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Alarm");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.setResizable(false);

            panel = new AlarmPanel();
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

    private static final int PANEL_SIZE = 120;
    private static final int LIGHT_DIAMETER = 80;
    private static final int LIGHT_BORDER = (PANEL_SIZE - LIGHT_DIAMETER) / 2;

    private static final Color COLOR_ON  = new Color(0xFF, 0x20, 0x20);
    private static final Color COLOR_OFF = new Color(0x60, 0x60, 0x60);
    private static final Color COLOR_GLOW = new Color(0xFF, 0x60, 0x60, 0x80);
    private static final Color BG = new Color(0x2A, 0x2A, 0x2A);

    /**
     * A small panel that draws a circular light: bright red when on,
     * dark grey when off.
     */
    private class AlarmPanel extends JPanel {

        AlarmPanel() {
            setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
            setBackground(BG);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Glow effect when on
            if (alarmOn) {
                g2.setColor(COLOR_GLOW);
                int glowSize = LIGHT_DIAMETER + 16;
                int glowOffset = LIGHT_BORDER - 8;
                g2.fillOval(glowOffset, glowOffset, glowSize, glowSize);
            }

            // Main light
            g2.setColor(alarmOn ? COLOR_ON : COLOR_OFF);
            g2.fillOval(LIGHT_BORDER, LIGHT_BORDER, LIGHT_DIAMETER, LIGHT_DIAMETER);

            // Highlight for 3D effect
            g2.setColor(new Color(255, 255, 255, alarmOn ? 80 : 30));
            g2.fillOval(LIGHT_BORDER + 12, LIGHT_BORDER + 8, 28, 20);

            // Outline
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(LIGHT_BORDER, LIGHT_BORDER, LIGHT_DIAMETER, LIGHT_DIAMETER);

            // Label
            g2.setColor(alarmOn ? COLOR_ON : Color.GRAY);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String label = alarmOn ? "ON" : "OFF";
            FontMetrics fm = g2.getFontMetrics();
            int textX = (PANEL_SIZE - fm.stringWidth(label)) / 2;
            g2.drawString(label, textX, PANEL_SIZE - 6);

            g2.dispose();
        }
    }
}
