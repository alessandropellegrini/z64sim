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

import org.z64sim.assembler.AsmToken;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * This class contains static utility methods to make highliting in text 
 * components easier.
 * 
 * @author Ayman Al-Sairafi
 */
public class Markers {

    /**
     * Removes only our private highlights
     * This is public so that we can remove the highlights when the editorKit
     * is unregistered.  DefaultHighlightPainter can be null, in which case all instances of
     * our Markers are removed.
     * @param component the text component whose markers are to be removed
     * @param marker the DefaultHighlightPainter to remove
     */
    public static void removeMarkers(JTextComponent component, DefaultHighlightPainter marker) {
        Highlighter hilite = component.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();

        for (Highlighter.Highlight hilite1 : hilites) {
            if (hilite1.getPainter() instanceof DefaultHighlightPainter) {
                DefaultHighlightPainter hMarker = (DefaultHighlightPainter) hilite1.getPainter();
                if (marker == null || hMarker.equals(marker)) {
                    hilite.removeHighlight(hilite1);
                }
            }
        }
    }

    /**
     * Remove all the markers from an JEditorPane
     * @param editorPane
     */
    public static void removeMarkers(JTextComponent editorPane) {
        removeMarkers(editorPane, null);
    }

    /**
     * add highlights for the given AsmToken on the given pane
     * @param pane
     * @param token
     * @param marker
     */
    public static void markToken(JTextComponent pane, AsmToken token, DefaultHighlightPainter marker) {
        markText(pane, token.start, token.end(), marker);
    }

    /**
     * add highlights for the given region on the given pane
     * @param pane
     * @param start
     * @param end
     * @param marker
     */
    public static void markText(JTextComponent pane, int start, int end, DefaultHighlightPainter marker) {
        try {
            Highlighter hiliter = pane.getHighlighter();
            int selStart = pane.getSelectionStart();
            int selEnd = pane.getSelectionEnd();
            // if there is no selection or selection does not overlap
            if(selStart == selEnd || end < selStart || start > selStart) {
                hiliter.addHighlight(start, end, marker);
                return;
            }
            // selection starts within the highlight, highlight before slection
            if(selStart > start && selStart < end ) {
                hiliter.addHighlight(start, selStart, marker);
            }
            // selection ends within the highlight, highlight remaining
            if(selEnd > start && selEnd < end ) {
                hiliter.addHighlight(selEnd, end, marker);
            }

        } catch (BadLocationException ex) {
            // nothing we can do if the request is out of bound
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Mark all text in the document that matches the given pattern
     * @param pane control to use
     * @param pattern pattern to match
     * @param marker marker to use for highlighting
     */
    public static void markAll(JTextComponent pane, Pattern pattern, DefaultHighlightPainter marker) {
        SyntaxDocument sDoc = ActionUtils.getSyntaxDocument(pane);
        if(sDoc  == null || pattern == null) {
            return;
        }
        Matcher matcher = sDoc.getMatcher(pattern);
        while(matcher.find()) {
            markText(pane, matcher.start(), matcher.end(), marker);
        }
    }
    
    private static final Logger LOG = Logger.getLogger(Markers.class.getName());
}