/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;


/**
 *
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory {

    //private static Program program = null;
    private static Program program = new Program(); // aggiunto per evitare nullPointerException

    // This class cannot be instantiated
    private Memory() {
    }

    public static Program getProgram() {
        return program;
    }

    public static void setProgram(Program program) {
        Memory.program = program;
    }

}
