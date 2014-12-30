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

import java.awt.Color;
import java.util.Iterator;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultHighlighter;
import org.z64sim.assembler.AsmToken;

/**
 * This class highlights Tokens within a document whenever the caret is moved
 * to a TokenType provided in the config file.
 * 
 * @author Ayman Al-Sairafi
 */
public class TokenMarker implements CaretListener {

    private JEditorPane pane;
    private final DefaultHighlighter.DefaultHighlightPainter marker = 
                    new DefaultHighlighter.DefaultHighlightPainter(Color.WHITE);
    private final TokenMap tMap = new TokenMap();

    /**
     * Constructs a new AsmToken highlighter
     */
    public TokenMarker() {
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        int pos = e.getDot();
        SyntaxDocument doc = ActionUtils.getSyntaxDocument(pane);
        AsmToken token = doc.getTokenAt(pos);
        removeMarkers();
               
        if (token != null && tMap.containsKey(token.token.kind)) {
            addMarkers(token);
        }
    }

    /**
     * removes all markers from the pane.
     */
    public void removeMarkers() {
        Markers.removeMarkers(pane, marker);
    }

    /**
     * add highlights for the given pattern
     * @param pattern
     */
    void addMarkers(AsmToken tok) {
        SyntaxDocument sDoc = (SyntaxDocument) pane.getDocument();
        sDoc.readLock();
        String text = tok.getText(sDoc);
        Iterator<AsmToken> it = sDoc.getTokens(0, sDoc.getLength());
        while (it.hasNext()) {
            AsmToken nextToken = it.next();
            if (nextToken.length == tok.length && text.equals(nextToken.getText(sDoc))) {
                Markers.markToken(pane, nextToken, marker);
            }
        }
        sDoc.readUnlock();
    }

    public void install(JEditorPane editor) {
        this.pane = editor;
        pane.addCaretListener(this);
    }

    public void deinstall(JEditorPane editor) {
        removeMarkers();
        pane.removeCaretListener(this);
    }

}