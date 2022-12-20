/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;


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
        Long dest = SimulatorController.getOperandValue(this.target);

        switch(mnemonic) {
            case "jc":
                if(SimulatorController.getCF())
                    SimulatorController.setRIP(dest);
                break;
            case "jp":
                if(SimulatorController.getPF())
                    SimulatorController.setRIP(dest);
                break;
            case "jz":
                if(SimulatorController.getZF())
                    SimulatorController.setRIP(dest);
                break;
            case "js":
                if(SimulatorController.getSF())
                    SimulatorController.setRIP(dest);
                break;
            case "jo":
                if(SimulatorController.getOF())
                    SimulatorController.setRIP(dest);
                break;
            case "jnc":
                if(!SimulatorController.getCF())
                    SimulatorController.setRIP(dest);
                break;
            case "jnp":
                if(!SimulatorController.getPF())
                    SimulatorController.setRIP(dest);
                break;
            case "jnz":
                if(!SimulatorController.getZF())
                    SimulatorController.setRIP(dest);
                break;
            case "jns":
                if(!SimulatorController.getSF())
                    SimulatorController.setRIP(dest);
                break;
            case "jno":
                if(!SimulatorController.getOF())
                    SimulatorController.setRIP(dest);
                break;
            default:
                throw new RuntimeException("Unknown Class 6 instruction: " + mnemonic);
        }
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
