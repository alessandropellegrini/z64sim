/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.model.Program;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class SimulatorController extends Controller {
    private static SimulatorController instance;
    private Program program;

    private SimulatorController() {}

    public static void init() {
        instance = new SimulatorController();
    }

    @Override
    public boolean dispatch(Events command) {
        switch(command) {
            case ASSEMBLE_PROGRAM:
                String code = MainWindow.getCode();

                Assembler a = new Assembler(code);
                try {
                    a.Program();
                } catch(ParseException ignored) {}

                List<String> syntaxErrors = new ArrayList<>(a.getSyntaxErrors());

                StringBuilder assemblerOutput = new StringBuilder();
                if(syntaxErrors.isEmpty()) {
                    assemblerOutput.append("Assembly successful.");
                } else {
                    assemblerOutput.append("Assembly failed with ").append(syntaxErrors.size()).append(" errors:\n\n");
                    for(String e : syntaxErrors) {
                        assemblerOutput.append(e).append("\n");
                    }
                }

                MainWindow.compileResult(assemblerOutput.toString());
                program = a.getProgram();
        }
        return false;
    }
}
