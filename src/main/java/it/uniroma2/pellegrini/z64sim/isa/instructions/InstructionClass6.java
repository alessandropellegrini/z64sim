/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass6 extends Instruction {

    private final OperandMemory target;

    public InstructionClass6(String mnemonic, OperandMemory t) throws ParseException {
        super(mnemonic, 6);
        this.target = t;
        this.setSize(8);
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
