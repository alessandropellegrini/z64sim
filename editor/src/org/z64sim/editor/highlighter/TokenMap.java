/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import org.z64sim.assembler.AssemblerTokenManager;

/**
 * This class is used to bridge the JavaCC token kinds in AsmToken / Token
 * class, and the Syntax Highlighter. Although the Syntax Highlighter is able to
 * directly use tokens declared in AssemblerConstants, some sub-tokens are
 * defined there as well, for the sake of writing a more clear grammar.
 * Therefore, we use this class to filter which are the tokens which should be
 * handled by the Syntax Highlighter, and which ones should be discarded (as a
 * subsequent reduce will most likely assemble them in a recognizable token).
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 * @date December 29, 2014
 */
public class TokenMap {

    private final Map<Integer, SyntaxStyle> map = new HashMap<Integer, SyntaxStyle>();
    
    /**
     * Manually add here in the constructor the tokens which should be handled by
     * the syntax highlighter, along with the syntax coloring associated with them.
     * Anything not specified here, but passed by the Lexer, will be simply ignored
     * and handled using the default style.
     */
    public TokenMap() {

        map.put(AssemblerTokenManager.COMMENT, new SyntaxStyle(Color.GRAY, false, false));
        map.put(AssemblerTokenManager.PROGRAM_BEGIN, new SyntaxStyle(Color.CYAN.darker(), false, false));
        map.put(AssemblerTokenManager.PROGRAM_END, new SyntaxStyle(Color.CYAN.darker(), false, false));
        map.put(AssemblerTokenManager.SCALE, new SyntaxStyle(Color.ORANGE, false, false));
        map.put(AssemblerTokenManager.CONSTANT, new SyntaxStyle(Color.ORANGE, false, false));
        map.put(AssemblerTokenManager.NUMBER, new SyntaxStyle(Color.ORANGE, false, false));
        map.put(AssemblerTokenManager.REG_8, new SyntaxStyle(Color.MAGENTA, false, false));
        map.put(AssemblerTokenManager.REG_16, new SyntaxStyle(Color.MAGENTA, false, false));
        map.put(AssemblerTokenManager.REG_32, new SyntaxStyle(Color.MAGENTA, false, false));
        map.put(AssemblerTokenManager.REG_64, new SyntaxStyle(Color.MAGENTA, false, false));
        map.put(AssemblerTokenManager.INSN_0, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_0_WQ, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_0_NOSUFF, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_1_S, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_1_E, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_SHIFT, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_1_M, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_JC, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_B_E, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_EXT, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_IN, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_OUT, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.INSN_IO_S, new SyntaxStyle(Color.BLUE, true, false));
        map.put(AssemblerTokenManager.LBRACE, new SyntaxStyle(Color.BLACK, true, false));
        map.put(AssemblerTokenManager.RBRACE, new SyntaxStyle(Color.BLACK, true, false));
        map.put(AssemblerTokenManager.LABEL, new SyntaxStyle(Color.DARK_GRAY, true, false));
    }
    
    public SyntaxStyle get(Integer i) {
        return map.get(i);
    }
    
    public boolean containsKey(Integer i) {
        return map.containsKey(i);
    }

}
