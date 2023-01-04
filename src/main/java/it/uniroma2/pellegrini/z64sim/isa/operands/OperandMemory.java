/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.operands;


import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.MemoryPointer;

public class OperandMemory extends Operand {

    // TODO: map -1 to nulls
    private int base = -1;
    private int scale = -1;
    private int index = -1;
    private MemoryPointer displacement = null;
    private int base_size = -1;

    public OperandMemory(int base, int base_size, int index, int scale, Integer displacement, int size) {
        super(size);

        this.base = base;
        this.base_size = base_size;
        this.index = index;
        this.scale = scale;
        this.displacement = new MemoryPointer(displacement);
    }

    public Integer getDisplacement() {
        if(this.displacement == null)
            return -1;
        return (int)displacement.getTarget();
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

    public void setDisplacement(Integer displacement) {
        this.displacement = new MemoryPointer(displacement);
    }

    public String toString() {
        String representation = "";

        if(this.displacement != null && this.displacement.getTarget() != 0) {
            representation = representation.concat(String.format("%#x", this.displacement.getTarget()));
        }

        if(this.base != -1 || this.index != -1) {
            representation = representation.concat("(");
        }

        if(this.base != -1) {
            representation = representation.concat(Register.getRegisterName(this.base, this.base_size));
        }

        if(this.index != -1) {
            representation = representation.concat(", " + Register.getRegisterName(this.index, 8));
            representation = representation.concat(", " + this.scale);
        }

        if(this.base != -1 || this.index != -1) {
            representation = representation.concat(")");
        }

        return representation;
    }

    public void relocate(MemoryPointer value) {
        this.displacement.setTarget(this.displacement.getTarget() + value.getTarget());
    }
}
