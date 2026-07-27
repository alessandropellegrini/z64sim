/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass6 extends Instruction {

    private static final String[] MNEMONICS = {"jc", "jp", "jz", "js", "jo", "jnc", "jnp", "jnz", "jns", "jno"};

    private final OperandMemory target;

    public InstructionClass6(String mnemonic, OperandMemory t) {
        super(mnemonic, 6);
        this.target = t;
        this.setSize(8);
    }

    public int getType() {
        return lookupType(MNEMONICS);
    }

    @Override
    protected byte[] encode() {
        byte[] buf = new byte[this.size];
        buf[0] = encodeOpcode(getType());
        buf[1] = encodeMode(0, 0, 2, 0);
        buf[2] = encodeSib(0, 0, 0, 0);
        buf[3] = encodeRm(0, 0);
        if (this.target != null) {
            writeLE32(buf, 4, this.target.getDisplacement());
        }
        return buf;
    }

    @Override
    public void run() throws SimulatorException {
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

    public OperandMemory getTarget() {
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
