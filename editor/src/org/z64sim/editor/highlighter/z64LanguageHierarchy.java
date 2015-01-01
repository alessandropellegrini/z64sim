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
            new z64TokenId("EOF", "whitespace", 0),
            new z64TokenId("PROGRAM_BEGIN", "keyword", 5),
            new z64TokenId("PROGRAM_END", "keyword", 6),
            new z64TokenId("COMMENT", "comment", 7),
            new z64TokenId("SCALE", "literal", 8),
            new z64TokenId("CONSTANT", "literal", 9),
            new z64TokenId("NUMBER", "literal", 10),
            new z64TokenId("DEC", "literal", 11),
            new z64TokenId("HEX", "literal", 12),
            new z64TokenId("REG_8", "register", 13),
            new z64TokenId("REG_16", "register", 14),
            new z64TokenId("REG_32", "register", 15),
            new z64TokenId("REG_64", "register", 16),
            new z64TokenId("INSN_0", "instruction", 17),
            new z64TokenId("INSN_0_WQ", "instruction", 18),
            new z64TokenId("INSN_0_NOSUFF", "instruction", 19),
            new z64TokenId("INSN_1_S", "instruction", 20),
            new z64TokenId("INSN_1_E", "instruction", 21),
            new z64TokenId("INSN_SHIFT", "instruction", 22),
            new z64TokenId("INSN_1_M", "instruction", 23),
            new z64TokenId("INSN_JC", "instruction", 24),
            new z64TokenId("INSN_B_E", "instruction", 25),
            new z64TokenId("INSN_EXT", "instruction", 26),
            new z64TokenId("INSN_IN", "instruction", 27),
            new z64TokenId("INSN_OUT", "instruction", 28),
            new z64TokenId("INSN_IO_S", "instruction", 29),
            new z64TokenId("LABEL", "string", 30),
            new z64TokenId("STRING", "string", 31),
            new z64TokenId("LBRACE", "operator", 32),
            new z64TokenId("RBRACE", "operator", 33),
            new z64TokenId("SUFFIX", "instruction", 34),
            new z64TokenId("SUFFIX_BWL", "instruction", 35),
            new z64TokenId("SUFFIX_WQ", "instruction", 36),
            new z64TokenId("EXT_SUFFIX", "instruction", 37)
        });
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
        return "text/z64asm";
    }

}
