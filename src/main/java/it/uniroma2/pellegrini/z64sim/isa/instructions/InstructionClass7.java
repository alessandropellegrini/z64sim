/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.DisassembleException;
import it.uniroma2.pellegrini.z64sim.devices.Dmac;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.CpuState;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;


/**
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class InstructionClass7 extends Instruction {

    private static final String[] MNEMONICS = {"in", "out", "ins", "outs"};

    private final int transferSize; // The size of a data transfer
    private Operand ioport; // The I/O port number in case of an explicit I/O port

    public InstructionClass7(String mnemonic, int size, Operand ioport) throws ParseException {
        super(mnemonic, 7);
        this.setSize(8);

        this.transferSize = size;
        this.ioport = ioport;
    }

    public int getTransferSize() {
        return this.transferSize;
    }

    public Operand getIoPort() {
        return this.ioport;
    }

    @Override
    public int getType() {
        return lookupType(MNEMONICS);
    }

    @Override
    protected byte[] encode() {
        byte[] buf = new byte[this.size];
        buf[0] = encodeOpcode(getType());

        int di = (this.ioport instanceof OperandImmediate) ? 1 : 0;
        buf[1] = encodeMode(sizeToSsDs(this.transferSize), sizeToSsDs(this.transferSize), di, 0);
        buf[2] = encodeSib(0, 0, 0, 0);
        buf[3] = encodeRm(0, 0);

        if (this.ioport instanceof OperandImmediate) {
            writeLE32(buf, 4, (int) ((OperandImmediate) this.ioport).getValue());
        }

        return buf;
    }

    @Override
    public void run() {
        // ins/outs: program the DMAC and trigger a burst transfer
        if (this.mnemonic.equals("ins") || this.mnemonic.equals("outs")) {
            runStringIO();
            return;
        }

        // Resolve the I/O port address
        long port;
        if (this.ioport != null) {
            port = ((OperandImmediate) this.ioport).getValue();
        } else {
            // Port comes from %dx (lower 16 bits of RDX)
            port = SimulatorController.getCpuState()
                       .getRegisterValue(Register.RDX) & 0xFFFF;
        }

        // Build a mask for the transfer size
        long mask;
        switch (this.transferSize) {
            case 1: mask = 0xFFL; break;
            case 2: mask = 0xFFFFL; break;
            case 4: mask = 0xFFFFFFFFL; break;
            default: mask = 0xFFFFFFFFFFFFFFFFL; break;
        }

        DeviceMapping mapping = Devices.getInstance().getMappingForPort(port);

        if (this.mnemonic.equals("in")) {
            long value = 0;
            if (mapping != null) {
                value = mapping.read(port);
            }
            // Mask to transfer size and store in %rax
            SimulatorController.getCpuState()
                .setRegisterValue(Register.RAX, value & mask);
        }

        if (this.mnemonic.equals("out")) {
            // Read %rax, mask to transfer size
            long value = SimulatorController.getCpuState()
                             .getRegisterValue(Register.RAX) & mask;
            if (mapping != null) {
                mapping.write(port, value);
            }
        }
    }

    /**
     * Execute an ins/outs string I/O instruction.
     * <p>
     * These instructions program the DMAC coprocessor from implicit operands
     * and trigger a burst transfer:
     * <ul>
     *   <li>{@code ins}: %dx=device port, %rdi=dest addr, %rcx=count, DF=direction</li>
     *   <li>{@code outs}: %dx=device port, %rsi=src addr, %rcx=count, DF=direction</li>
     * </ul>
     */
    private void runStringIO() {
        CpuState cpuState = SimulatorController.getCpuState();
        long port = cpuState.getRegisterValue(Register.RDX) & 0xFFFF;
        long count = cpuState.getRegisterValue(Register.RCX);
        boolean df = SimulatorController.getDF();

        Dmac dmac = Devices.getInstance().getDmac();

        if (this.mnemonic.equals("ins")) {
            // Device --> memory: destination address in %rdi
            long destAddr = cpuState.getRegisterValue(Register.RDI);
            dmac.transfer(destAddr, port, count, this.transferSize, df, 0);
            // Update %rdi: advance by count × transferSize
            long delta = count * this.transferSize;
            cpuState.setRegisterValue(Register.RDI, destAddr + (df ? -delta : delta));
        } else {
            // Memory --> device: source address in %rsi
            long srcAddr = cpuState.getRegisterValue(Register.RSI);
            dmac.transfer(srcAddr, port, count, this.transferSize, df, 1);
            // Update %rsi: advance by count × transferSize
            long delta = count * this.transferSize;
            cpuState.setRegisterValue(Register.RSI, srcAddr + (df ? -delta : delta));
        }

        // %rcx = 0 after transfer
        cpuState.setRegisterValue(Register.RCX, 0L);
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

    private String getIoPortString() {
        if(this.ioport == null) {
            return "%dx";
        }
        return this.ioport.toString();
    }

    @Override
    public String toString() {
        String insn;
        try {
            insn = this.mnemonic + this.transferSizeToInsnSuffix();
            if (this.mnemonic.equals("ins") || this.mnemonic.equals("outs")) {
                // String I/O instructions have no explicit operands
                return insn;
            }
            insn += " ";
            if(this.mnemonic.equals("in")) {
                insn += this.getIoPortString() + ", " + this.transferSizeToReg();
            }
            if(this.mnemonic.equals("out")) {
                insn += this.transferSizeToReg() + ", " + this.getIoPortString();
            }
        } catch(DisassembleException e) {
            throw new RuntimeException(e);
        }

        return insn;
    }
}
