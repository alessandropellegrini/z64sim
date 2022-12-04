package it.uniroma2.pellegrini.z64sim.isa.operands;

public class MemoryTarget {
    private Long memoryDisplacement = null;

    public MemoryTarget(Long value) {
        this.memoryDisplacement = value;
    }

    public Long getDisplacement() {
        return memoryDisplacement;
    }

    public void setDisplacement(long value) {
        this.memoryDisplacement = value;
    }
}
