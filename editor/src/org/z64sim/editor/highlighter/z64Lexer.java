/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.z64sim.assembler.JavaCharStream;
import org.z64sim.assembler.AssemblerTokenManager;
import org.z64sim.assembler.Token;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
class z64Lexer implements Lexer<z64TokenId> {

    private final LexerRestartInfo<z64TokenId> info;
    private final AssemblerTokenManager assemblerTokenManager;

    z64Lexer(LexerRestartInfo<z64TokenId> info) {
        this.info = info;
        JavaCharStream stream = new JavaCharStream(new LexerInputReader(info.input()));
        assemblerTokenManager = new AssemblerTokenManager(stream);
    }

    @Override
    public org.netbeans.api.lexer.Token<z64TokenId> nextToken() {
        Token token = assemblerTokenManager.getNextToken();
        if (info.input().readLength() < 1) {
            return null;
        }
        return info.tokenFactory().createToken(z64LanguageHierarchy.getToken(token.kind));
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }

}
