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

    private final byte bit;
    private byte val;

    public InstructionClass4(String mnemonic) {
        super(mnemonic, 4);
        this.bit = 0; /* depends on the mnemonic */
        this.val = 0; /* depends on the mnemonic: 0 for clear, 1 for set */

        // Set the size in memory
        this.setSize(8);

        // Will be initialized in the switch case, as well the class
        byte[] enc = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        enc[0] = 0b01000000;

        switch(mnemonic) {
            case "clc":
                this.type = 0;
                break;
            case "clp":
                this.type = 1;
                break;
            case "clz":
                this.type = 2;
                break;
            case "cls":
                this.type = 3;
                break;
            case "cli":
                this.type = 4;
                break;
            case "cld":
                this.type = 5;
                break;
            case "clo":
                this.type = 6;
                break;
            case "stc":
                this.type = 8;
                break;
            case "stp":
                this.type = 9;
                break;
            case "stz":
                this.type = 10;
                break;
            case "sts":
                this.type = 11;
                break;
            case "sti":
                this.type = 12;
                break;
            case "std":
                this.type = 13;
                break;
            case "sto":
                this.type = 14;
                break;
            default:
                throw new RuntimeException("Unknown Class 4 instruction: " + mnemonic);
        }

        enc[0] = (byte) (enc[0] | this.type);
        this.setEncoding(enc);


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
    }

    @Override
    public String toString() {
        return this.mnemonic;
    }
}
