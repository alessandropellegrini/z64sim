/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.operands;

import it.uniroma2.pellegrini.z64sim.isa.registers.Register;

public class OperandRegister extends Operand {

    private final int register;

    public OperandRegister(int register, int size) {
        super(size);
        this.register = register;
    }

    public int getRegister() {
        return register;
    }

    @Override
    public String toString() {
        return Register.getRegisterName(this.register, this.size);
    }
}
