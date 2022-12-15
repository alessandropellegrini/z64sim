/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


/**
 *
 * @author pellegrini
 */

public class AssemblerTest {

    private void assemble(String testProgram) throws ParseException {
        InputStream is = getClass().getResourceAsStream(testProgram);
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        a.getSyntaxErrors().forEach(System.out::println);
        assert(a.getSyntaxErrors().isEmpty());
    }

    @Test
    @DisplayName("Testing Assembler")
    public void testAssembler() throws ParseException {
        assertDoesNotThrow(() -> assemble("/test.asm"));
        assertDoesNotThrow(() -> assemble("/relocations.asm"));
        assertDoesNotThrow(() -> assemble("/isa.asm"));
    }

}
