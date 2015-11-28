/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import java.io.IOException;
import java.io.Reader;
import org.netbeans.spi.lexer.LexerInput;

/**
 * A <code>java.io.Reader</code> which retreives data from a
 * <code>LexerInput</code> instance. This class may be useful to somebody that
 * uses a lexer generated from JFlex, ANTLR, etc., that read their input from a
 * <code>java.io.Reader</code>.
 *
 * One key feature of this implementation is that the <code>read(char[])</code>
 * and <code>read(char[], int, int)</code> methods will only return one
 * character at a time (at most), regardless of the number of characters
 * requested. This is important for performance reasons when doing lexical
 * analysis in the editor. Please see the documentation of {@link #read(char[], int, int)
 * } for more information about this.
 *
 * Note that {@link #mark} and {@link #reset} are not supported.
 *
 * https://netbeans.org/bugzilla/show_bug.cgi?id=88968
 *
 * @see java.io.Reader
 * @see org.netbeans.spi.lexer.LexerInput
 *
 * @author Brian Smith
 */
public class LexerInputReader extends Reader {

    LexerInput lexerInput;

    public LexerInputReader(LexerInput lexerInput) {
        if (lexerInput == null) {
            throw new IllegalArgumentException("Lexer input cannot be null");
        }

        this.lexerInput = lexerInput;
    }

    /**
     * Reads one character from the lexer input.
     *
     * @return <code>-1</code> if the lexer input is at the end of the file;
     * <code>0</code> otherwise.
     */
    @Override
    public int read() {
        return lexerInput.read();
    }

    /**
     * Synonym for <code>read(cbuf, 0, cbuf.length)</code>.
     *
     * @see #read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf) {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * Attempts to read up to a single character and put it in the buffer.
     *
     * If a character was successfully read, then it is placed at
     * <code>cbuf[off]</code>. Note that this method will never read more than
     * one character at a time, even if the <code>len</code> parameter is
     * greater than one.
     *
     * Lexical analysis frameworks (e.g.
     * <a href="http://www.jflex.de/">JFlex</a>) usually use this method to read
     * a large block of characters into a buffer, in an attempt to improve
     * performance in the usual case where the input is coming from a file.
     * However, this kind of buffering actually hurts performance of the
     * NetBeans Lexer module's incremental lexer, according to the the
     * &ldquo;Lookahead&rdquo; section of the
     * <a href="http://lexer.netbeans.org/doc/batch-lexer-requirements.html">Batch
     * Lexer Requirements</a> document. By only providing at most one character
     * per call, we can effectively disable this attempted optimization without
     * making changes to the buffering code used by the lexical analyzer.
     *
     * However, note that some lexer frameworks still need some tweaking to
     * allow them to deal well with receiving one character at a time. For
     * example, by default JFlex 1.4.1 will move around a lot of data in its
     * buffer order to make room for the data it reads using this method. If you
     * do not disable this moving around of data, then performance is likely to
     * suffer significantly.
     *
     * @param cbuf The buffer that the read character (if any) should be placed
     * into.
     * @param off The offset into the buffer to place the read character.
     * @param len If <code>len</code> is less than one, then
     * @return <code>1</code> if a character was successfully placed into the
     * buffer; <code>0</code> if the <code>len</code> parameter was less than
     * one; <code>-1</code> if the end of the input was reached before a
     * character could be read.
     */
    // TODO: Add a link to the lexer documentation that talks about
    //       limiting lookahead and buffering.
    @Override
    public int read(char[] cbuf, int off, int len) {
        if (len <= 0) {
            return 0;
        }

        int nextChar = read();
        if (nextChar == LexerInput.EOF) {
            return -1;
        } else {
            cbuf[off] = (char) nextChar;
            return 1;
        }
    }

    @Override
    public long skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < n; ++i) {
            int nextChar = read();
            if (nextChar == LexerInput.EOF) {
                return i;
            }
        }
        return n;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean ready() {
        // TODO: LexerInput will never block, will it?
        return true;
    }

    /**
     * Always returns <code>false</code> to indicate that <code>mark</code> and
     * <code>reset</code> are not supported.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Always throws an <code>IOException</code>; <code>mark</code> and
     * <code>reset</code> are not supported.
     */
    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new IOException("mark() not supported");
    }

    /**
     * Always throws an <code>IOException</code>; <code>mark</code> and
     * <code>reset</code> are not supported.
     */
    @Override
    public void reset() throws IOException {
        throw new IOException("reset() not supported");
    }

}
