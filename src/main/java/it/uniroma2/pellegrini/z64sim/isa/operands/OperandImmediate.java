/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.operands;

import it.uniroma2.pellegrini.z64sim.model.MemoryPointer;

public class OperandImmediate extends Operand {

    private long value;

    public OperandImmediate(long value) {
        super(32);
        // An immediate is 32-bit long, except when it cannot be represented using 32 bits
        if(value > Integer.MAX_VALUE) {
            this.setSize(64);
        }

        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "$" + this.value;
    }

    // This call actually sums the value of the label. This is because we could
    // write an instruction such as "movq $constant+10". The +10 is stored in the
    // 'value' field of the object, and we have then to sum $constant.
    public void relocate(MemoryPointer value) {
        this.value += value.getTarget();
    }
}
