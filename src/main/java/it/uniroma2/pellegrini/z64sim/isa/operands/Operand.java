/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.operands;

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
//    public abstract String toString();
}
