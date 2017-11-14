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
public class InstructionClass1 extends Instruction {

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

        byte ss = 0b00000000;
        byte sd = 0b00000000;
        byte di = 0b00000000;
        byte diImm = 0b00000000;
        byte mem = 0b00000000;
        
        if (s instanceof OperandImmediate && s.getSize() == 64 || (s instanceof OperandMemory &&
                (byte)((OperandMemory) s).getDisplacement()!=-1) || (d instanceof OperandMemory &&
                (byte)((OperandMemory) d).getDisplacement()!=-1)) {
            this.setSize(16);
            enc = new byte[16];
        } else {
            this.setSize(8);
            enc = new byte[8];
        }
        enc[0] = 0b00010000; // Class code
        
        if (s instanceof OperandRegister && d instanceof OperandMemory) {
            mem = 0b00000001;
            if (((OperandMemory) d).getDisplacement() != -1) {
                di = 0b00001000;
                enc[4] = (byte)((OperandMemory) d).getDisplacement(); // IPOTIZZANDO CHE IL CAST TRONCHI
                enc[5] = (byte)((((OperandMemory) d).getDisplacement()) >> 8);
                enc[6] = (byte)((((OperandMemory) d).getDisplacement()) >> 16);
                enc[7] = (byte)((((OperandMemory) d).getDisplacement()) >> 24);
            } else {
                di = 0b00000000;
            }
        } else if (s instanceof OperandMemory && d instanceof OperandRegister) {
            mem = 0b00000010;
            if (((OperandMemory) s).getDisplacement() != -1) {
                di = 0b00001000;
                enc[4] = (byte)((OperandMemory) s).getDisplacement(); // IPOTIZZANDO CHE IL CAST TRONCHI
                enc[5] = (byte)((((OperandMemory) s).getDisplacement()) >> 8);
                enc[6] = (byte)((((OperandMemory) s).getDisplacement()) >> 16);
                enc[7] = (byte)((((OperandMemory) s).getDisplacement()) >> 24);
            } else {
                di = 0b00000000;
            }
        } else if (s instanceof OperandRegister && d instanceof OperandRegister) {
            mem = 0b00000000;
            di = 0b00000000;
        }

        if (s instanceof OperandImmediate) {
            diImm = 0b00000100;
            OperandImmediate sorg = (OperandImmediate) s;
            if (sorg.getSize() <= 32 && di != 0b00001000) {
                //Metti nel displacement da 4 byte
                enc[4] = (byte)((OperandImmediate) s).getValue(); // IPOTIZZANDO CHE IL CAST TRONCHI
                enc[5] = (byte)((((OperandImmediate) s).getValue()) >> 8);
                enc[6] = (byte)((((OperandImmediate) s).getValue()) >> 16);
                enc[7] = (byte)((((OperandImmediate) s).getValue()) >> 24);
               
            } else {
                enc[8] = (byte)((OperandImmediate)s).getValue();
                enc[9] = (byte)((((OperandImmediate) s).getValue()) >> 8);
                enc[10] = (byte)((((OperandImmediate) s).getValue()) >> 16);
                enc[11] = (byte)((((OperandImmediate) s).getValue()) >> 24);
                enc[12] = (byte)((((OperandImmediate) s).getValue()) >> 32);
                enc[13] = (byte)((((OperandImmediate) s).getValue()) >> 40);
                enc[14] = (byte)((((OperandImmediate) s).getValue()) >> 48);
                enc[15] = (byte)((((OperandImmediate) s).getValue()) >> 56);  
                
            }
        }
        
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
                default: ss = 0b00000000;
            }
        }
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
                default: sd = 0b00000000;
                }
        }
        //MODE
        enc[1] = (byte) (ss | sd | diImm | di | mem);
        
        //SIB
        byte Bp = 0b00000000;
        byte Ip = 0b00000000;
        byte Scale = 0b00000000;
        byte reg = 0b00000000;
        
        
        
        if(s instanceof OperandMemory || d instanceof OperandMemory){
            Bp = (byte) 0b10000000;
            Ip = 0b01000000;
        }
        if( s instanceof OperandMemory){
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
                default: Scale= 0b00000000;
            }
            reg = (byte) ((OperandMemory)s).getIndex();
        }
        if( d instanceof OperandMemory){
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
                default: Scale = 0b00000000;
            }
            reg = (byte) ((OperandMemory)d).getIndex();
        }
        
        enc[2] = (byte) (Bp | Ip | Scale | reg);
        
        //R-M    
        byte sour = 0b00000000;
        byte dest = 0b00000000;
        
        if(s instanceof OperandMemory) {
            sour = (byte)(((OperandMemory)s).getBase() << 4);
            
        } else if(s instanceof OperandRegister) {
            sour = (byte) (((OperandRegister)s).getRegister() << 4);
            
        }
        
        if(d instanceof OperandMemory) {
            dest = (byte)(((OperandMemory)d).getBase());
            
        } else if(d instanceof OperandRegister) {
            dest = (byte) (((OperandRegister)d).getRegister());
            
        }
       
        enc[3] = (byte) (sour | dest);
        
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
                
        enc[0] = (byte)(enc[0] | this.type);
        this.setEncoding(enc);
        
        System.out.println("encoding[0]: "+encoding[0]);
        System.out.println("type "+this.type);
        System.out.println("ss: "+ss);
        System.out.println("sd: "+sd);
        System.out.println("di: "+di);
        System.out.println("diImm: "+diImm);
        System.out.println("mem: "+mem);
        System.out.println("bp: "+Bp);
        System.out.println("ip: "+Ip);
        System.out.println("scale: "+Scale);
        System.out.println("Reg: "+reg);
        System.out.println("source: "+sour); 
        System.out.println("dest: "+dest);
        
        System.out.println("size:"+this.getSize());
        
        System.out.println("encoding[4]: "+encoding[4]);
        System.out.println("encoding[5]: "+encoding[5]);
        System.out.println("encoding[6]: "+encoding[6]);
        System.out.println("encoding[7]: "+encoding[7]);
        System.out.println("encoding[8]: "+encoding[8]);
        System.out.println("encoding[9]: "+encoding[9]);
        System.out.println("encoding[10]: "+encoding[10]);
        System.out.println("encoding[11]: "+encoding[11]);
        System.out.println("encoding[12]: "+encoding[12]);
        System.out.println("encoding[13]: "+encoding[13]);
        System.out.println("encoding[14]: "+encoding[14]);
        System.out.println("encoding[15]: "+encoding[15]);
        
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String toString() {
        String mnem = this.mnemonic;

        if (this.implicitSize != -1) {
            switch (this.implicitSize) {
                case 8:
                    mnem = mnem.concat("b");
                    break;
                case 16:
                    mnem = mnem.concat("w");
                    break;
                case 32:
                    mnem = mnem.concat("l");
                    break;
                case 64:
                    mnem = mnem.concat("q");
                    break;
                default:
                    throw new RuntimeException("Implicit Size of Class 1 instruction is set, yet to a wrong value");

            }
        }

        if (this.source != null) {
            mnem = mnem.concat(this.source.toString());
        }

        if (this.destination != null) {
            mnem = mnem.concat(", " + this.destination.toString());
        }

        return mnem;
    }

    public Operand getSource() {
        return this.source;
    }

    public Operand getDestination() {
        return this.destination;
    }

}
