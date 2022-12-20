/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;

;

/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass7 extends Instruction {

    private final int transferSize; // The size of a data transfer
    private Operand ioport; // The I/O port number in case of an explicit I/O port

    public InstructionClass7(String mnemonic, int size, Operand ioport) throws ParseException {
        super(mnemonic, 7);
        this.setSize(8);

        this.transferSize = size;
        this.ioport = ioport;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("I/O instructions not supported yet.");
    }

    private String transferSizeToInsnSuffix() throws DisassembleException {
        switch(this.transferSize) {
            case 1:
                return "b";
            case 2:
                return "w";
            case 4:
                return "l";
            case 8:
                return "q";
        }
        throw new DisassembleException("Invalid transfer size");
    }

    private String transferSizeToReg() throws DisassembleException {
        switch(this.transferSize) {
            case 1:
                return "%al";
            case 2:
                return "%ax";
            case 4:
                return "%eax";
            case 8:
                return "%rax";
        }
        throw new DisassembleException("Invalid transfer size");
    }

    private String getIoPort() {
        if(this.ioport == null) {
            return "%dx";
        }
        return this.ioport.toString();
    }

    @Override
    public String toString() {
        String insn;
        try {
            insn = this.mnemonic + this.transferSizeToInsnSuffix() + " ";
            if(this.mnemonic.equals("in")) {
                insn += this.getIoPort() + ", " + this.transferSizeToReg();
            }
            if(this.mnemonic.equals("out")) {
                insn += this.transferSizeToReg() + ", " + this.getIoPort();
            }
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }

        return insn;
    }
}
