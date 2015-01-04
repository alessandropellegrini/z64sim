/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;


public class OperandMemory extends Operand {
    
    private final long address;
    
    public OperandMemory(long address) {
        this.address = address;
    }
    
}
