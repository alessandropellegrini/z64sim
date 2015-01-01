/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import java.util.*;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class z64LanguageHierarchy extends LanguageHierarchy<z64TokenId> {

    private static List<z64TokenId> tokens;
    private static Map<Integer, z64TokenId> idToToken;

    private static void init() {
        tokens = Arrays.<z64TokenId>asList(new z64TokenId[]{
            new z64TokenId("EOF", "", 0),
            new z64TokenId("PROGRAM_BEGIN", "", 0),
            new z64TokenId("PROGRAM_END", "", 0),
            new z64TokenId("COMMENT", "", 0),
            new z64TokenId("SCALE", "", 0),
            new z64TokenId("CONSTANT", "", 0),
            new z64TokenId("NUMBER", "", 0),
            new z64TokenId("DEC", "", 0),
            new z64TokenId("HEX", "", 0),
            new z64TokenId("REG_8", "", 0),
            new z64TokenId("REG_16", "", 0),
            new z64TokenId("REG_32", "", 0),
            new z64TokenId("REG_64", "", 0),
            new z64TokenId("INSN_0", "", 0),
            new z64TokenId("INSN_0_WQ", "", 0),
            new z64TokenId("INSN_0_NOSUFF", "", 0),
            new z64TokenId("INSN_1_S", "", 0),
            new z64TokenId("INSN_1_E", "", 0),
            new z64TokenId("INSN_SHIFT", "", 0),
            new z64TokenId("INSN_1_M", "", 0),
            new z64TokenId("INSN_JC", "", 0),
            new z64TokenId("INSN_B_E", "", 0),
            new z64TokenId("INSN_EXT", "", 0),
            new z64TokenId("INSN_IN", "", 0),
            new z64TokenId("INSN_OUT", "", 0),
            new z64TokenId("INSN_IO_S", "", 0),
            new z64TokenId("LABEL", "", 0),
            new z64TokenId("STRING", "", 0),
            new z64TokenId("LBRACE", "", 0),
            new z64TokenId("RBRACE", "", 0),
            new z64TokenId("SUFFIX", "", 0),
            new z64TokenId("SUFFIX_BWL", "", 0),
            new z64TokenId("SUFFIX_WQ", "", 0),
            new z64TokenId("EXT_SUFFIX", "", 0),});
        idToToken = new HashMap<Integer, z64TokenId>();
        for (z64TokenId token : tokens) {
            idToToken.put(token.ordinal(), token);
        }
    }

    static synchronized z64TokenId getToken(int id) {
        if (idToToken == null) {
            init();
        }
        return idToToken.get(id);
    }

    @Override
    protected synchronized Collection<z64TokenId> createTokenIds() {
        if (tokens == null) {
            init();
        }
        return tokens;
    }

    @Override
    protected synchronized Lexer<z64TokenId> createLexer(LexerRestartInfo<z64TokenId> info) {
        return new z64Lexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-sj";
    }

}
