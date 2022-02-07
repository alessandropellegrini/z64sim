package it.uniroma2.pellegrini.z64sim.isa.operands;


import it.uniroma2.pellegrini.z64sim.isa.registers.Register;

public class OperandMemory extends Operand {

    private int base = -1;
    private int scale = -1;
    private int index = -1;
    private int displacement = -1;

    // In z64 assembly you can say both (%ax) or (%rax) for example, so we must
    // account fot the size of the base register as well
    // On the other hand, the index is always a 64-bit register
    private int base_size = -1;

    public OperandMemory(int base, int base_size, int index, int scale, int displacement, int size) {
        super(size);

        this.base = base;
        this.base_size = base_size;
        this.index = index;
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

    public long getBase() {
        return base;
    }

    public int getBaseSize() {
        return base_size;
    }

    public void setDisplacement(int displacement) {
        this.displacement = displacement;
    }

    public String toString() {
        String representation = "";

        if(this.displacement != -1) {
            representation = representation.concat(String.format("$%x", this.displacement));
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

    public void relocate(long value) {
        this.displacement += value;
    }
}
