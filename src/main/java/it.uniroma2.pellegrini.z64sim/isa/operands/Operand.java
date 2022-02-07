package it.uniroma2.pellegrini.z64sim.isa.operands;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public abstract class Operand {

    protected int size;

    public Operand(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    // toString() must be explicitly re-implemented
//    public abstract String toString();
}
