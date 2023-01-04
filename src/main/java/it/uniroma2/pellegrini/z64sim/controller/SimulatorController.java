/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.CpuState;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import it.uniroma2.pellegrini.z64sim.view.components.RegisterBank;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public class SimulatorController extends Controller {
    private static final Logger log = LoggerFactory.getLogger();
    private static SimulatorController instance;

    private final CpuState cpuState = new CpuState();
    private Program program;
    private RegisterBank cpuView;

    private SimulatorController() {
    }

    public static void init() {
        instance = new SimulatorController();
    }

    private static SimulatorController getInstance() {
        if(instance == null) init();
        return instance;
    }

    public static void setCpuView(RegisterBank cpuView) {
        getInstance().cpuView = cpuView;
    }

    public static void displaceRIP(int displacement) {
        long rip = getInstance().cpuState.getRIP();
        setRIP(rip + displacement);

    }

    public static Long getRIP() {
        return getInstance().cpuState.getRIP();
    }


    // TODO: move to a dedicated class
    public static long computeAddressingMode(OperandMemory op) {
        final int displacement = op.getDisplacement();
        final long base = op.getBase(); // TODO: long?!
        final int index = op.getIndex();
        final int scale = op.getScale();

        final Long baseValue = base != -1 ? getInstance().cpuState.getRegisterValue((int) base) : 0;
        final Long indexValue = index != -1 ? getInstance().cpuState.getRegisterValue(index) : 0;

        long address = scale != -1 ? indexValue * scale : 0;
        address += baseValue;
        address += displacement;
        return address;
    }

    public static Long getOperandValue(Operand op) throws SimulatorException {
        if(op instanceof OperandRegister) {
            return getInstance().cpuState.getRegisterValue(((OperandRegister) op).getRegister());
        }
        if(op instanceof OperandImmediate) {
            return ((OperandImmediate) op).getValue();
        }
        if(op instanceof OperandMemory) {
            long address = computeAddressingMode((OperandMemory) op);

            long memoryValue = 0;

            try {
                switch(op.getSize()) {
                    case -1:
                        memoryValue = address;
                        break;
                    case 1:
                        memoryValue = Memory.getValueAt(address) & 0xFF;
                        break;
                    case 2:
                        long b0 = Memory.getValueAt(address) & 0xFF;
                        long b1 = Memory.getValueAt(address + 1) & 0xFF;
                        memoryValue = b0 | (b1 << 8);
                        break;
                    case 4:
                        b0 = Memory.getValueAt(address) & 0xFF;
                        b1 = Memory.getValueAt(address + 1) & 0xFF;
                        long b2 = Memory.getValueAt(address + 2) & 0xFF;
                        long b3 = Memory.getValueAt(address + 3) & 0xFF;
                        memoryValue = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
                        break;
                    case 8:
                        b0 = Memory.getValueAt(address) & 0xFF;
                        b1 = Memory.getValueAt(address + 1) & 0xFF;
                        b2 = Memory.getValueAt(address + 2) & 0xFF;
                        b3 = Memory.getValueAt(address + 3) & 0xFF;
                        long b4 = Memory.getValueAt(address + 4) & 0xFF;
                        long b5 = Memory.getValueAt(address + 5) & 0xFF;
                        long b6 = Memory.getValueAt(address + 6) & 0xFF;
                        long b7 = Memory.getValueAt(address + 7) & 0xFF;
                        memoryValue = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32) | (b5 << 40) | (b6 << 48) | (b7 << 56);
                        break;
                }
            } catch(NullPointerException e) {
                log.error("Memory access violation at address " + address);
                throw new SimulatorException("Memory access violation at address " + address);
            }

            return memoryValue;
        }

        return null;
    }

    public static void setOperandValue(Operand destination, Long srcValue) {
        if(destination instanceof OperandRegister) {
            getInstance().cpuState.setRegisterValue(((OperandRegister) destination).getRegister(), srcValue);
            getInstance().refreshRegisters((OperandRegister) destination);
        }
        if(destination instanceof OperandMemory) {
            long address = computeAddressingMode((OperandMemory) destination);

            switch(destination.getSize()) {
                case 1:
                    Memory.setValueAt(address, srcValue.byteValue());
                    break;
                case 2:
                    Memory.setValueAt(address, srcValue.byteValue());
                    Memory.setValueAt(address + 1, (byte) (srcValue >> 8));
                    break;
                case 4:
                    Memory.setValueAt(address, srcValue.byteValue());
                    Memory.setValueAt(address + 1, (byte) (srcValue >> 8));
                    Memory.setValueAt(address + 2, (byte) (srcValue >> 16));
                    Memory.setValueAt(address + 3, (byte) (srcValue >> 24));
                    break;
                case 8:
                    Memory.setValueAt(address, srcValue.byteValue());
                    Memory.setValueAt(address + 1, (byte) (srcValue >> 8));
                    Memory.setValueAt(address + 2, (byte) (srcValue >> 16));
                    Memory.setValueAt(address + 3, (byte) (srcValue >> 24));
                    Memory.setValueAt(address + 4, (byte) (srcValue >> 32));
                    Memory.setValueAt(address + 5, (byte) (srcValue >> 40));
                    Memory.setValueAt(address + 6, (byte) (srcValue >> 48));
                    Memory.setValueAt(address + 7, (byte) (srcValue >> 56));
                    break;
            }
            // TODO: highlight in a different color the memory cells that have been modified
//            Memory.selectAddress(address);
        }
    }

    public static CpuState getCpuState() {
        return getInstance().cpuState;
    }

    private void refreshRegisters(OperandRegister destination) {
        cpuView.setRegister(destination.getRegister(), cpuState.getRegisterValue(destination.getRegister()));
    }

    private void refreshFlags() {
        cpuView.setFlags(cpuState.getFlags(), cpuState.getOF(), cpuState.getDF(), cpuState.getIF(), cpuState.getSF(), cpuState.getZF(), cpuState.getPF(), cpuState.getCF());
    }

    public static void setCF(boolean value) {
        getInstance().cpuState.setCF(value);
    }

    public static void setZF(boolean value) {
        getInstance().cpuState.setZF(value);
    }

    public static void setSF(boolean value) {
        getInstance().cpuState.setSF(value);
    }

    public static void setOF(boolean value) {
        getInstance().cpuState.setOF(value);
    }

    public static void setPF(boolean value) {
        getInstance().cpuState.setPF(value);
    }

    public static void setDF(boolean value) {
        getInstance().cpuState.setDF(value);
    }

    public static void setIF(boolean value) {
        getInstance().cpuState.setIF(value);
    }

    public static boolean getCF() {
        return getInstance().cpuState.getCF();
    }

    public static boolean getZF() {
        return getInstance().cpuState.getZF();
    }

    public static boolean getSF() {
        return getInstance().cpuState.getSF();
    }

    public static boolean getOF() {
        return getInstance().cpuState.getOF();
    }

    public static boolean getPF() {
        return getInstance().cpuState.getPF();
    }

    public static boolean getDF() {
        return getInstance().cpuState.getDF();
    }

    public static boolean getIF() {
        return getInstance().cpuState.getIF();
    }

    private void assembleProgram() {
        String code = MainWindow.getCode();

        Assembler a = new Assembler(code);
        try {
            a.Program();
        } catch(ParseException ignored) {
        } catch(RuntimeException e) {
            JOptionPane.showMessageDialog(null, PropertyBroker.getMessageFromBundle("internal.error.0", e.getClass().getSimpleName()), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        }

        List<String> syntaxErrors = new ArrayList<>(a.getSyntaxErrors());

        StringBuilder assemblerOutput = new StringBuilder();
        if(syntaxErrors.isEmpty()) {
            assemblerOutput.append(PropertyBroker.getMessageFromBundle("gui.assembly.successful"));
            MainWindow.compileResult(assemblerOutput.toString());
        } else {
            assemblerOutput.append(PropertyBroker.getMessageFromBundle("gui.assembly.failed.with.0.errors", syntaxErrors.size()));
            for(String e : syntaxErrors) {
                assemblerOutput.append(e).append("\n");
            }
            MainWindow.compileResult(assemblerOutput.toString());
            return;
        }

        this.program = a.getProgram();
        Memory.setProgram(this.program);

        long _start = this.program._start.getTarget();
        setRIP(_start);
        getInstance().cpuState.setRSP((long) this.program.getLargestAddress());
        getInstance().cpuView.setRegister(Register.RSP, (long) this.program.getLargestAddress());
    }

    public static boolean step() {
        return getInstance().stepInstruction();
    }

    public static void run() {
        boolean hlt;
        do {
            hlt = getInstance().stepInstruction();
        } while(!hlt);
    }

    // Return true if the program reached a halt instruction
    private boolean stepInstruction() {
        SimulatorController sc = getInstance();
        if(sc.program == null) return true;

        long rip = sc.cpuState.getRIP();
        Instruction instruction = sc.fetch();

        try {
            instruction.run();

            if(instruction.getMnemonic().equals("hlt")) {
                return true;
            }
        } catch(RuntimeException e) {
            String error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            String message = PropertyBroker.getMessageFromBundle("runtime.error.0.at.1", error, Long.toHexString(rip));
            JOptionPane.showMessageDialog(null, message, PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        } catch(SimulatorException e) {
            JOptionPane.showMessageDialog(null, e.getMessage() + " [RIP: " + rip + "]", PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    private Instruction fetch() {
        long rip = this.cpuState.getRIP();
        Instruction instruction = (Instruction) this.program.getMemoryElementAt(rip);
        setRIP(rip + instruction.getSize());

        return instruction;
    }

    public static void setRIP(long address) {
        getInstance().cpuState.setRIP(address);
        getInstance().cpuView.setRIP(address);
        Memory.selectAddress(address);
    }

    public static void updateFlagsAndRefresh(long src, long dst, long result, int size) {
        updateFlags(src, dst, result, size);
        getInstance().refreshFlags();
    }

    // TODO: exposing this is a violation of MVC pattern
    public static void refreshUIFlags() {
        getInstance().refreshFlags();
    }

    // This function updates FLAGS based on ADDITION arithmetics. If used for subtraction, src should be negated.
    public static void updateFlags(long src, long dst, long result, int size) {
        long mask = 0;
        long msbMask = 0;
        switch(size) {
            case 1:
                mask = 0xFF;
                msbMask = 1 << 7;
                break;
            case 2:
                mask = 0xFFFF;
                msbMask = 1 << 15;
                break;
            case 4:
                mask = 0xFFFFFFFF;
                msbMask = 1L << 31;
                break;
            case 8:
                mask = 0xFFFFFFFFFFFFFFFFL;
                msbMask = 1L << 63;
                break;
        }

        boolean cf = src > mask - dst;
        boolean zf = (result & mask) == 0;
        boolean sf = (result & msbMask) != 0;
        boolean of = (src & msbMask) == 0 && (dst & msbMask) == 0 && (result & msbMask) != 0
            || (src & msbMask) != 0 && (dst & msbMask) != 0 && (result & msbMask) == 0;
        boolean pf = countSetBits(result & mask) % 2 == 1;

        setCF(cf);
        setZF(zf);
        setSF(sf);
        setOF(of);
        setPF(pf);
    }

    private static int countSetBits(long n) {
        int count = 0;
        while(n > 0) {
            count += n & 1;
            n >>= 1;
        }
        return count;
    }

    @Override
    public boolean dispatch(Events command) {
        switch(command) {
            case ASSEMBLE_PROGRAM:
                assembleProgram();
                break;
        }
        return false;
    }
}
