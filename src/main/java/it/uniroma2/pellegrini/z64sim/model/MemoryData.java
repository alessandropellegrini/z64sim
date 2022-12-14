package it.uniroma2.pellegrini.z64sim.model;

public class MemoryData implements MemoryElement {
    private byte value;

    public MemoryData(byte value) {
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return new byte[]{this.value};
    }

    @Override
    public int getSize() {
        return 1;
    }

    public void setValue(byte value) {
        this.value = value;
    }
}
