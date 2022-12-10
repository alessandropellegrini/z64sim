package it.uniroma2.pellegrini.z64sim.isa.operands;

public class MemoryTarget {
    private Integer memoryDisplacement = null;

    public MemoryTarget(Integer value) {
        this.memoryDisplacement = value;
    }

    public Integer getDisplacement() {
        return memoryDisplacement;
    }

    public void setDisplacement(Integer value) {
        this.memoryDisplacement = value;
    }
}
