/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass6 extends Instruction {

    private final byte bit;
    private final OperandMemory target;

    public InstructionClass6(String mnemonic, OperandMemory t) throws ParseException {
        super(mnemonic, 6);
        this.bit = 0; /* depends on the mnemonic */
        this.target = t;

        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        enc[0] = 0b01100000;
        // Set the size in memory
        this.setSize(8);
        byte dest = 0b00000000;
        byte sour = 0b00000000;
        if(t instanceof OperandMemory) {
            dest = (byte) (((OperandMemory) target).getBase());
        }

        enc[3] = (byte) (sour | dest);

        switch(mnemonic) {
            case "jc":
                this.type = 0x00;
                break;
            case "jp":
                this.type = 0x01;
                break;
            case "jz":
                this.type = 0x02;
                break;
            case "js":
                this.type = 0x03;
                break;
            case "jo":
                this.type = 0x04;
                break;
            case "jnc":
                this.type = 0x05;
                break;
            case "jnp":
                this.type = 0x06;
                break;
            case "jnz":
                this.type = 0x07;
                break;
            case "jns":
                this.type = 0x08;
                break;
            case "jno":
                this.type = 0x09;
                break;
            default:
                throw new ParseException("Unknown Class 6 instruction: " + mnemonic);
        }

        enc[0] = (byte) (enc[0] | this.type);
        this.setEncoding(enc);

    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public static String disassemble(byte[] encoding) throws DisassembleException {
        String instr = "";
        switch(encoding[0]) {
            case 0x60:
                instr += "jc";
                break;
            case 0x61:
                instr += "jp";
                break;
            case 0x62:
                instr += "jz";
                break;
            case 0x63:
                instr += "js";
                break;
            case 0x64:
                instr += "jo";
                break;
            case 0x65:
                instr += "jnc";
                break;
            case 0x66:
                instr += "jnp";
                break;
            case 0x67:
                instr += "jnz";
                break;
            case 0x68:
                instr += "jns";
                break;
            case 0x69:
                instr += "jno";
                break;
            default:
                throw new DisassembleException("Unkown instruction type");
        }

        int sizeInt = 0;

        switch(byteToBits(encoding[1], 5, 4)) {
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
                throw new DisassembleException("Wrong value size");
        }

        int destRegister = byteToBits(encoding[3], 3, 0);
        String dest_Reg = Register.getRegisterName(destRegister, sizeInt);
        instr += " " + dest_Reg;


        return instr;

    }

    public Operand getTarget() {
        return this.target;
    }

    @Override
    public String toString() {
        String insn = this.mnemonic + " ";
        if(this.target != null) {
            insn += this.target;
        }
        return insn;
    }
}
