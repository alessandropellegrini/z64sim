/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.operands;


import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;

public class OperandMemory extends Operand {

    private int base = -1;
    private int scale = -1;
    private int index = -1;
    private MemoryTarget displacement = null;

    // In z64 assembly you can say both (%ax) or (%rax) for example, so we must
    // account fot the size of the base register as well
    // On the other hand, the index is always a 64-bit register
    private int base_size = -1;

    public OperandMemory(int base, int base_size, int index, int scale, long displacement, int size) {
        super(size);

        this.base = base;
        this.base_size = base_size;
        this.index = index;
        this.scale = scale;
        this.displacement = new MemoryTarget(displacement);
    }

    public Long getDisplacement() {
        if(this.displacement == null)
            return -1L;
        return displacement.getDisplacement();
    }

    public int getScale() {
        return scale;
    }

    public int getIndex() {
        return index;
    }

    public long getBase() {
        return base;
    }

    public int getBaseSize() {
        return base_size;
    }

    public void setDisplacement(long displacement) {
        this.displacement = new MemoryTarget(displacement);
    }

    public String toString() {
        String representation = "";

        if(this.displacement != null) {
            if(!(this.displacement instanceof Instruction))
                representation = representation.concat(String.format("$%x", this.displacement.getDisplacement()));
        }

        if(this.base != -1 || this.index != -1) {
            representation = representation.concat("(");
        }

        if(this.base != -1) {
            representation = representation.concat(Register.getRegisterName(this.base, this.base_size));
        }

        if(this.index != -1) {
            representation = representation.concat(", " + Register.getRegisterName(this.index, 64));
            representation = representation.concat(", " + this.scale);
        }

        if(this.base != -1 || this.index != -1) {
            representation = representation.concat(")");
        }

        return representation;
    }

    public void relocate(MemoryTarget value) {
        this.displacement.setDisplacement(this.displacement.getDisplacement() + value.getDisplacement());
    }
}
