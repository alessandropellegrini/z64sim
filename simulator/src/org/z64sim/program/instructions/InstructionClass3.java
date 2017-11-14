/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

import org.z64sim.program.Instruction;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
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
       
        byte ss = 0b00000000;
        byte sd = 0b00000000;
        byte di = 0b00000000;
        byte diImm = 0b00000000;
        byte mem = 0b00000000;
        
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
        if(places == -1) sd = ss;
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
        sour = (byte) (((OperandRegister)reg).getRegister() << 4);
        dest =  (byte) (((OperandRegister)reg).getRegister());
        enc[3] = (byte) (sour | dest);

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
        System.out.println("\n\nSTAMPA: "+sour +"\n\n");
        System.out.println("\n\nSTAMPA: "+dest+"\n\n");
        System.out.println("\n Stampa size d "+reg.getSize());
        System.out.println("\n\nSTAMPA: "+mem);
        System.out.println("\n\nSTAMPA: "+di);
        System.out.println("\n\nSTAMPA: "+diImm+"");
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {

        String mnem = this.mnemonic;

        if(this.places != -1) {
            mnem = mnem.concat("$" + this.places + ", ");
        }

        mnem = mnem.concat(this.reg.toString());

        return mnem;
    }

}
