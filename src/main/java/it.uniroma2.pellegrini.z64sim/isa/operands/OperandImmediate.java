package it.uniroma2.pellegrini.z64sim.isa.operands;

public class OperandImmediate extends Operand {

    private long value;

    public OperandImmediate(long value) {
        super(32);
        // Basically an immediate is 32-bit long, except when it cannot
        // be represented using 32 bits (!!!)
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
    public void relocate(long value) {
        this.value += value;
    }
}
