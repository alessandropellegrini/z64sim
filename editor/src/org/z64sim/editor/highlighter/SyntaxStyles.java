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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import org.z64sim.assembler.AssemblerTokenManager;
//import org.z64sim.editor.jsyntaxpane.util.JarServiceProvider;

/**
 * The STyles to use for each TokenType. The defaults are created here, and then
 * the resource META-INF/services/syntaxstyles.properties is read and merged.
 * You can also pass a properties instance and merge your prefered styles into
 * the default styles
 *
 * @author Ayman
 */
public class SyntaxStyles {

    Map<Integer, SyntaxStyle> styles;
    private static SyntaxStyles instance = createInstance();
    private static final Logger LOG = Logger.getLogger(SyntaxStyles.class.getName());

    private static SyntaxStyle DEFAULT_STYLE = new SyntaxStyle(Color.BLACK, Font.PLAIN);

    private SyntaxStyles() {
        styles = new HashMap<Integer, SyntaxStyle>();
        styles.put(AssemblerTokenManager.COMMENT, new SyntaxStyle(Color.GRAY, false, false));
        styles.put(AssemblerTokenManager.PROGRAM_BEGIN, new SyntaxStyle(Color.BLUE, false, false));
        styles.put(AssemblerTokenManager.PROGRAM_END, new SyntaxStyle(Color.BLUE, false, false));
        styles.put(AssemblerTokenManager.SCALE, new SyntaxStyle(Color.ORANGE, false, false));
        styles.put(AssemblerTokenManager.CONSTANT, new SyntaxStyle(Color.ORANGE, false, false));
        styles.put(AssemblerTokenManager.NUMBER, new SyntaxStyle(Color.ORANGE, false, false));
        styles.put(AssemblerTokenManager.REG_8, new SyntaxStyle(Color.RED, false, false));
        styles.put(AssemblerTokenManager.REG_16, new SyntaxStyle(Color.RED, false, false));
        styles.put(AssemblerTokenManager.REG_32, new SyntaxStyle(Color.RED, false, false));
        styles.put(AssemblerTokenManager.REG_64, new SyntaxStyle(Color.RED, false, false));
        styles.put(AssemblerTokenManager.INSN_0, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_0_WQ, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_0_NOSUFF, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_1_S, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_1_E, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_SHIFT, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_1_M, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_JC, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_B_E, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_EXT, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_IN, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_OUT, new SyntaxStyle(Color.GREEN, false, false));
        styles.put(AssemblerTokenManager.INSN_IO_S, new SyntaxStyle(Color.GREEN, false, false));
    }

    /**
     * Create default styles
     *
     * @return
     */
    private static SyntaxStyles createInstance() {
        SyntaxStyles syntaxstyles = new SyntaxStyles();
//        Properties styles = JarServiceProvider.readProperties(SyntaxStyles.class);
//        syntaxstyles.mergeStyles(styles);
        return syntaxstyles;
    }

    public static SyntaxStyles getInstance() {
        return instance;
    }


    /**
     * Set the graphics font and others to the style for the given token
     *
     * @param g
     * @param type
     * @deprecated
     */
    @Deprecated
    public void setGraphicsStyle(Graphics g, int type) {
        Font c = g.getFont();
        SyntaxStyle ss = styles.get(type);
        if (ss != null) {
            g.setFont(g.getFont().deriveFont(ss.getFontStyle()));
            g.setColor(ss.getColor());
        } else {
            g.setFont(g.getFont().deriveFont(Font.PLAIN));
            g.setColor(Color.BLACK);
        }
    }

    /**
     * Return the style for the given TokenType
     *
     * @param type
     * @return
     */
    public SyntaxStyle getStyle(int type) {
        if (styles.containsKey(type)) {
            return styles.get(type);
        } else {
            return DEFAULT_STYLE;
        }
    }

    /**
     * Draw the given AsmToken. This will simply find the proper SyntaxStyle for
 the TokenType and then asks the proper Style to draw the text of the
 AsmToken.
     *
     * @param segment
     * @param x
     * @param y
     * @param graphics
     * @param e
     * @param token
     * @return
     */
    public int drawText(Segment segment, int x, int y,
            Graphics graphics, TabExpander e, AsmToken tok) {
        SyntaxStyle s = getStyle(tok.token.kind);
        return s.drawText(segment, x, y, graphics, e, tok.start);
    }
}
