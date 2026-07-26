/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.AssemblerConstants;
import it.uniroma2.pellegrini.z64sim.assembler.ParseErrorFormatter;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.assembler.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ParseErrorFormatter}.
 */
public class ParseErrorFormatterTest {

    // =====================================================================
    // Helper methods
    // =====================================================================

    private static Token makeToken(String image, int line) {
        Token t = new Token();
        t.image = image;
        t.beginLine = line;
        return t;
    }

    /**
     * Build a structured ParseException as JavaCC would produce.
     */
    private static ParseException makeStructured(String foundImage, int line, int[][] expectedSeqs) {
        Token errorToken = makeToken(foundImage, line);
        Token prevToken = makeToken("", 0);
        prevToken.next = errorToken;
        return new ParseException(prevToken, expectedSeqs, AssemblerConstants.tokenImage);
    }

    // =====================================================================
    // Tests
    // =====================================================================

    @Test
    @DisplayName("Returns raw message when currentToken is null")
    public void testNullCurrentToken() {
        ParseException ex = new ParseException("Custom error message");
        String result = ParseErrorFormatter.format(ex);
        assertEquals("Custom error message", result);
    }

    @Test
    @DisplayName("Single expected token category")
    public void testSingleExpected() {
        int[][] expected = {{AssemblerConstants.INSN_0}};
        ParseException ex = makeStructured("sbraga", 5, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("5"), "Should contain line number");
        assertTrue(result.contains("sbraga"), "Should contain the unexpected token");
        assertTrue(result.contains("instruction"), "Should contain 'instruction'");
    }

    @Test
    @DisplayName("Multiple expected token categories are deduplicated and joined")
    public void testMultipleExpected() {
        int[][] expected = {
            {AssemblerConstants.INSN_0},
            {AssemblerConstants.INSN_0_WQ},
            {AssemblerConstants.LABEL},
            {AssemblerConstants.REG_64}
        };
        ParseException ex = makeStructured("xyz", 10, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("10"), "Should contain line number");
        assertTrue(result.contains("xyz"), "Should contain the unexpected token");
        assertTrue(result.contains("instruction"), "Should contain 'instruction'");
        assertTrue(result.contains("label"), "Should contain 'label'");
        assertTrue(result.contains("register"), "Should contain 'register'");
        assertTrue(result.contains(", or "), "Should use ', or ' before the last category");
    }

    @Test
    @DisplayName("Punctuation tokens are returned as literals")
    public void testPunctuationExpected() {
        int[][] expected = {{AssemblerConstants.COMMA}};
        ParseException ex = makeStructured("xyz", 3, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("\",\""), "Should contain literal comma token");
    }

    @Test
    @DisplayName("Empty expected sequences produce fallback message")
    public void testEmptyExpected() {
        int[][] expected = {};
        ParseException ex = makeStructured("bad", 7, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("7"), "Should contain line number");
        assertTrue(result.contains("bad"), "Should contain the unexpected token");
    }

    @Test
    @DisplayName("NEWLINE token is suppressed from expected list")
    public void testNewlineSuppressed() {
        int[][] expected = {{AssemblerConstants.NEWLINE}};
        ParseException ex = makeStructured("foo", 2, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("2"), "Should contain line number");
        assertTrue(result.contains("foo"), "Should contain the unexpected token");
    }

    @Test
    @DisplayName("EOF expected token")
    public void testEofExpected() {
        int[][] expected = {{AssemblerConstants.EOF}};
        ParseException ex = makeStructured("junk", 99, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("99"), "Should contain line number");
        assertTrue(result.contains("end of file"), "Should contain 'end of file'");
    }

    @Test
    @DisplayName("Directive token categories are deduplicated")
    public void testDirectiveDedup() {
        int[][] expected = {
            {AssemblerConstants.ORG},
            {AssemblerConstants.DATA_SECTION},
            {AssemblerConstants.CODE_SECTION},
            {AssemblerConstants.BSS_SECTION}
        };
        ParseException ex = makeStructured("x", 1, expected);
        String result = ParseErrorFormatter.format(ex);

        // All map to "directive" — count occurrences
        int count = 0;
        int idx = 0;
        while ((idx = result.indexOf("directive", idx)) != -1) {
            count++;
            idx++;
        }
        assertEquals(1, count, "Directive should appear exactly once (deduplicated)");
    }

    @Test
    @DisplayName("Two expected categories use ', or ' separator")
    public void testTwoCategoriesJoining() {
        int[][] expected = {
            {AssemblerConstants.LABEL},
            {AssemblerConstants.CONSTANT}
        };
        ParseException ex = makeStructured("$", 42, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("label"), "Should contain 'label'");
        assertTrue(result.contains("immediate"), "Should contain 'immediate'");
        assertTrue(result.contains(", or "), "Two items should be joined with ', or '");
    }

    @Test
    @DisplayName("Mixed token kinds: register, number, and punctuation")
    public void testMixedTokenKinds() {
        int[][] expected = {
            {AssemblerConstants.REG_64},
            {AssemblerConstants.INTEGER},
            {AssemblerConstants.LBRACE}
        };
        ParseException ex = makeStructured("??", 8, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("register"), "Should contain 'register'");
        assertTrue(result.contains("number"), "Should contain 'number'");
        assertTrue(result.contains("\"(\""), "Should contain literal paren");
    }

    @Test
    @DisplayName("Sequences with length > 1 use only the first token")
    public void testMultiTokenSequence() {
        int[][] expected = {{AssemblerConstants.INSN_0, AssemblerConstants.REG_64}};
        ParseException ex = makeStructured("oops", 15, expected);
        String result = ParseErrorFormatter.format(ex);

        assertTrue(result.contains("instruction"), "Should contain 'instruction'");
    }
}
