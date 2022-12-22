/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass3 extends Instruction {
    private static final Logger log = LoggerFactory.getLogger();

    // TODO: make consistent with src and dest. Can use RCX as source when explicit places is given
    private final int places;
    private final OperandRegister reg;

    public InstructionClass3(String mnemonic, int p, OperandRegister r) {
        super(mnemonic, 3);
        this.places = p;
        this.reg = r;
        this.setSize(8);
    }

    @Override
    public void run() throws SimulatorException {
        Long value = SimulatorController.getOperandValue(this.reg);

        long mask = 0;
        switch(this.reg.getSize()) {
            case 1:
                mask = 0xFF;
                break;
            case 2:
                mask = 0xFFFF;
                break;
            case 4:
                mask = 0xFFFFFFFF;
                break;
            case 8:
                mask = 0xFFFFFFFFFFFFFFFFL;
                break;
        }
        long msbMask = mask & (~mask >> 1);
        long msb = value & msbMask;
        long lsb = value & 1;

        long result;

        switch(mnemonic) {
            case "sal":
            case "shl":
                result = value << places;
                SimulatorController.updateFlags(-1, -1, result, this.reg.getSize());
                SimulatorController.setCF(msb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
                SimulatorController.refreshUIFlags();
                break;
            case "sar":
                result = value >>> places;
                SimulatorController.updateFlags(-1, -1, result, this.reg.getSize());
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(false);
                }
                SimulatorController.refreshUIFlags();
                break;
            case "shr":
                result = value >> places;
                SimulatorController.updateFlags(-1, -1, result, this.reg.getSize());
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(msb == 1);
                }
                SimulatorController.refreshUIFlags();
                break;
            case "rcl":
                long currentCF = SimulatorController.getCF() ? 1 : 0;
                result = (value << 1) | currentCF;
                SimulatorController.setCF(msb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
                SimulatorController.refreshUIFlags();
            case "rcr":
                currentCF = SimulatorController.getCF() ? 1 : 0;
                result = (value >> 1) | (currentCF << (this.reg.getSize() - 1));
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
                SimulatorController.refreshUIFlags();
            case "rol":
                result = (value << 1) | (msb >> (this.reg.getSize() - 1));
                SimulatorController.setCF(msb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) == 1);
                }
                SimulatorController.refreshUIFlags();
            case "ror":
                result = (value >> 1) | (lsb << (this.reg.getSize() - 1));
                SimulatorController.setCF(lsb == 1);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ (result & (msbMask >> 1))) == 1);
                }
                SimulatorController.refreshUIFlags();
                throw new UnsupportedOperationException("Not supported yet.");
            default:
                throw new RuntimeException("Unknown Class 3 instruction: " + mnemonic);
        }

        SimulatorController.setOperandValue(this.reg, result);
    }

    @Override
    public String toString() {
        try {
            return this.mnemonic + this.reg.getSizeSuffix() + " $" + this.places + ", " + this.reg;
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }
    }
}
