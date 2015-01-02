/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class z64IndentTask implements IndentTask {

    public static final int INDENT = 4;
    private final Context context;

    z64IndentTask(Context context) {
        this.context = context;
    }

    @Override
    public void reindent() throws BadLocationException {
        if (context.isIndent()) {
            reindentPreviousLine();
            reindentCurrentLine();
        }
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }
    
    /** If we have just inserted a block operator, then indent it to the left */
    private void reindentPreviousLine() throws BadLocationException {
        int caretOffset = context.caretOffset();
        int prevLineOffset = context.lineStartOffset(caretOffset - 1);
        Document doc = context.document();

        String lastLine = doc.getText(prevLineOffset, caretOffset - prevLineOffset);
        int lastLineStart = lineStart(lastLine);

        boolean block = isBlockOpener(lastLine);

        int indent = lastLineStart - lastLineStart % INDENT - (block ? INDENT : 0);
        if(indent < 0)
            indent = 0;

        context.modifyIndent(prevLineOffset, indent);
    }

    private void reindentCurrentLine() throws BadLocationException {
        int caretOffset = context.caretOffset();
        int prevLineOffset = context.lineStartOffset(caretOffset - 1);
        int lineOffset = context.lineStartOffset(caretOffset);
        Document doc = context.document();

        String lastLine = doc.getText(prevLineOffset, caretOffset - prevLineOffset);
        int lastLineStart = lineStart(lastLine);

        boolean block = isBlockOpener(lastLine);

        int indent = lastLineStart - lastLineStart % INDENT + (block ? INDENT : 0);

        context.modifyIndent(lineOffset, indent);
    }

    private int lineStart(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != ' ') {
                return i;
            }
        }
        return 0;
    }

    private boolean isBlockOpener(String line) {
        int i = 0;
        char c;
        do {
            c = line.charAt(i++);
        } while (i < line.length() && Character.isWhitespace(c));

        return (i < line.length() && c == '.');
    }

}
