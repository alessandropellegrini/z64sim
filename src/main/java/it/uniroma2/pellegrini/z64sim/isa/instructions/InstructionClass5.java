/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass5 extends Instruction {
    private static final Logger log = LoggerFactory.getLogger();

    private final Operand target;

    public InstructionClass5(String mnemonic, Operand t) {
        super(mnemonic, 5);
        this.target = t;
        this.setSize(8);
    }

    @Override
    public void run() {
        Long dest = SimulatorController.getOperandValue(this.target);

        switch(mnemonic) {
            case "jmp":
                SimulatorController.setRIP(dest);
                break;
            case "call":
                OperandRegister sp = new OperandRegister(Register.RSP, 8);
                Long spValue = SimulatorController.getOperandValue(sp) - 8;
                // TODO: this is a hack, fix it: SP should be 8 bytes, but we're using 4 bytes for now
                OperandMemory spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                SimulatorController.setOperandValue(spMem, SimulatorController.getRIP());
                SimulatorController.setOperandValue(sp, spValue);
                SimulatorController.setRIP(dest);
            case "ret":
            case "retq":
                sp = new OperandRegister(Register.RSP, 8);
                spValue = SimulatorController.getOperandValue(sp);
                spMem = new OperandMemory(-1, -1, -1, -1, spValue.intValue(), 8);
                SimulatorController.setRIP(SimulatorController.getOperandValue(spMem));
                SimulatorController.setOperandValue(sp, spValue + 8);
                break;
            case "iret":
            case "iretq":
                throw new UnsupportedOperationException("Not supported yet.");
            default:
                throw new RuntimeException("Unknown Class 5 instruction: " + mnemonic);
        }
    }

    public Operand getTarget() {
        return this.target;
    }

    @Override
    public String toString() {
        String insn = this.mnemonic + " ";
        if(this.target != null) {
            if(this.target instanceof OperandRegister)
                insn += "*";
            insn += this.target;
        }
        return insn;
    }

}
