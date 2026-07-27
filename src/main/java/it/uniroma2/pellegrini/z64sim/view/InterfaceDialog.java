/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Modal dialog that renders a device's interface schematic with
 * zoom (mouse-wheel + buttons) and scroll, plus PNG export.
 */
public class InterfaceDialog extends JDialog {

    private static final double ZOOM_STEP = 0.1;
    private static final double ZOOM_MIN = 0.2;
    private static final double ZOOM_MAX = 5.0;

    private final DeviceDescriptor descriptor;
    private final InterfaceRenderer renderer = new InterfaceRenderer();
    private final SchematicPanel panel;
    private double zoom = 1.0;

    public InterfaceDialog(Window owner, DeviceDescriptor descriptor) {
        super(owner, "Interface — " + descriptor.getDeviceName(),
                ModalityType.APPLICATION_MODAL);
        this.descriptor = descriptor;

        panel = new SchematicPanel();

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        // Toolbar with zoom controls and export
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton zoomInBtn = new JButton("+");
        zoomInBtn.setToolTipText("Zoom In");
        zoomInBtn.addActionListener(e -> adjustZoom(ZOOM_STEP));

        JButton zoomOutBtn = new JButton("\u2212"); // minus sign
        zoomOutBtn.setToolTipText("Zoom Out");
        zoomOutBtn.addActionListener(e -> adjustZoom(-ZOOM_STEP));

        JButton zoomFitBtn = new JButton("Fit");
        zoomFitBtn.setToolTipText("Fit to Window");
        zoomFitBtn.addActionListener(e -> fitToWindow(scrollPane));

        JButton zoomResetBtn = new JButton("1:1");
        zoomResetBtn.setToolTipText("Reset Zoom");
        zoomResetBtn.addActionListener(e -> {
            zoom = 1.0;
            panel.revalidate();
            panel.repaint();
        });

        JButton exportBtn = new JButton("Export PNG");
        exportBtn.setToolTipText("Save diagram as PNG");
        exportBtn.addActionListener(e -> exportPng());

        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.addSeparator();
        toolbar.add(zoomFitBtn);
        toolbar.add(zoomResetBtn);
        toolbar.addSeparator();
        toolbar.add(exportBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Mouse-wheel zoom
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown() || e.isMetaDown()) {
                e.consume();
                double delta = e.getWheelRotation() < 0 ? ZOOM_STEP : -ZOOM_STEP;
                adjustZoom(delta);
            }
        });

        setSize(900, 700);
        setLocationRelativeTo(owner);
    }

    private void adjustZoom(double delta) {
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom + delta));
        panel.revalidate();
        panel.repaint();
    }

    private void fitToWindow(JScrollPane scrollPane) {
        Dimension base = computeBaseSize();
        Dimension viewport = scrollPane.getViewport().getSize();
        if (viewport.width <= 0 || viewport.height <= 0) return;
        double scaleX = (double) viewport.width / base.width;
        double scaleY = (double) viewport.height / base.height;
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, Math.min(scaleX, scaleY)));
        panel.revalidate();
        panel.repaint();
    }

    /**
     * Render at 1:1 to a scratch image to measure the natural dimensions.
     */
    private Dimension computeBaseSize() {
        BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scratch.createGraphics();
        Dimension d = renderer.render(g2, descriptor);
        g2.dispose();
        return d;
    }

    private void exportPng() {
        Dimension base = computeBaseSize();
        int w = (int) Math.ceil(base.width * zoom);
        int h = (int) Math.ceil(base.height * zoom);
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.scale(zoom, zoom);
        renderer.render(g2, descriptor);
        g2.dispose();

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(descriptor.getDeviceName() + ".png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(image, "PNG", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this,
                        "Saved to " + chooser.getSelectedFile().getAbsolutePath(),
                        "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Inner panel that renders the schematic at the current zoom level.
     */
    private class SchematicPanel extends JPanel {
        SchematicPanel() {
            setBackground(Color.WHITE);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension base = computeBaseSize();
            return new Dimension(
                    (int) Math.ceil(base.width * zoom),
                    (int) Math.ceil(base.height * zoom));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.scale(zoom, zoom);
            renderer.render(g2, descriptor);
            g2.dispose();
        }
    }
}
