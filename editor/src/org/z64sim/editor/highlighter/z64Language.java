/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.editor.highlighter;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
@LanguageRegistration(mimeType = "text/z64asm")
public class z64Language extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return z64TokenId.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "z64 Assembly";
    }
    
    @Override
    public Parser getParser() {
        return new z64Parser();
    }

}
