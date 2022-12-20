/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import org.jetbrains.annotations.NonNls;


/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass2 extends Instruction {

    private final Operand source;
    private final Operand destination;
    @NonNls
    private static final String[] opcodes = {"add", "sub", "adc", "sbb", "cmp", "test", "neg", "and", "or", "xor", "not", "bt"};

    public InstructionClass2(String mnemonic, Operand s, Operand d) throws ParseException {
        super(mnemonic, 2);
        this.source = s;
        this.destination = d;

        if(s instanceof OperandImmediate && s.getSize() == 8 ||
            s instanceof OperandImmediate && d instanceof OperandMemory) {
            this.setSize(16);
        } else {
            this.setSize(8);
        }
    }

    @Override
    public void run() {
        Long srcValue = SimulatorController.getOperandValue(this.source);
        Long dstValue = SimulatorController.getOperandValue(this.destination);

        long mask = 0;
        switch(this.source.getSize()) {
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

        switch(mnemonic) {
            case "add":
                long result = srcValue + dstValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                break;
            case "sub":
                result = dstValue - srcValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(-srcValue, dstValue, result, this.source.getSize());
                break;
            case "adc":
                srcValue += SimulatorController.getCF() ? 1 : 0;
                result = srcValue + dstValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
            case "sbb":
                srcValue += SimulatorController.getCF() ? 0 : 1;
                result = dstValue - srcValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(-srcValue, dstValue, result, this.source.getSize());
                throw new UnsupportedOperationException("Not supported yet.");
            case "cmp":
                result = dstValue - srcValue;
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                break;
            case "test":
                result = dstValue & srcValue;
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "neg":
                result = -srcValue;
                SimulatorController.setOperandValue(this.source, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(srcValue != 0);
                break;
            case "and":
                result = srcValue & dstValue & mask;
                SimulatorController.setOperandValue(this.destination, result);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "or":
                result = srcValue | dstValue;
                SimulatorController.setOperandValue(this.destination, result & mask);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "xor":
                result = srcValue ^ dstValue;
                SimulatorController.setOperandValue(this.destination, result);
                SimulatorController.updateFlagsAndRefresh(srcValue, dstValue, result, this.source.getSize());
                SimulatorController.setCF(false);
                SimulatorController.setOF(false);
                break;
            case "not":
                SimulatorController.setOperandValue(this.source, ~srcValue & mask);
                break;
            case "bt":
                result = dstValue & (1L << srcValue);
                SimulatorController.setCF(result != 0);
                break;
            default:
                throw new RuntimeException("Unknown Class 2 instruction: " + mnemonic);
        }
    }

    public Operand getSource() {
        return this.source;
    }

    public Operand getDestination() {
        return this.destination;
    }

    @Override
    public String toString() {
        String insn;
        try {
            insn = this.mnemonic + this.source.getSizeSuffix();
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }

        insn += " " + this.source;
        if(this.destination != null) {
            insn += ", " + this.destination;
        }

        return insn;
    }
}
