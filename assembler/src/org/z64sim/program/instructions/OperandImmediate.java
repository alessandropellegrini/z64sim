/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;


public class OperandImmediate extends Operand {
    
    private final long value;

    public OperandImmediate(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }   
}
