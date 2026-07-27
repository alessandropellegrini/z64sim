/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.editor;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A find/replace bar that sits below the editor pane.
 * <p>
 * All visible text is internationalised via the {@code i18n} resource bundle.
 * The replace row is toggled via a small expand button or by opening the bar
 * with {@link #open(boolean) open(true)}.
 */
public class FindReplaceBar extends JPanel {

    private final JTextPane editor;
    private final JTextField findField;
    private final JTextField replaceField;
    private final JLabel countLabel;
    private final JPanel replaceRow;
    private final JButton toggleReplaceBtn;

    private final Highlighter.HighlightPainter matchPainter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 100, 150));
    private final Highlighter.HighlightPainter currentMatchPainter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 150, 0, 150));

    private final List<Match> matches = new ArrayList<>();
    private int currentMatchIndex = -1;
    private boolean replaceVisible = false;

    private static class Match {
        int start;
        int end;
        Object highlightTag;

        Match(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public FindReplaceBar(JTextPane editor) {
        this.editor = editor;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        setVisible(false);

        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        Insets btnMargin = new Insets(1, 4, 1, 4);

        // ---- Shared GridBagConstraints ----
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 2, 1, 2);
        gbc.anchor = GridBagConstraints.WEST;

        // ========== Find row (row 0) ==========

        // Toggle replace button
        toggleReplaceBtn = new JButton("\u25b6");
        toggleReplaceBtn.setToolTipText(PropertyBroker.getMessageFromBundle("find.toggle.replace.tooltip"));
        toggleReplaceBtn.setMargin(btnMargin);
        toggleReplaceBtn.setFocusable(false);
        toggleReplaceBtn.addActionListener(e -> toggleReplace());
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(toggleReplaceBtn, gbc);

        // Find label
        JLabel findLabel = new JLabel(PropertyBroker.getMessageFromBundle("find.label"));
        gbc.gridx = 1;
        add(findLabel, gbc);

        // Find text field
        findField = new JTextField(20);
        findField.setFont(monoFont);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(findField, gbc);

        // Previous button
        JButton prevBtn = new JButton("\u25b2");
        prevBtn.setMargin(btnMargin);
        prevBtn.setToolTipText(PropertyBroker.getMessageFromBundle("find.prev.tooltip"));
        prevBtn.setFocusable(false);
        prevBtn.addActionListener(e -> findPrevious());
        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(prevBtn, gbc);

        // Next button
        JButton nextBtn = new JButton("\u25bc");
        nextBtn.setMargin(btnMargin);
        nextBtn.setToolTipText(PropertyBroker.getMessageFromBundle("find.next.tooltip"));
        nextBtn.setFocusable(false);
        nextBtn.addActionListener(e -> findNext());
        gbc.gridx = 4;
        add(nextBtn, gbc);

        // Match count
        countLabel = new JLabel(PropertyBroker.getMessageFromBundle("find.no.matches"));
        countLabel.setPreferredSize(new Dimension(80, countLabel.getPreferredSize().height));
        gbc.gridx = 5;
        add(countLabel, gbc);

        // Close button
        JButton closeBtn = new JButton("\u2715");
        closeBtn.setMargin(btnMargin);
        closeBtn.setToolTipText(PropertyBroker.getMessageFromBundle("find.close.tooltip"));
        closeBtn.setFocusable(false);
        closeBtn.addActionListener(e -> close());
        gbc.gridx = 6;
        add(closeBtn, gbc);

        // ========== Replace row (row 1) — initially hidden ==========

        replaceRow = new JPanel(new GridBagLayout());
        replaceRow.setVisible(false);
        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.insets = new Insets(1, 2, 1, 2);
        rgbc.anchor = GridBagConstraints.WEST;

        // Spacer matching the toggle button width
        rgbc.gridx = 0; rgbc.gridy = 0;
        rgbc.fill = GridBagConstraints.NONE;
        rgbc.weightx = 0;
        replaceRow.add(Box.createRigidArea(toggleReplaceBtn.getPreferredSize()), rgbc);

        // Replace label
        JLabel replaceLabel = new JLabel(PropertyBroker.getMessageFromBundle("find.replace.label"));
        rgbc.gridx = 1;
        replaceRow.add(replaceLabel, rgbc);

        // Replace text field
        replaceField = new JTextField(20);
        replaceField.setFont(monoFont);
        rgbc.gridx = 2;
        rgbc.fill = GridBagConstraints.HORIZONTAL;
        rgbc.weightx = 1.0;
        replaceRow.add(replaceField, rgbc);

        // Replace button
        JButton replaceBtn = new JButton(PropertyBroker.getMessageFromBundle("find.replace.button"));
        replaceBtn.setMargin(btnMargin);
        replaceBtn.setFocusable(false);
        replaceBtn.addActionListener(e -> replaceCurrent());
        rgbc.gridx = 3;
        rgbc.fill = GridBagConstraints.NONE;
        rgbc.weightx = 0;
        replaceRow.add(replaceBtn, rgbc);

        // Replace All button
        JButton replaceAllBtn = new JButton(PropertyBroker.getMessageFromBundle("find.replace.all.button"));
        replaceAllBtn.setMargin(btnMargin);
        replaceAllBtn.setFocusable(false);
        replaceAllBtn.addActionListener(e -> replaceAll());
        rgbc.gridx = 4;
        replaceRow.add(replaceAllBtn, rgbc);

        // Add the replace row spanning the full width
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(replaceRow, gbc);

        // ---- Event listeners ----

        findField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            public void changedUpdate(DocumentEvent e) { updateSearch(); }
        });

        findField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (e.isShiftDown()) findPrevious(); else findNext();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    close();
                }
            }
        });

        replaceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    close();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    replaceCurrent();
                }
            }
        });

        // Global Escape handler
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { close(); }
        });
    }

    // ---- Public API ----

    /**
     * Show the find bar. If {@code showReplace} is true, the replace row
     * is also expanded. Pre-fills the find field with the current selection.
     */
    public void open(boolean showReplace) {
        setVisible(true);
        if (showReplace != replaceVisible) {
            toggleReplace();
        }
        String selection = editor.getSelectedText();
        if (selection != null && !selection.isEmpty()) {
            findField.setText(selection);
        } else {
            updateSearch();
        }
        findField.requestFocusInWindow();
        findField.selectAll();
    }

    /** Hide the bar and clear all highlights from the editor. */
    public void close() {
        setVisible(false);
        clearHighlights();
        editor.requestFocusInWindow();
    }

    /** Return whether the bar is currently visible. */
    public boolean isBarVisible() {
        return isVisible();
    }

    // ---- Internal ----

    private void toggleReplace() {
        replaceVisible = !replaceVisible;
        replaceRow.setVisible(replaceVisible);
        toggleReplaceBtn.setText(replaceVisible ? "\u25bc" : "\u25b6");
        revalidate();
        repaint();
    }

    private void updateSearch() {
        clearHighlights();
        currentMatchIndex = -1;

        String query = findField.getText();
        if (query.isEmpty()) {
            countLabel.setText(PropertyBroker.getMessageFromBundle("find.no.matches"));
            return;
        }

        String queryLower = query.toLowerCase();
        Document doc = editor.getDocument();
        try {
            String text = doc.getText(0, doc.getLength()).toLowerCase();
            Highlighter hl = editor.getHighlighter();
            int index = text.indexOf(queryLower);
            while (index >= 0) {
                Match m = new Match(index, index + query.length());
                m.highlightTag = hl.addHighlight(m.start, m.end, matchPainter);
                matches.add(m);
                index = text.indexOf(queryLower, index + query.length());
            }
            if (!matches.isEmpty()) {
                currentMatchIndex = findClosestMatchIndex();
                highlightCurrentMatch();
            } else {
                countLabel.setText(PropertyBroker.getMessageFromBundle("find.no.matches"));
            }
        } catch (BadLocationException ignored) {
        }
    }

    private int findClosestMatchIndex() {
        int caretPos = editor.getCaretPosition();
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).start >= caretPos) return i;
        }
        return 0;
    }

    private void findNext() {
        if (matches.isEmpty()) return;
        currentMatchIndex = (currentMatchIndex + 1) % matches.size();
        highlightCurrentMatch();
    }

    private void findPrevious() {
        if (matches.isEmpty()) return;
        currentMatchIndex = (currentMatchIndex - 1 + matches.size()) % matches.size();
        highlightCurrentMatch();
    }

    private void highlightCurrentMatch() {
        if (matches.isEmpty() || currentMatchIndex < 0) return;
        Highlighter hl = editor.getHighlighter();
        for (int i = 0; i < matches.size(); i++) {
            Match m = matches.get(i);
            try {
                if (m.highlightTag != null) hl.removeHighlight(m.highlightTag);
                if (i == currentMatchIndex) {
                    m.highlightTag = hl.addHighlight(m.start, m.end, currentMatchPainter);
                    editor.setCaretPosition(m.end);
                    editor.select(m.start, m.end);
                } else {
                    m.highlightTag = hl.addHighlight(m.start, m.end, matchPainter);
                }
            } catch (BadLocationException ignored) {
            }
        }
        countLabel.setText((currentMatchIndex + 1) + " / " + matches.size());
    }

    private void clearHighlights() {
        Highlighter hl = editor.getHighlighter();
        for (Match m : matches) {
            if (m.highlightTag != null) hl.removeHighlight(m.highlightTag);
        }
        matches.clear();
        countLabel.setText(PropertyBroker.getMessageFromBundle("find.no.matches"));
    }

    private void replaceCurrent() {
        if (matches.isEmpty() || currentMatchIndex < 0) return;
        Match m = matches.get(currentMatchIndex);
        try {
            Document doc = editor.getDocument();
            doc.remove(m.start, m.end - m.start);
            doc.insertString(m.start, replaceField.getText(), null);
            updateSearch();
        } catch (BadLocationException ignored) {
        }
    }

    private void replaceAll() {
        if (matches.isEmpty()) return;
        String replacement = replaceField.getText();
        try {
            Document doc = editor.getDocument();
            for (int i = matches.size() - 1; i >= 0; i--) {
                Match m = matches.get(i);
                doc.remove(m.start, m.end - m.start);
                doc.insertString(m.start, replacement, null);
            }
            updateSearch();
        } catch (BadLocationException ignored) {
        }
    }
}
