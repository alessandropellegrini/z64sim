package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass3 extends Instruction {

    private final int places;
    private final OperandRegister reg;

    public InstructionClass3(String mnemonic, int p, OperandRegister r) {
        super(mnemonic, 3);
        this.places = p;
        this.reg = r;

        // First byte is the class
        byte[] enc = {0b00110000, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        this.setSize(8);

        enc[0] = 0b00110000;

        byte ss = 0b00000000;
        byte sd = 0b00000000;
        byte di = 0b00000000;
        byte diImm = 0b00000000;
        byte mem = 0b00000000;

        if(reg!=null){
            switch (reg.getSize()) {
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
        }else{
            ss = 0b00000000;
        }
        if(places == -1){
            sd = ss;
            diImm = 0b00000000;
        }else{
            diImm = 0b00000100;
        }
        //MODE
        enc[1] = (byte) (ss | sd | diImm | di | mem);

        //SIB
        byte Bp = 0b00000000;
        byte Ip = 0b00000000;
        byte Scale = 0b00000000;
        byte Reg = 0b00000000;

        enc[2] = (byte) (Bp | Ip | Scale | Reg);

        //R-M
        byte sour = 0b00000000;
        byte dest = 0b00000000;

        dest =  (byte) (((OperandRegister)reg).getRegister());
        enc[3] = (byte) (sour | dest);

        enc[4] = (byte)(places >> 24); // IPOTIZZANDO CHE IL CAST TRONCHI
        enc[5] = (byte)(places >> 16);
        enc[6] = (byte)(places >> 8);
        enc[7] = (byte)(places);

        switch (mnemonic) {
            case "sal":
                if(this.places != -1){
                    this.type = 0x00;
                }
                else this.type = 0x01;
                break;
            case "shl":
                if(this.places != -1){
                    this.type = 0x00;
                }
                else this.type = 0x01;
                break;
            case "sar":
                if(this.places != -1){
                    this.type = 0x02;
                }
                else this.type = 0x03;
                break;
            case "shr":
                if(this.places != -1){
                    this.type = 0x04;
                }
                else this.type = 0x05;
                break;
            case "rcl":
                if(this.places != -1){
                    this.type = 0x06;
                }
                else this.type = 0x07;
                break;
            case "rcr":
                if(this.places != -1){
                    this.type = 0x08;
                }
                else this.type = 0x09;
                break;
            case "rol":
                if(this.places != -1){
                    this.type = 0x0a;
                }
                else this.type = 0x0b;
                break;
            case "ror":
                if(this.places != -1){
                    this.type = 0x0c;
                }
                else this.type = 0x0d;
                break;
            default:
                throw new RuntimeException("Unknown Class 3 instruction: " + mnemonic);
        }
        this.setSize(size);
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
        switch (b[0]){
            case 0x30:
            case 0x31:
                instr+="shl ";
                break;
            case 0x32:
            case 0x33:
                instr+="sar ";
                break;
            case 0x34:
            case 0x35:
                instr+="shr ";
                break;
            case 0x36:
            case 0x37:
                instr+="rcl ";
                break;
            case 0x38:
            case 0x39:
                instr+="rcr ";
                break;
            case 0x3a:
            case 0x3b:
                instr+="rol ";
                break;
            case 0x3c:
            case 0x3d:
                instr+="ror ";
                break;
            default:
                throw new RuntimeException("Unkown instruction type");

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
        if(byteToBits(b[1],2,2) == 1){
            instr +="$"+((b[7]<<24)+(b[6]<<16)+(b[5]<<8)+b[4])+",";
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
}