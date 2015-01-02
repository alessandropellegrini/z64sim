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
            new z64TokenId("WHITESPACE", "whitespace", 1),
            new z64TokenId("COMMENT", "comment", 2),
            new z64TokenId("LOCATION_COUNTER", "keyword", 3),
            new z64TokenId("DATA_SECTION", "keyword", 4),
            new z64TokenId("CODE_SECTION", "keyword", 5),
            new z64TokenId("BSS_SECTION", "keyword", 6),
            new z64TokenId("PROGRAM_END", "keyword", 7),
            new z64TokenId("EQU_ASSIGN", "keyword", 8),
            new z64TokenId("BYTE_ASSIGN", "keyword", 9),
            new z64TokenId("WORD_ASSIGN", "keyword", 10),
            new z64TokenId("LONG_ASSIGN", "keyword", 11),
            new z64TokenId("QUAD_ASSIGN", "keyword", 12),
            new z64TokenId("ASCII_ASSIGN", "keyword", 13),
            new z64TokenId("FILL_ASSIGN", "keyword", 14),
            new z64TokenId("COMM_ASSIGN", "keyword", 15),
            new z64TokenId("DRIVER", "keyword", 16),
            new z64TokenId("SCALE", "literal", 17),
            new z64TokenId("CONSTANT", "number", 18),
            new z64TokenId("NUMBER", "number", 19),
            new z64TokenId("DEC", "number", 20),
            new z64TokenId("HEX", "number", 21),
            new z64TokenId("ASSIGN", "operator", 22),
            new z64TokenId("PLUS", "operator", 23),
            new z64TokenId("MINUS", "operator", 24),
            new z64TokenId("TIMES", "operator", 25),
            new z64TokenId("DIVIDE", "operator", 26),
            new z64TokenId("LBRACE", "operator", 27),
            new z64TokenId("RBRACE", "operator", 28),
            new z64TokenId("COMMA", "separator", 29),
            new z64TokenId("REG_8", "identifier", 30),
            new z64TokenId("REG_16", "identifier", 31),
            new z64TokenId("REG_32", "identifier", 32),
            new z64TokenId("REG_64", "identifier", 33),
            new z64TokenId("INSN_0", "instruction", 34),
            new z64TokenId("INSN_0_WQ", "instruction", 35),
            new z64TokenId("INSN_0_NOSUFF", "instruction", 36),
            new z64TokenId("INSN_1_S", "instruction", 37),
            new z64TokenId("INSN_LEA", "instruction", 38),
            new z64TokenId("INSN_1_E", "instruction", 39),
            new z64TokenId("INSN_SHIFT", "instruction", 40),
            new z64TokenId("INSN_1_M", "instruction", 41),
            new z64TokenId("INSN_JC", "instruction", 42),
            new z64TokenId("INSN_B_E", "instruction", 43),
            new z64TokenId("INSN_EXT", "instruction", 44),
            new z64TokenId("INSN_IN", "instruction", 45),
            new z64TokenId("INSN_OUT", "instruction", 46),
            new z64TokenId("INSN_IO_S", "instruction", 47),
            new z64TokenId("LABEL", "string", 48),
            new z64TokenId("STRING_LITERAL", "string", 49),
            new z64TokenId("ERROR", "whitespace", 50),
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
