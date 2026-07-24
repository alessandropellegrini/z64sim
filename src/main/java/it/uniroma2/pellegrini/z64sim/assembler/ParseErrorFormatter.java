/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.assembler;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;

import java.util.LinkedHashSet;

/**
 * Formats JavaCC {@link ParseException} objects into concise, human-readable
 * error messages suitable for display in the compiler output panel.
 */
public final class ParseErrorFormatter {

    private ParseErrorFormatter() {}

    /**
     * Format a {@link ParseException} into a human-readable error message.
     * <p>
     * For structured parse errors (with {@code currentToken} and
     * {@code expectedTokenSequences}), produces messages like:
     * <pre>Line 5: Unexpected "sbraga" — expected instruction, directive, or label</pre>
     * <p>
     * For manually thrown {@code ParseException}s with only a string message,
     * returns the message as-is.
     */
    public static String format(ParseException ex) {
        if (ex.currentToken == null || ex.expectedTokenSequences == null) {
            return ex.getMessage();
        }

        Token errorToken = ex.currentToken.next;
        int line = errorToken != null ? errorToken.beginLine : 0;
        String found = errorToken != null ? errorToken.image : "EOF";

        // Build a human-readable list of expected token categories
        LinkedHashSet<String> expectedSet = new LinkedHashSet<>();
        for (int[] seq : ex.expectedTokenSequences) {
            if (seq.length > 0) {
                String friendly = friendlyTokenName(seq[0]);
                if (friendly != null) {
                    expectedSet.add(friendly);
                }
            }
        }

        if (expectedSet.isEmpty()) {
            return PropertyBroker.getMessageFromBundle("parse.error.no.expected", line, found);
        }

        // Join with commas and "or" before the last item
        StringBuilder joined = new StringBuilder();
        String[] items = expectedSet.toArray(new String[0]);
        for (int i = 0; i < items.length; i++) {
            if (i > 0 && i == items.length - 1) {
                joined.append(", or ");
            } else if (i > 0) {
                joined.append(", ");
            }
            joined.append(items[i]);
        }

        return PropertyBroker.getMessageFromBundle("parse.error", line, found, joined.toString());
    }

    /**
     * Map a JavaCC token kind to a human-readable, i18n category name.
     * Returns {@code null} for tokens that should be suppressed in error messages.
     */
    private static String friendlyTokenName(int tokenKind) {
        String key = tokenKey(tokenKind);
        if (key == null) return null;
        // Punctuation tokens are returned as-is (not i18n keys)
        if (key.startsWith("\"")) return key;
        return PropertyBroker.getMessageFromBundle(key);
    }

    /**
     * Map a JavaCC token kind to its i18n property key, or a literal
     * string for punctuation tokens.
     */
    private static String tokenKey(int tokenKind) {
        switch (tokenKind) {
            case AssemblerConstants.NEWLINE: return null;
            case AssemblerConstants.COMMENT: return "token.comment";
            case AssemblerConstants.LOCATION_COUNTER: return "token.location.counter";
            case AssemblerConstants.ORG:
            case AssemblerConstants.DATA_SECTION:
            case AssemblerConstants.CODE_SECTION:
            case AssemblerConstants.BSS_SECTION:
            case AssemblerConstants.EQU_ASSIGN:
            case AssemblerConstants.BYTE_ASSIGN:
            case AssemblerConstants.WORD_ASSIGN:
            case AssemblerConstants.LONG_ASSIGN:
            case AssemblerConstants.QUAD_ASSIGN:
            case AssemblerConstants.ASCII_ASSIGN:
            case AssemblerConstants.FILL_ASSIGN:
            case AssemblerConstants.COMM_ASSIGN:
            case AssemblerConstants.DRIVER: return "token.directive";
            case AssemblerConstants.CONSTANT: return "token.immediate";
            case AssemblerConstants.INTEGER:
            case AssemblerConstants.DEC:
            case AssemblerConstants.HEX:
            case AssemblerConstants.BIN:
            case AssemblerConstants.FLONUM: return "token.number";
            case AssemblerConstants.ASSIGN: return "\"=\"";
            case AssemblerConstants.PLUS: return "\"+\"";
            case AssemblerConstants.MINUS: return "\"-\"";
            case AssemblerConstants.TIMES: return "\"*\"";
            case AssemblerConstants.DIVIDE: return "\"/\"";
            case AssemblerConstants.LBRACE: return "\"(\"";
            case AssemblerConstants.RBRACE: return "\")\"";
            case AssemblerConstants.COMMA: return "\",\"";
            case AssemblerConstants.REG_8:
            case AssemblerConstants.REG_16:
            case AssemblerConstants.REG_32:
            case AssemblerConstants.REG_64: return "token.register";
            case AssemblerConstants.INSN_0:
            case AssemblerConstants.INSN_0_WQ:
            case AssemblerConstants.INSN_0_NOSUFF:
            case AssemblerConstants.INSN_1_S:
            case AssemblerConstants.INSN_LEA:
            case AssemblerConstants.INSN_1_B:
            case AssemblerConstants.INSN_SHIFT:
            case AssemblerConstants.INSN_BT:
            case AssemblerConstants.INSN_1_M:
            case AssemblerConstants.INSN_JC:
            case AssemblerConstants.INSN_B_E:
            case AssemblerConstants.INSN_EXT:
            case AssemblerConstants.INSN_IN:
            case AssemblerConstants.INSN_OUT:
            case AssemblerConstants.INSN_IO_S:
            case AssemblerConstants.IRET: return "token.instruction";
            case AssemblerConstants.LABEL: return "token.label";
            case AssemblerConstants.LABEL_NAME: return "token.identifier";
            case AssemblerConstants.STRING_LITERAL: return "token.string";
            case AssemblerConstants.EOF: return "token.eof";
            default: return null;
        }
    }
}
