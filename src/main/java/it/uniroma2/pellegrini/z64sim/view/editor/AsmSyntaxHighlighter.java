/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.editor;

import it.uniroma2.pellegrini.z64sim.assembler.AssemblerConstants;
import it.uniroma2.pellegrini.z64sim.assembler.AssemblerTokenManager;
import it.uniroma2.pellegrini.z64sim.assembler.JavaCharStream;
import it.uniroma2.pellegrini.z64sim.assembler.Token;
import it.uniroma2.pellegrini.z64sim.assembler.TokenMgrError;

import javax.swing.text.*;
import java.awt.*;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Syntax highlighter for z64 assembly code. Uses the JavaCC-generated
 * {@link AssemblerTokenManager} to tokenize source text and applies
 * colored character attributes to a {@link StyledDocument}.
 * <p>
 * Supports both light and dark color schemes, switchable at runtime.
 */
public class AsmSyntaxHighlighter {

    // ---- Color palettes ----

    // Light theme colors
    private static final Color LIGHT_INSTRUCTION = new Color(0x00, 0x33, 0xB3);
    private static final Color LIGHT_REGISTER    = new Color(0x00, 0x80, 0x80);
    private static final Color LIGHT_DIRECTIVE   = new Color(0x7B, 0x1F, 0xA2);
    private static final Color LIGHT_NUMBER      = new Color(0x17, 0x50, 0xEB);
    private static final Color LIGHT_COMMENT     = new Color(0x8C, 0x8C, 0x8C);
    private static final Color LIGHT_LABEL       = new Color(0x87, 0x10, 0x94);
    private static final Color LIGHT_STRING      = new Color(0x06, 0x7D, 0x17);
    private static final Color LIGHT_ERROR       = new Color(0xFF, 0x00, 0x00);

    // Dark theme colors
    private static final Color DARK_INSTRUCTION  = new Color(0x68, 0x97, 0xBB);
    private static final Color DARK_REGISTER     = new Color(0x98, 0x76, 0xAA);
    private static final Color DARK_DIRECTIVE    = new Color(0xB3, 0x89, 0xD9);
    private static final Color DARK_NUMBER       = new Color(0x68, 0x97, 0xBB);
    private static final Color DARK_COMMENT      = new Color(0x80, 0x80, 0x80);
    private static final Color DARK_LABEL        = new Color(0xFF, 0xC6, 0x6D);
    private static final Color DARK_STRING       = new Color(0x6A, 0x87, 0x59);
    private static final Color DARK_ERROR        = new Color(0xFF, 0x6B, 0x68);

    private boolean darkTheme = false;

    /** Style cache: token kind → AttributeSet. Rebuilt when theme changes. */
    private final Map<Integer, AttributeSet> styleMap = new HashMap<>();

    /** Style used for comment special tokens. */
    private AttributeSet commentStyle;

    /** Default style (plain foreground, no special formatting). */
    private AttributeSet defaultStyle;

    public AsmSyntaxHighlighter() {
        buildStyles();
    }

    /**
     * Switch between light and dark color palettes.
     * After calling this, the caller should re-highlight the document.
     */
    public void setDarkTheme(boolean dark) {
        if (this.darkTheme != dark) {
            this.darkTheme = dark;
            buildStyles();
        }
    }

    /**
     * (Re)build the style map for the current theme.
     */
    private void buildStyles() {
        styleMap.clear();

        Color cInstruction = darkTheme ? DARK_INSTRUCTION : LIGHT_INSTRUCTION;
        Color cRegister    = darkTheme ? DARK_REGISTER    : LIGHT_REGISTER;
        Color cDirective   = darkTheme ? DARK_DIRECTIVE   : LIGHT_DIRECTIVE;
        Color cNumber      = darkTheme ? DARK_NUMBER      : LIGHT_NUMBER;
        Color cComment     = darkTheme ? DARK_COMMENT     : LIGHT_COMMENT;
        Color cLabel       = darkTheme ? DARK_LABEL       : LIGHT_LABEL;
        Color cString      = darkTheme ? DARK_STRING      : LIGHT_STRING;
        Color cError       = darkTheme ? DARK_ERROR       : LIGHT_ERROR;

        // Instructions (all INSN_* variants + IRET)
        AttributeSet instrStyle = createStyle(cInstruction, true, false);
        styleMap.put(AssemblerConstants.INSN_0, instrStyle);
        styleMap.put(AssemblerConstants.INSN_0_WQ, instrStyle);
        styleMap.put(AssemblerConstants.INSN_0_NOSUFF, instrStyle);
        styleMap.put(AssemblerConstants.INSN_1_S, instrStyle);
        styleMap.put(AssemblerConstants.INSN_LEA, instrStyle);
        styleMap.put(AssemblerConstants.INSN_1_B, instrStyle);
        styleMap.put(AssemblerConstants.INSN_SHIFT, instrStyle);
        styleMap.put(AssemblerConstants.INSN_BT, instrStyle);
        styleMap.put(AssemblerConstants.INSN_1_M, instrStyle);
        styleMap.put(AssemblerConstants.INSN_JC, instrStyle);
        styleMap.put(AssemblerConstants.INSN_B_E, instrStyle);
        styleMap.put(AssemblerConstants.INSN_EXT, instrStyle);
        styleMap.put(AssemblerConstants.INSN_IN, instrStyle);
        styleMap.put(AssemblerConstants.INSN_OUT, instrStyle);
        styleMap.put(AssemblerConstants.INSN_IO_S, instrStyle);
        styleMap.put(AssemblerConstants.IRET, instrStyle);

        // Registers
        AttributeSet regStyle = createStyle(cRegister, false, false);
        styleMap.put(AssemblerConstants.REG_8, regStyle);
        styleMap.put(AssemblerConstants.REG_16, regStyle);
        styleMap.put(AssemblerConstants.REG_32, regStyle);
        styleMap.put(AssemblerConstants.REG_64, regStyle);

        // Directives
        AttributeSet dirStyle = createStyle(cDirective, true, false);
        styleMap.put(AssemblerConstants.ORG, dirStyle);
        styleMap.put(AssemblerConstants.DATA_SECTION, dirStyle);
        styleMap.put(AssemblerConstants.CODE_SECTION, dirStyle);
        styleMap.put(AssemblerConstants.BSS_SECTION, dirStyle);
        styleMap.put(AssemblerConstants.EQU_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.BYTE_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.WORD_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.LONG_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.QUAD_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.ASCII_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.FILL_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.COMM_ASSIGN, dirStyle);
        styleMap.put(AssemblerConstants.DRIVER, dirStyle);

        // Numbers
        AttributeSet numStyle = createStyle(cNumber, false, false);
        styleMap.put(AssemblerConstants.INTEGER, numStyle);
        styleMap.put(AssemblerConstants.FLONUM, numStyle);
        // DEC, HEX, BIN are internal helper tokens used by INTEGER — unlikely to appear
        // standalone, but map them just in case
        styleMap.put(AssemblerConstants.DEC, numStyle);
        styleMap.put(AssemblerConstants.HEX, numStyle);
        styleMap.put(AssemblerConstants.BIN, numStyle);

        // Labels
        styleMap.put(AssemblerConstants.LABEL, createStyle(cLabel, false, false));

        // String literals
        styleMap.put(AssemblerConstants.STRING_LITERAL, createStyle(cString, false, false));

        // Constant prefix ($)
        styleMap.put(AssemblerConstants.CONSTANT, createStyle(cNumber, false, false));

        // Error token
        styleMap.put(AssemblerConstants.ERROR, createStyle(cError, false, false));

        // Comment style (used for SPECIAL_TOKEN chain)
        commentStyle = createStyle(cComment, false, true);

        // Default style (operators, punctuation, newlines, etc.)
        defaultStyle = new SimpleAttributeSet();
    }

