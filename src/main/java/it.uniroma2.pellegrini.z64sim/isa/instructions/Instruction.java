package it.uniroma2.pellegrini.z64sim.isa.instructions;


import java.nio.ByteBuffer;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public abstract class Instruction {

    protected final String mnemonic;
    protected final byte clas;
    protected byte type;
    protected int size;
    protected byte[] encoding;

    public Instruction(String mnemonic, int clas) {
        this.mnemonic = mnemonic;
        this.clas = (byte)clas;
    }

    protected static boolean skip = false;

    // toString() must be explicitly re-implemented
//    public static String disassemble(int address) {
//        if(Instruction.skip) {
//            Instruction.skip = false;
//            return "";
//        }
//        byte opcode = 0;
//        opcode = (byte)(Memory.getProgram().program[address] & 0b11110000);
//
//        switch(opcode){
//            case 0:
//                return InstructionClass0.disassemble(address);
//            case 1*16:
//                return InstructionClass1.disassemble(address);
//            case 2*16:
//                return disassemble(address);
//            case 3*16:
//                return InstructionClass3.disassemble(address);
//            case 4*16:
//                return InstructionClass4.disassemble(address);
//            case 5*16:
//                return InstructionClass5.disassemble(address);
//            case 6*16:
//                return InstructionClass6.disassemble(address);
//            case 7*16:
//                return InstructionClass7.disassemble(address);
//            default :
//                return JOptionPane.showInputDialog(opcode);
//        }
//    }

    public byte getClas() {
        return this.clas;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public byte[] getEncoding() {
        return encoding;
    }

    public void setEncoding(byte[] encoding) {
        this.encoding = encoding;
    }

    public abstract void run();

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        ByteBuffer bb = ByteBuffer.allocate(b.length);
        bb.put(b);
        return bb.getLong();
    }

    public static byte byteToBits(byte b, int start, int end){
        byte mask = 0;
        if(start < end || start > 7 || end < 0 ) throw new RuntimeException("No valid start || end");

        for (int i = 7-start; i <= 7-end; i++) {
            mask += 1 << 7-i;
        }
        byte ret = (byte) (mask & b);
        for (int i = 0; i < end; i++) {
            ret /= 2;
        }
        if (ret < 0) {
            ret += Math.pow(2, start-end+1);
        }
        return ret;
    }



}
