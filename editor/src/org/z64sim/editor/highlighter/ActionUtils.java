/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License 
 *       at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 */
package org.z64sim.editor.highlighter;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.MutableComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.z64sim.assembler.AsmToken;


/**
 * Various utility methods to work on JEditorPane and its SyntaxDocument
 * for use by Actions
 *
 * @author Ayman Al-Sairafi
 */
public class ActionUtils {

    /**
     * A helper function that will return the SyntaxDocument attached to the
     * given text component.  Return null if the document is not a 
     * SyntaxDocument, or if the text component is null
     * @param component
     * @return
     */
    public static SyntaxDocument getSyntaxDocument(JTextComponent component) {
        if (component == null) {
            return null;
        }
        Document doc = component.getDocument();
        if (doc instanceof SyntaxDocument) {
            return (SyntaxDocument) doc;
        } else {
            return null;
        }
    }

    /**
     * Gets the Line Number at the give position of the editor component.
     * The first line number is ZERO
     * @param editor
     * @param pos
     * @return line number
     * @throws javax.swing.text.BadLocationException
     */
    public static int getLineNumber(JTextComponent editor, int pos)
            throws BadLocationException {
        if (getSyntaxDocument(editor) != null) {
            SyntaxDocument sdoc = getSyntaxDocument(editor);
            return sdoc.getLineNumberAt(pos);
        } else {
            Document doc = editor.getDocument();
            return doc.getDefaultRootElement().getElementIndex(pos);
        }
    }

    /**
     * Gets the column number at given position of editor.  The first column is
     * ZERO
     * @param editor
     * @param pos
     * @return the 0 based column number
     * @throws javax.swing.text.BadLocationException
     */
    public static int getColumnNumber(JTextComponent editor, int pos)
            throws BadLocationException {
        Rectangle r = editor.modelToView(pos);
        int start = editor.viewToModel(new Point(0, r.y));
        int column = pos - start;
        return column;
    }

}