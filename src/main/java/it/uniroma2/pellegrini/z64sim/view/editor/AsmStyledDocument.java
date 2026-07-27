/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.editor;

import javax.swing.*;
import javax.swing.text.*;

/**
 * A {@link DefaultStyledDocument} subclass that automatically triggers
 * syntax highlighting after every text insertion or removal.
 * <p>
 * Uses a coalescing mechanism to avoid redundant re-highlighting
 * during rapid typing: the highlight pass is deferred to the next
 * EDT idle cycle via {@link SwingUtilities#invokeLater}, and duplicate
 * requests are collapsed.
 * <p>
 * Suppresses {@link javax.swing.event.UndoableEditEvent} firing during
 * highlighting passes so that style changes do not pollute the undo stack.
 */
public class AsmStyledDocument extends DefaultStyledDocument {

    private final AsmSyntaxHighlighter highlighter;
    private boolean highlightingEnabled = true;
    private boolean highlightPending = false;
    private boolean highlighting = false;

    public AsmStyledDocument(AsmSyntaxHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    /**
     * Enable or disable automatic highlighting. When disabled (e.g., during
     * bulk setText operations), edits will not trigger re-highlighting.
     * When re-enabled, a highlight pass is immediately scheduled.
     */
    public void setHighlightingEnabled(boolean enabled) {
        this.highlightingEnabled = enabled;
        if (enabled) {
            scheduleHighlight();
        }
    }

    /**
     * Force a full re-highlight of the document. Useful after theme changes.
     */
    public void rehighlight() {
        scheduleHighlight();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // Auto-indent: when the user presses Enter, copy the leading
        // whitespace from the current line onto the new line.
        if ("\n".equals(str) || "\r\n".equals(str) || "\r".equals(str)) {
            String indent = getLeadingWhitespace(offs);
            super.insertString(offs, str + indent, a);
        } else {
            super.insertString(offs, str, a);
        }
        if (highlightingEnabled) {
            scheduleHighlight();
        }
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        super.remove(offs, len);
        if (highlightingEnabled) {
            scheduleHighlight();
        }
    }

    /**
     * Suppress undo events fired during highlighting. Style attribute changes
     * must not end up in the undo stack.
     */
    @Override
    protected void fireUndoableEditUpdate(javax.swing.event.UndoableEditEvent e) {
        if (!highlighting) {
            super.fireUndoableEditUpdate(e);
        }
    }

    /**
     * Schedule a highlight pass on the EDT, coalescing multiple rapid requests.
     */
    private void scheduleHighlight() {
        if (!highlightPending) {
            highlightPending = true;
            SwingUtilities.invokeLater(() -> {
                highlightPending = false;
                highlighting = true;
                try {
                    highlighter.highlight(this);
                } finally {
                    highlighting = false;
                }
            });
        }
    }

    /**
     * Returns the leading whitespace (spaces and tabs) of the line
     * that contains the given document offset.
     */
    private String getLeadingWhitespace(int offs) throws BadLocationException {
        Element root = getDefaultRootElement();
        int lineIndex = root.getElementIndex(offs);
        Element line = root.getElement(lineIndex);
        int lineStart = line.getStartOffset();
        int lineEnd = line.getEndOffset();
        String lineText = getText(lineStart, lineEnd - lineStart);

        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < lineText.length(); i++) {
            char ch = lineText.charAt(i);
            if (ch == ' ' || ch == '\t') {
                indent.append(ch);
            } else {
                break;
            }
        }
        return indent.toString();
    }
}
