package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass5 extends Instruction {

    private final Operand target;

    public InstructionClass5(String mnemonic, Operand t) {
        super(mnemonic, 5);
        this.target = t;

        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        enc[0] = 0b01010000;
        // Set the size in memory
        this.setSize(8);

        byte sour = 0b00000000;
        byte dest = 0b00000000;
        byte sd = 0b00000000;

        if(t!=null){
            switch (t.getSize()) {
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
        else{
            sd = 0b00000000;
        }
        if(t!=null){
            if(t instanceof OperandMemory)
                dest = (byte) (((OperandMemory)t).getBase());
            else {
                dest = (byte)(((OperandRegister)t).getRegister());
            }
        }

        enc[3] = (byte) (sour | dest);

        switch (mnemonic) {
            case "jmp":
                if(t instanceof OperandMemory){
                    this.type = 0x00;
                }else{
                    this.type = 0x01;
                }
                break;
            case "call":
                if(t instanceof OperandMemory){
                    this.type = 0x02;
                }else{
                    this.type = 0x03;
                }
            case "ret":
                this.type = 0x04;
                break;
            case "iret":
                this.type = 0x05;
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }

        enc[0] = (byte)(enc[0] | this.type);
        this.setEncoding(enc);

    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public static String disassemble(int address) {
        byte b[] = new byte[8];
        for(int i = 0; i < 8; i++) {
            b[i] = Memory.getProgram().program[address + i];
        }

        String instr="";

        switch(b[0]){
            case 0x50:
                instr+="jmp *";
                break;
            case 0x51:
                instr+="jmp ";
                break;
            case 0x52:
                instr+="call *";
                break;
            case 0x53:
                instr+="call ";
                break;
            case 0x54:
                instr+="ret ";
                return instr;

            case 0x55:
                instr+="iret ";
                return instr;

        }
        int sizeInt=0;

        switch(byteToBits(b[1],7,6)){
            case 0:
                sizeInt = 8;
                break;
            case 1:
                sizeInt = 16;
                break;
            case 2:
                sizeInt = 32;
                break;
            case 3:
                sizeInt = 64;
                break;
            default:
                throw new RuntimeException("Wrong value size");
        }

        int destRegister = byteToBits(b[3],3,0);
        String dest_Reg = Register.getRegisterName(destRegister, sizeInt);
        instr+=dest_Reg;

        System.out.println(b[0]);
        System.out.println(b[1]);
        System.out.println(b[2]);
        System.out.println(b[3]);
        System.out.println(b[4]);
        System.out.println(b[5]);
        System.out.println(b[6]);
        System.out.println(b[7]);


        return instr;
    }

    public Operand getTarget() {
        return this.target;
    }

}
