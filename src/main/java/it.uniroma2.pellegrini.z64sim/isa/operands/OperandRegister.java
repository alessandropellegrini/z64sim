package it.uniroma2.pellegrini.z64sim.isa.operands;

public class OperandRegister extends Operand {

    private final int register;

    public OperandRegister(int register, int size) {
        super(size);
        this.register = register;
    }

    public int getRegister() {
        return register;
    }

//    @Override
//    public String toString() {
//        return Register.getRegisterName(this.register, this.size);
//    }
}
