/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
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

        int bitWidth = this.reg.getSize() * 8;
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
        long msbMask = mask & (~mask >>> 1);
        long msb = value & msbMask;
        long lsb = value & 1;

        long result;

        switch(mnemonic) {
            case "sal":
            case "shl":
                result = (value << places) & mask;
                SimulatorController.updateFlags(-1, -1, result, this.reg.getSize(), false);
                SimulatorController.setCF(msb != 0);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) ^ msb) != 0);
                }
                break;
            case "sar":
                // SAR = Shift Arithmetic Right: preserves sign bit
                result = (value >> places) & mask;
                SimulatorController.updateFlags(-1, -1, result, this.reg.getSize(), false);
                SimulatorController.setCF(lsb != 0);
                if(places == 1) {
                    SimulatorController.setOF(false);
                }
                break;
            case "shr":
                // SHR = Shift Logical Right: fills with zeros
                result = (value >>> places) & mask;
                SimulatorController.updateFlags(-1, -1, result, this.reg.getSize(), false);
                SimulatorController.setCF(lsb != 0);
                if(places == 1) {
                    SimulatorController.setOF(msb != 0);
                }
                break;
            case "rcl": {
                // RCL = Rotate through Carry Left
                result = value;
                boolean cf = SimulatorController.getCF();
                for(int i = 0; i < places; i++) {
                    boolean oldMsb = (result & msbMask) != 0;
                    result = ((result << 1) & mask) | (cf ? 1 : 0);
                    cf = oldMsb;
                }
                SimulatorController.setCF(cf);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) != 0) != cf);
                }
                break;
            }
            case "rcr": {
                // RCR = Rotate through Carry Right
                result = value;
                boolean cf = SimulatorController.getCF();
                for(int i = 0; i < places; i++) {
                    boolean oldLsb = (result & 1) != 0;
                    result = ((result >>> 1) & mask) | (cf ? msbMask : 0);
                    cf = oldLsb;
                }
                SimulatorController.setCF(cf);
                if(places == 1) {
                    long resultMsb = result & msbMask;
                    long resultMsb1 = result & (msbMask >>> 1);
                    SimulatorController.setOF((resultMsb != 0) != (resultMsb1 != 0));
                }
                break;
            }
            case "rol": {
                // ROL = Rotate Left
                int count = places % bitWidth;
                result = ((value << count) | (value >>> (bitWidth - count))) & mask;
                SimulatorController.setCF((result & 1) != 0);
                if(places == 1) {
                    SimulatorController.setOF(((result & msbMask) != 0) != ((result & 1) != 0));
                }
                break;
            }
            case "ror": {
                // ROR = Rotate Right
                int count = places % bitWidth;
                result = ((value >>> count) | (value << (bitWidth - count))) & mask;
                SimulatorController.setCF((result & msbMask) != 0);
                if(places == 1) {
                    long resultMsb = result & msbMask;
                    long resultMsb1 = result & (msbMask >>> 1);
                    SimulatorController.setOF((resultMsb != 0) != (resultMsb1 != 0));
                }
                break;
            }
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
