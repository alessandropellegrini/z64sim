/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;


import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass4 extends Instruction {

    public InstructionClass4(String mnemonic) {
        super(mnemonic, 4);
        this.setSize(8);
    }

    @Override
    public void run() {
        switch(mnemonic) {
            case "clc":
                SimulatorController.setCF(false);
                break;
            case "clp":
                SimulatorController.setPF(false);
                break;
            case "clz":
                SimulatorController.setZF(false);
                break;
            case "cls":
                SimulatorController.setSF(false);
                break;
            case "cli":
                SimulatorController.setIF(false);
                break;
            case "cld":
                SimulatorController.setDF(false);
                break;
            case "clo":
                SimulatorController.setOF(false);
                break;
            case "stc":
                SimulatorController.setCF(true);
                break;
            case "stp":
                SimulatorController.setPF(true);
                break;
            case "stz":
                SimulatorController.setZF(true);
                break;
            case "sts":
                SimulatorController.setSF(true);
                break;
            case "sti":
                SimulatorController.setIF(true);
                break;
            case "std":
                SimulatorController.setDF(true);
                break;
            case "sto":
                SimulatorController.setOF(true);
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }
        SimulatorController.refreshUIFlags();
    }

    @Override
    public String toString() {
        return this.mnemonic;
    }
}
