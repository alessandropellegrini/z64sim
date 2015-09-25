/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;


public class OperandMemory extends Operand {
    
    private int base = -1;
    private int scale = -1;
    private int index = -1 ;
    private int displacement = -1;
    
    // In z64 assembly you can say both (%ax) or (%rax) for example, so we must
    // account fot the size of the base and index registers as well
    private int base_size = -1;
    private int index_size = -1;

    public OperandMemory(int base, int base_size, int index, int index_size, int scale, int displacement) {
        this.base = base;
        this.base_size = base_size;
        this.index = index;
        this.index_size = index_size;
        this.scale = scale;
        this.displacement = displacement;
    }
    
    public int getDisplacement() {
        return displacement;
    }
    
    public int getScale() {
        return scale;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getIndexSize() {
        return index_size;
    }
    
    public long getBase() {
        return base;
    }
    
    public int getBaseSize() {
        return base_size;
    }
}
