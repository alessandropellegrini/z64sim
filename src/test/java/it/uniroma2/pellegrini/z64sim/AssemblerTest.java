/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.*;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass1;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;


/**
 *
 * @author pellegrini
 */

public class AssemblerTest {
    private static final Logger log = LoggerFactory.getLogger(AssemblerTest.class);

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUpMethod() {
    }

    @AfterEach
    public void tearDownMethod() {
    }

    @Test
    @DisplayName("Lexer test")
    public void testLexer() {
        InputStream is = getClass().getResourceAsStream("/test.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        JavaCharStream stream = new JavaCharStream(isr);
        AssemblerTokenManager manager = new AssemblerTokenManager(stream);
        Token token = manager.getNextToken();

        while (token != null && token.kind != AssemblerConstants.EOF) {
            log.debug("Next token: {} [{}]", token, token.kind);
            token = manager.getNextToken();
        }
    }

    @Test
    @Disabled
    @DisplayName("Parser test")
    public void testParser() throws ParseException {
        InputStream is = getClass().getResourceAsStream("/test.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
    }
}