    /**
     * Create an {@link AttributeSet} with the given foreground color and style flags.
     */
    private static AttributeSet createStyle(Color foreground, boolean bold, boolean italic) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, foreground);
        if (bold) StyleConstants.setBold(attrs, true);
        if (italic) StyleConstants.setItalic(attrs, true);
        return attrs;
    }

    /**
     * Tokenize the given source text and apply syntax highlighting styles
     * to the provided {@link StyledDocument}.
     * <p>
     * This method re-highlights the entire document from scratch.
     * It must be called on the EDT.
     *
     * @param doc the styled document to apply highlighting to
     */
    public void highlight(StyledDocument doc) {
        String text;
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return; // should not happen
        }

        if (text.isEmpty()) {
            return;
        }

        // Reset all character attributes to default
        doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);

        try {
            JavaCharStream charStream = new JavaCharStream(new StringReader(text));
            AssemblerTokenManager tokenManager = new AssemblerTokenManager(charStream);

            Token token;
            while (true) {
                try {
                    token = tokenManager.getNextToken();
                } catch (TokenMgrError e) {
                    // Malformed input — stop highlighting but don't crash
                    break;
                }

                if (token.kind == AssemblerConstants.EOF) {
                    // Process any special tokens (comments) attached to the EOF token
                    highlightSpecialTokens(token, doc, text);
                    break;
                }

                // Highlight special tokens (comments/whitespace) attached to this regular token
                highlightSpecialTokens(token, doc, text);

                // Highlight the regular token itself
                AttributeSet style = styleMap.get(token.kind);
                if (style != null) {
                    int startOffset = lineColToOffset(text, token.beginLine, token.beginColumn);
                    int endOffset = lineColToOffset(text, token.endLine, token.endColumn) + 1;
                    if (startOffset >= 0 && endOffset > startOffset && endOffset <= doc.getLength()) {
                        doc.setCharacterAttributes(startOffset, endOffset - startOffset, style, true);
                    }
                }
            }
        } catch (Exception e) {
            // Fail silently — partial highlighting is better than crashing
        }
    }

    /**
     * Walk the specialToken linked list attached to a regular token and highlight
     * any comments found.
     */
    private void highlightSpecialTokens(Token regularToken, StyledDocument doc, String text) {
        // The specialToken chain is: regularToken.specialToken → previous special → ...
        // We need to walk backwards, but we only care about comments (kind == COMMENT).
        Token special = regularToken.specialToken;
        while (special != null) {
            if (special.kind == AssemblerConstants.COMMENT) {
                int startOffset = lineColToOffset(text, special.beginLine, special.beginColumn);
                int endOffset = lineColToOffset(text, special.endLine, special.endColumn) + 1;
                if (startOffset >= 0 && endOffset > startOffset && endOffset <= doc.getLength()) {
                    doc.setCharacterAttributes(startOffset, endOffset - startOffset, commentStyle, true);
                }
            }
            special = special.specialToken;
        }
    }

    /**
     * Convert a 1-based line and column to a 0-based character offset in the text.
     * Returns -1 if the position is out of bounds.
     */
    private static int lineColToOffset(String text, int line, int column) {
        int currentLine = 1;
        int offset = 0;
        while (currentLine < line && offset < text.length()) {
            if (text.charAt(offset) == '\n') {
                currentLine++;
            }
            offset++;
        }
        if (currentLine != line) {
            return -1;
        }
        // column is 1-based
        return offset + column - 1;
    }
}
