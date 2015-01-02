/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.z64sim.assembler.JavaCharStream;
import org.z64sim.assembler.AssemblerTokenManager;
import org.z64sim.assembler.Token;
import org.z64sim.assembler.TokenMgrError;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
class z64Lexer implements Lexer<z64TokenId> {

    private final LexerRestartInfo<z64TokenId> info;
    private final AssemblerTokenManager assemblerTokenManager;
    private static final Logger logger = Logger.getLogger(Lexer.class.getName());

    z64Lexer(LexerRestartInfo<z64TokenId> info) {
        this.info = info;
        JavaCharStream stream = new JavaCharStream(new LexerInputReader(info.input()));
        assemblerTokenManager = new AssemblerTokenManager(stream);
    }

    @Override
    public org.netbeans.api.lexer.Token<z64TokenId> nextToken() {
        Token token;

        try {
            token = assemblerTokenManager.getNextToken();
        } catch (TokenMgrError e) {
            logger.log(Level.INFO, e.getMessage());
            return null;
        }

        if (null == token || token.kind == AssemblerTokenManager.EOF) {
            return null;
        }
        
        return info.tokenFactory().createToken(z64LanguageHierarchy.getToken(token.kind), token.image.length());
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }

}
