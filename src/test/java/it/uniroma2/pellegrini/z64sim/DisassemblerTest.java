package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.ProgramException;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class DisassemblerTest {
    @Test
    @DisplayName("Disassembler test")
    public void testProgram() throws ParseException, ProgramException {
        InputStream is = getClass().getResourceAsStream("/test.asm");
        InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is));
        Assembler a = new Assembler(isr);
        a.Program();
        Program p = a.getProgram();

        int current = 0x808;
        p[current];
    }
}
