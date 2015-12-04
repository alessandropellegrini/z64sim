/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

public class OperandImmediate extends Operand {

    private long value;

    public OperandImmediate(long value) {
        super(8); // An immediate is always 8 bytes long
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
    public void relocate(long value) {
        this.value += value;
    }
}
