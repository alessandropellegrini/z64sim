/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.*;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Objects;


/**
 *
 * @author pellegrini
 */

public class AssemblerTest {
    @Test
    @DisplayName("Lexer test")
    public void testLexer() throws IOException {
        File f = new File(Objects.requireNonNull(this.getClass().getResource("/test.asm")).getFile());
        InputStreamReader isr = new FileReader(Objects.requireNonNull(f));
        JavaCharStream stream = new JavaCharStream(isr);
        AssemblerTokenManager manager = new AssemblerTokenManager(stream);
        Token token = manager.getNextToken();

        int i = 1;
        while (token != null && token.kind != AssemblerConstants.EOF) {
            token = manager.getNextToken();
            i++;
        }

        System.out.println("tokens: " + i);
        assert(i == 145);
    }

    @Test
    @DisplayName("Parser test")
    public void testParser() throws ParseException {
        InputStream is = getClass().getResourceAsStream("/test.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
    }

    @Test
    @DisplayName("Relocations")
    public void testRelocations() throws ParseException {
        InputStream is = getClass().getResourceAsStream("/relocations.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        Program p = a.getProgram();
        assert(p != null);
    }
}
