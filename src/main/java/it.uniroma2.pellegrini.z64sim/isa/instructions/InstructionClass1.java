package it.uniroma2.pellegrini.z64sim.isa.instructions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass1 extends Instruction {

    /*static boolean disassemble(long l) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    private final Operand source;
    private final Operand destination;
    private final int implicitSize; // For instructions such as pushf, popf,
    // movs, stos, we store the size ('b', 'w',
    // 'l', 'q') here because they have no operands

    public InstructionClass1(String mnemonic, Operand s, Operand d, int implicitSize) {
        super(mnemonic, 1);
        this.source = s;
        this.destination = d;
        this.implicitSize = implicitSize;

        byte[] enc;

        if (s instanceof OperandImmediate && s.getSize() == 64 || (s instanceof OperandMemory &&
                (byte)((OperandMemory) s).getDisplacement()!=-1) || (d instanceof OperandMemory &&
                (byte)((OperandMemory) d).getDisplacement()!=-1)) {
            this.setSize(16);
            enc = new byte[16];
        } else {
            this.setSize(8);
            enc = new byte[8];
        }

        boolean sour_register = s instanceof OperandRegister;
        boolean sour_memory = s instanceof OperandMemory;
        boolean sour_immediate = s instanceof OperandImmediate;
        boolean dest_register = d instanceof OperandRegister;
        boolean dest_memory = d instanceof OperandMemory;

        byte ss = 0;
        byte sd = 0;
        byte di = 0;
        byte mem = 0;
        byte Bp = 0;
        byte Ip = 0;
        byte Scale = 0;
        byte Index_Register = 0;
        byte sour_Register = 0;
        byte dest_Register = 0;

        //Popolamento campo ss
        if(s!=null){
            switch (s.getSize()) {
                case 8:
                    ss = 0b00000000;
                    break;
                case 16:
                    ss = 0b01000000;
                    break;
                case 32:
                    ss = (byte) 0b10000000;
                    break;
                case 64:
                    ss = (byte) 0b11000000;
                    break;
            }
        }

        //Popolamento campo ds
        if(d!=null){
            switch (d.getSize()) {
                case 8:
                    sd = 0b00000000;
                    break;
                case 16:
                    sd = 0b00010000;
                    break;
                case 32:
                    sd = 0b00100000;
                    break;
                case 64:
                    sd = 0b00110000;
                    break;
            }
        }
        //Popolamento campo di
        if(!sour_immediate && !dest_memory) di = 0b00000000;
        if(sour_immediate && !dest_memory) di = 0b00000100;
        if(!sour_immediate && dest_memory && ((OperandMemory)d).getDisplacement() != -1) di = 0b00001000;
        if(sour_immediate && dest_memory && ((OperandMemory)d).getDisplacement() != -1) di = 0b00001100;

        //Popolamento campo mem
        if(sour_register && dest_register) mem = 0b00000000;
        if(sour_register && dest_memory) mem = 0b00000001;
        if(sour_memory && dest_register) mem = 0b00000010;

        //Popolamento campo Bp
        if(sour_memory || dest_memory){
            if(sour_memory){
                if(((OperandMemory)s).getBase() != -1) Bp = (byte)0b10000000;
                else Bp = 0b00000000;
            }
            else{
                if(((OperandMemory)d).getBase() != -1) Bp = (byte)0b10000000;
                else Bp = 0b00000000;
            }
        }

        //Popolamento campo Ip
        if(sour_memory || dest_memory){
            if(sour_memory){
                if(((OperandMemory)s).getIndex()!= -1) Ip = (byte)0b01000000;
                else Ip = 0b00000000;
            }
            else{
                if(((OperandMemory)d).getIndex() != -1) Ip = (byte)0b01000000;
                else Ip = 0b00000000;
            }
        }

        //Popolamento campo Scale e IndexRegister
        if(sour_memory){
            switch (((OperandMemory) s).getScale()){
                case 1:
                    Scale = 0b00000000;
                    break;
                case 2:
                    Scale = 0b00010000;
                    break;
                case 4:
                    Scale = 0b00100000;
                    break;
                case 8:
                    Scale = 0b00110000;
                    break;
            }
            Index_Register = (byte)((OperandMemory)s).getIndex();
        }
        if(dest_memory){
            switch (((OperandMemory) d).getScale()){
                case 1:
                    Scale = 0b00000000;
                    break;
                case 2:
                    Scale = 0b00010000;
                    break;
                case 4:
                    Scale = 0b00100000;
                    break;
                case 8:
                    Scale = 0b00110000;
                    break;
            }
            Index_Register = (byte)((OperandMemory)d).getIndex();
        }
        //Popolamento campo R/M
        if(sour_register) sour_Register = (byte)(((OperandRegister)s).getRegister() << 4);
        if(sour_memory) sour_Register = (byte)(((OperandMemory)s).getBase() << 4);

        if(dest_register) dest_Register = (byte)((OperandRegister)d).getRegister();
        if(dest_memory) dest_Register = (byte)((OperandMemory)d).getBase();

        //Popolamento Displacement e Immediate
        boolean min32 = false;
        boolean displ = false;
        if(sour_immediate && ((OperandImmediate)s).getSize() <= 32) min32 = true;

        if(sour_memory || dest_memory){
            if(sour_memory && ((OperandMemory)s).getDisplacement()!=-1){
                enc[4] = (byte)((OperandMemory) s).getDisplacement();
                enc[5] = (byte)((((OperandMemory) s).getDisplacement()) >> 8);
                enc[6] = (byte)((((OperandMemory) s).getDisplacement()) >> 16);
                enc[7] = (byte)(((OperandMemory) s).getDisplacement() >> 24);
                displ = true;
            }
            if(dest_memory &&((OperandMemory)d).getDisplacement()!=-1){
                enc[4] = (byte)((OperandMemory) d).getDisplacement();
                enc[5] = (byte)((((OperandMemory) d).getDisplacement()) >> 8);
                enc[6] = (byte)((((OperandMemory) d).getDisplacement()) >> 16);
                enc[7] = (byte)(((OperandMemory) d).getDisplacement() >> 24);
                displ = true;
            }
        }
        if(sour_immediate && min32){
            if(displ == false){
                enc[4] = (byte)((OperandImmediate) s).getValue();
                enc[5] = (byte)((((OperandImmediate) s).getValue()) >> 8);
                enc[6] = (byte)((((OperandImmediate) s).getValue()) >> 16);
                enc[7] = (byte)(((OperandImmediate) s).getValue() >> 24);
            }else{
                enc[8] = (byte)((OperandImmediate)s).getValue();
                enc[9] = (byte)((((OperandImmediate) s).getValue()) >> 8);
                enc[10] = (byte)((((OperandImmediate) s).getValue()) >> 16);
                enc[11] = (byte)((((OperandImmediate) s).getValue()) >> 24);
                enc[12] = (byte)((((OperandImmediate) s).getValue()) >> 32);
                enc[13] = (byte)((((OperandImmediate) s).getValue()) >> 40);
                enc[14] = (byte)((((OperandImmediate) s).getValue()) >> 48);
                enc[15] = (byte)(((OperandImmediate) s).getValue() >> 56);
            }
        }
        if(sour_immediate && !min32){
            enc[8] = (byte)((OperandImmediate)s).getValue();
            enc[9] = (byte)((((OperandImmediate) s).getValue()) >> 8);
            enc[10] = (byte)((((OperandImmediate) s).getValue()) >> 16);
            enc[11] = (byte)((((OperandImmediate) s).getValue()) >> 24);
            enc[12] = (byte)((((OperandImmediate) s).getValue()) >> 32);
            enc[13] = (byte)((((OperandImmediate) s).getValue()) >> 40);
            enc[14] = (byte)((((OperandImmediate) s).getValue()) >> 48);
            enc[15] = (byte)(((OperandImmediate) s).getValue() >> 56);
        }

        //MODE
        enc[1] = (byte) (ss | sd  | di | mem);
        //SIB
        enc[2] = (byte) (Bp | Ip | Scale | Index_Register);
        //R-M
        enc[3] = (byte) (sour_Register | dest_Register);
        //Opcode
        switch (mnemonic) {
            case "mov":
                this.type = 0x00;
                break;
            case "movsX":
                this.type = 0x01;
                break;
            case "movzX":
                this.type = 0x02;
                break;
            case "lea":
                this.type = 0x03;
                break;
            case "push":
                this.type = 0x04;
                break;
            case "pop":
                this.type = 0x05;
                break;
            case "pushf":
                this.type = 0x06;
                break;
            case "popf":
                this.type = 0x07;
                break;
            case "movs":
                this.type = 0x08;
                break;
            case "stos":
                this.type = 0x09;
                break;
            default:
                throw new RuntimeException("Unknown Class 1 instruction: " + mnemonic);
        }

        enc[0] = (byte)((byte)0b00010000 | this.type);
        this.setEncoding(enc);
        System.out.println(enc[0]);
        System.out.println("ss è: "+ss);
        System.out.println("ds è: "+sd);
        System.out.println("di è: "+di);
        System.out.println("mem è :"+mem);
        System.out.println("Bp è: "+Bp);
        System.out.println("Ip è: "+Ip);
        System.out.println("Scale è: "+Scale);
        System.out.println("IndexRegister è: "+Index_Register);
        System.out.println("sourReg è: "+sour_Register);
        System.out.println("destReg è: "+dest_Register);
        System.out.println("enc[4] è: "+enc[4]);
        System.out.println("enc[5] è: "+enc[5]);
        System.out.println("enc[6] è: "+enc[6]);
        System.out.println("enc[7[ è: "+enc[7]);
        if(enc.length > 8){
            System.out.println(enc[8]);
            System.out.println(enc[9]);
            System.out.println(enc[10]);
            System.out.println(enc[11]);
            System.out.println(enc[12]);
            System.out.println(enc[13]);
            System.out.println(enc[14]);
            System.out.println(enc[15]);}


    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String toString() {
        String mnem = this.mnemonic;
        return mnem;
    }

    private static String opcodes[] = {"mov", "movs", "movz","lea", "push", "pop","pushf","popf",
            "movs","stos"};

    public static String disassemble(int address) {

        byte[] b = new byte[8];
        for(int i = 0; i < 8; i++) {
            b[i] = Memory.getProgram().program[address + i];
        }

        byte[] b2 = new byte[8];

        String instr="";
        int index = byteToBits(b[0],3,0);
        instr+=opcodes[index];

        int sizeIntDs=0;
        String sizeDest="";
        switch(byteToBits(b[1],5,4)){
            case 0:
                sizeDest="b ";
                sizeIntDs = 8;
                break;
            case 1:
                sizeDest = "w ";
                sizeIntDs = 16;
                break;
            case 2:
                sizeDest="l ";
                sizeIntDs = 32;
                break;
            case 3:
                sizeDest="q ";
                sizeIntDs = 64;
                break;
            default:
                throw new RuntimeException("Wrong value size");
        }
        int sizeIntSs=0;
        String sizeSorg="";
        switch(byteToBits(b[1],7,6)){
            case 0:
                sizeSorg ="b";
                sizeIntSs = 8;
                break;
            case 1:
                sizeSorg ="w";
                sizeIntSs = 16;
                break;
            case 2:
                sizeSorg="l";
                sizeIntSs = 32;
                break;
            case 3:
                sizeSorg="q";
                sizeIntSs = 64;
                break;
            default:
                throw new RuntimeException("Wrong value size");
        }
        int destRegister = byteToBits(b[3],3,0);
        int sourRegister = byteToBits(b[3],7,4);
        String sour_Reg = Register.getRegisterName(sourRegister, sizeIntSs);
        String dest_Reg = Register.getRegisterName(destRegister, sizeIntDs);

        if(index == 1 || index == 2){
            instr += sizeSorg+sizeDest;
        }
        else if(index == 6 || index == 7 || index == 8 || index == 9){
            return instr;
        }
        else if(index == 4 || index == 5){
            instr+= sizeDest+" "+dest_Reg;
            return instr;
        }
        else{
            instr+=sizeDest;
        }


        boolean hasImm  = byteToBits(b[1],2,2) == 1;
        boolean hasDisp = byteToBits(b[1],3,3) == 1;


        // Additional Fetch
        long displ = 0;
        long immed = 0;

        if(hasImm && hasDisp || hasImm && sizeIntSs == 64) {
            for(int i = 0; i < 8; i++) {
                b2[i] = Memory.getProgram().program[address + 8  + i];
            }

            ByteBuffer wrapped = ByteBuffer.wrap(b2);
            wrapped.order(ByteOrder.LITTLE_ENDIAN);
            immed = wrapped.getLong();
            if (immed < 0) immed += Math.pow(2, 64);
            instr+="$"+immed+",";

            skip = true;

        } else if(hasImm && !hasDisp) {
            byte b3[] = new byte[4];
            b3[0] = b[4];
            b3[1] = b[5];
            b3[2] = b[6];
            b3[3] = b[7];


            ByteBuffer wrapped = ByteBuffer.wrap(b3);
            wrapped.order(ByteOrder.LITTLE_ENDIAN);
            immed = wrapped.getInt();
            System.out.println(String.format("%016x", immed));
            if (immed < 0) immed += Math.pow(2, 64);
            instr+="$"+immed+",";
        }

        if(hasDisp) {
            byte b3[] = new byte[4];
            b3[0] = b[4];
            b3[1] = b[5];
            b3[2] = b[6];
            b3[3] = b[7];

            ByteBuffer wrapped = ByteBuffer.wrap(b3);
            wrapped.order(ByteOrder.LITTLE_ENDIAN);
            displ = wrapped.getInt();
            if (displ < 0) displ += Math.pow(2, 32);
            instr+=displ;
        }

        boolean isBp = byteToBits(b[2],7,7) == 1;
        boolean isIp = byteToBits(b[2],6,6) == 1;
        boolean isMemorySource = byteToBits(b[1],1,1) == 1;
        boolean isMemoryDest = byteToBits(b[1],0,0) == 1;

        if(isMemorySource){
            if(isBp){
                instr+="("+sour_Reg;
            }
            else{
                instr+="(,";
            }

            if(isIp){
                int indexRegister = byteToBits(b[2],3,0);
                String index_Reg = Register.getRegisterName(indexRegister, sizeIntSs);
                instr+=", "+index_Reg;
            }
            else{
                instr+=", ";
            }

            switch(byteToBits(b[2],5,4)){
                case 0b00:
                    instr+=", 1), ";
                    break;
                case 0b01:
                    instr+=", 2), ";
                    break;
                case 0b10:
                    instr+=", 4), ";
                    break;
                case 0b11:
                    instr+=", 8), ";
                    break;
                default:
                    instr+=",), ";
            }


        }if(isMemoryDest){
            if(isBp){
                instr+="("+dest_Reg;
            }
            else {
                instr+="(,";
            }

            if(isIp){
                int indexRegister = byteToBits(b[2],3,0);
                String index_Reg = Register.getRegisterName(indexRegister, sizeIntDs);
                instr+=", "+index_Reg;
            }
            else{
                instr+=", ";
            }

            switch(byteToBits(b[2],5,4)){
                case 0b00:
                    instr+=", 1)";
                    break;
                case 0b01:
                    instr+=", 2)";
                    break;
                case 0b10:
                    instr+=", 4)";
                    break;
                case 0b11:
                    instr+=", 8)";
                    break;
                default:
                    instr+=",)";
            }


        }if(!isMemorySource && !isMemoryDest && !hasImm){
            instr+=""+sour_Reg+", "+dest_Reg;
        }else{
            instr+=" "+dest_Reg;
        }

        return instr;
    }


    public Operand getSource() {
        return this.source;
    }

    public Operand getDestination() {
        return this.destination;
    }

}
