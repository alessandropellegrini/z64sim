/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.editor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A panel that displays line numbers aligned with the lines of a {@link JTextPane}.
 * Intended to be used as the {@code rowHeaderView} of the {@link JScrollPane}
 * that contains the editor.
 * <p>
 * Automatically updates when the document changes, the viewport scrolls,
 * or the editor's font changes.
 */
public class LineNumberPanel extends JPanel implements DocumentListener, PropertyChangeListener {

    private static final int H_PADDING = 8;
    private final JTextPane editor;

    public LineNumberPanel(JTextPane editor) {
        this.editor = editor;
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, editor.getFont().getSize()));
        setBorder(BorderFactory.createEmptyBorder(0, H_PADDING, 0, H_PADDING));
        setOpaque(true);

        // Listen for document changes to update line count
        editor.getDocument().addDocumentListener(this);
        // Listen for font changes
        editor.addPropertyChangeListener("font", this);
    }

    /**
     * Returns the preferred size: width based on digit count, height matching the editor.
     */
    @Override
    public Dimension getPreferredSize() {
        int lines = getLineCount();
        FontMetrics fm = getFontMetrics(getFont());
        String maxLineNum = String.valueOf(lines);
        int width = fm.stringWidth(maxLineNum) + 2 * H_PADDING;
        return new Dimension(width, editor.getPreferredSize().height);
    }

    /**
     * Trigger a layout recalculation and repaint after a document change.
     */
    private void updateSize() {
        revalidate();
        repaint();
    }

    private int getLineCount() {
        Element root = editor.getDocument().getDefaultRootElement();
        return root.getElementCount();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Use a subdued foreground for line numbers
        g2.setColor(getLineNumberForeground());
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();

        // Get the visible clip to only paint visible lines
        Rectangle clip = g2.getClipBounds();
        int panelWidth = getWidth();

        Element root = editor.getDocument().getDefaultRootElement();
        int lineCount = root.getElementCount();

        for (int i = 0; i < lineCount; i++) {
            try {
                // Get the y position of this line in the editor
                Rectangle rect = editor.modelToView(root.getElement(i).getStartOffset());
                if (rect == null) continue;

                int y = rect.y;
                int lineHeight = rect.height;

                // Skip lines outside the visible clip
                if (y + lineHeight < clip.y) continue;
                if (y > clip.y + clip.height) break;

                // Draw the line number right-aligned
                String lineNum = String.valueOf(i + 1);
                int strWidth = fm.stringWidth(lineNum);
                int x = panelWidth - H_PADDING - strWidth;
                // Baseline = top of line + ascent
                int baseline = y + fm.getAscent();
                g2.drawString(lineNum, x, baseline);
            } catch (Exception e) {
                // modelToView2D can throw if the document is being modified concurrently
                break;
            }
        }

        g2.dispose();
    }

    /**
     * Returns a subdued color for line numbers, adapting to the current background.
     */
    private Color getLineNumberForeground() {
        Color bg = getBackground();
        if (bg == null) bg = Color.WHITE;
        // Determine if we're on a dark background
        int brightness = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
        if (brightness < 128) {
            // Dark background --> light gray numbers
            return new Color(120, 120, 120);
        } else {
            // Light background --> dark gray numbers
            return new Color(140, 140, 140);
        }
    }

    // --- DocumentListener ---

    @Override
    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(this::updateSize);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(this::updateSize);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // Attribute changes don't affect line count, but may affect layout
        SwingUtilities.invokeLater(this::repaint);
    }

    // --- PropertyChangeListener (font changes) ---

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, editor.getFont().getSize()));
        updateSize();
    }
}
