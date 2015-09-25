/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

public class OperandRegister extends Operand {
    
    private final int register;
    private final int size;

    public OperandRegister(int register, int size) {
        this.register = register;
        this.size = size;
    }

    public int getRegister() {
        return register;
    }

    public int getSize() {
        return size;
    }
}
