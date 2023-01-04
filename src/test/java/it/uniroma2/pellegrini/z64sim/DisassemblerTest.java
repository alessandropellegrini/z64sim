/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DisassemblerTest {

    private void assembleAndDisassemble(String testProgram) throws ParseException, IOException {
        InputStream is = getClass().getResourceAsStream(testProgram);
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        Program p = a.getProgram();

        // Disassemble the assembled program
        System.out.println("Disassembled program [" + testProgram + "]: ");
        long rip = p.text.getTarget();
        while(true) {
            MemoryElement me = p.getMemoryElementAt(rip);
            if(me == null) {
                break;
            }

            if(me instanceof Instruction) {
                String disassembled = me.toString();
                System.out.println("0x" + Long.toHexString(rip) + ": " + disassembled);
            }

            rip += me.getSize();
        }
    }

    @Test
    @DisplayName("Testing Disassembler")
    public void testProgram() {
        assertDoesNotThrow(() -> assembleAndDisassemble("/test.asm"));
        assertDoesNotThrow(() -> assembleAndDisassemble("/relocations.asm"));
        assertDoesNotThrow(() -> assembleAndDisassemble("/isa.asm"));
    }
}
