/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.operands;

import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public abstract class Operand {

    protected int size;

    public Operand(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    // toString() must be explicitly re-implemented
    public abstract String toString();

    public String getSizeSuffix() throws DisassembleException {
        switch(this.size) {
            case 1:
                return "b";
            case 2:
                return "w";
            case 4:
                return "l";
            case 8:
                return "q";
        }
        throw new DisassembleException("Unable to determine instruction suffix.");
    }
}
