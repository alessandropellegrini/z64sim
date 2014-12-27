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
package org.z64sim.assembler;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class AsmToken implements Serializable, Comparable {

    public int start;
    public int length;
    public Token token;

    /**
     * Constructs a new token
     * Actually, this should be never called directly, as this is just a superclass
     * of Token, which is used to keep the position of the token in the character
     * stream in a vector-like way, so that when Tokens are used by the Syntax
     * Highlighter, the identification of the position in the text can be done much
     * more easily. It is the JavaCC lexer that, upon the creation of a Token,
     * correctly initializes the elements of this super class as well.
     */
    public AsmToken() {
    }
    
    public AsmToken(int kind, int start, int length) {
        this.token = new Token();
        this.token.kind = kind;
        this.start = start;
        this.length = length;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Object) {
            AsmToken t = (AsmToken) obj;
            return ((this.start == t.start) &&
                    (this.length == t.length) &&
                    (this.token.kind == t.token.kind));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return start;
    }

    @Override
    public String toString() {
        return String.format("%s (%d, %d)", this.token.kind, start, length);
    }

    @Override
    public int compareTo(Object o) {
        AsmToken t = (AsmToken) o;
        if (this.start !=  t.start) {
            return (this.start - t.start);
        } else if(this.length != t.length) {
            return (this.length - t.length);
        } else {
            Integer i = this.token.kind;
            return i.compareTo(t.token.kind);
        }
    }

    /**
     * return the end position of the token.
     * @return start + length
     */
    public int end() {
        return start + length;
    }
    
    /**
     * Get the text of the token from this document
     * @param doc
     * @return
     */
    public String getText(Document doc) {
        String text = null;
        try {
            text = doc.getText(start, length);
        } catch (BadLocationException ex) {
            Logger.getLogger(AsmToken.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text;
    }
}