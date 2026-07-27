/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.isa.instructions;

import it.uniroma2.pellegrini.z64sim.isa.operands.Operand;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandMemory;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates resolved microoperation sequences for z64 instructions.
 * Conditionals from the parametric microcode are resolved based on the
 * concrete operand types of each instruction instance.
 */
public class MicrocodeGenerator {

    private static final String ARROW = " ← ";

    // =====================================================================
    // Public entry point
    // =====================================================================

    /**
     * Generate the resolved microoperation sequence for the given instruction.
     */
    public static List<String> generate(Instruction insn) {
        if (insn instanceof InstructionClass0) return generateClass0((InstructionClass0) insn);
        if (insn instanceof InstructionClass1) return generateClass1((InstructionClass1) insn);
        if (insn instanceof InstructionClass2) return generateClass2((InstructionClass2) insn);
        if (insn instanceof InstructionClass3) return generateClass3((InstructionClass3) insn);
        if (insn instanceof InstructionClass4) return generateClass4((InstructionClass4) insn);
        if (insn instanceof InstructionClass5) return generateClass5((InstructionClass5) insn);
        if (insn instanceof InstructionClass6) return generateClass6((InstructionClass6) insn);
        if (insn instanceof InstructionClass7) return generateClass7((InstructionClass7) insn);

        List<String> ops = new ArrayList<>();
        addFetch(ops);
        return ops;
    }

    // =====================================================================
    // Shared building blocks
    // =====================================================================

    /** Instruction fetch: common to all instructions */
    private static void addFetch(List<String> ops) {
        ops.add("MAR" + ARROW + "RIP");
        ops.add("MDR" + ARROW + "(MAR); RIP" + ARROW + "RIP + 8");
        ops.add("IR" + ARROW + "MDR");
    }

    /** 64-bit immediate fetch: extra memory read after fetch */
    private static void addImmediateFetch(List<String> ops) {
        ops.add("MAR" + ARROW + "RIP");
        ops.add("MDR" + ARROW + "(MAR); RIP" + ARROW + "RIP + 8");
    }

    /** Memory read: MDR ← (MAR) */
    private static void addMemoryRead(List<String> ops) {
        ops.add("MDR" + ARROW + "(MAR)");
    }

    /** Memory write: (MAR) ← MDR */
    private static void addMemoryWrite(List<String> ops) {
        ops.add("(MAR)" + ARROW + "MDR");
    }

    /** Push MDR onto stack: decrement RSP via ALU, write MDR to (RSP) */
    private static void addStackPush(List<String> ops) {
        ops.add("TEMP1" + ARROW + "RSP");
        ops.add("TEMP2" + ARROW + "8");
        ops.add("RSP" + ARROW + "ALU_OUT[SUB]");
        ops.add("MAR" + ARROW + "RSP");
        addMemoryWrite(ops);
    }

    /** Pop from stack into MDR: read (RSP) into MDR, increment RSP via ALU */
    private static void addStackPop(List<String> ops) {
        ops.add("MAR" + ARROW + "RSP");
        addMemoryRead(ops);
        ops.add("TEMP1" + ARROW + "RSP");
        ops.add("TEMP2" + ARROW + "8");
        ops.add("RSP" + ARROW + "ALU_OUT[ADD]");
    }

    /** Relative jump: RIP ← RIP + sign-extend(IR[0:31]) */
    private static void addRelativeJump(List<String> ops) {
        addRelativeJump(ops, "");
    }

    /** Relative jump with indentation (for conditional blocks) */
    private static void addRelativeJump(List<String> ops, String indent) {
        ops.add(indent + "TEMP2" + ARROW + "IR[0:31]");
        ops.add(indent + "TEMP1" + ARROW + "ALU_OUT[Sign Extend, 10, 11]");
        ops.add(indent + "TEMP2" + ARROW + "RIP");
        ops.add(indent + "RIP" + ARROW + "ALU_OUT[ADD]");
    }

    /** Decrement RCX by 1 (REP loop counter) */
    private static void addRepDecrement(List<String> ops, String indent) {
        ops.add(indent + "TEMP1" + ARROW + "RCX");
        ops.add(indent + "TEMP2" + ARROW + "1");
        ops.add(indent + "RCX" + ARROW + "ALU_OUT[SUB]");
    }

    /** Advance a pointer register by stepSize, direction depends on DF */
    private static void addDirectionAdvance(List<String> ops, String reg, String indent, int stepSize) {
        ops.add(indent + "TEMP1" + ARROW + reg);
        ops.add(indent + "TEMP2" + ARROW + String.valueOf(stepSize));
        ops.add(indent + "if FLAGS[D] == 0");
        ops.add(indent + "    " + reg + ARROW + "ALU_OUT[ADD]");
        ops.add(indent + "else");
        ops.add(indent + "    " + reg + ARROW + "ALU_OUT[SUB]");
        ops.add(indent + "endif");
    }

    /**
     * Preamble for instructions with src/dst operands:
     * fetch 64-bit immediate if needed, then compute address.
     */
    private static void addOperandPreamble(List<String> ops, int insnSize,
                                            boolean hasImm, Operand src, Operand dst) {
        if (hasImm && insnSize == 16) {
            addImmediateFetch(ops);
        }
        if (src instanceof OperandMemory) {
            addAddressCalc(ops, (OperandMemory) src);
        } else if (dst instanceof OperandMemory) {
            addAddressCalc(ops, (OperandMemory) dst);
        }
    }

    /**
     * Address calculation for a memory operand.
     * Resolves the parametric microcode based on actual Bp, Ip, D values.
     */
    private static void addAddressCalc(List<String> ops, OperandMemory mem) {
        boolean hasBp = mem.getBase() >= 0;
        boolean hasIp = mem.getIndex() >= 0;
        boolean hasD = mem.getDisplacement() != 0;

        if (hasD && !hasBp && !hasIp) {
            // Only displacement
            ops.add("MAR" + ARROW + "IR[0:31]");
        } else if (!hasD && hasBp && !hasIp) {
            // Only base register
            ops.add("MAR" + ARROW + regName64((int) mem.getBase()));
        } else if (hasIp) {
            // Index present — full SIB calculation
            int scaleLog = Integer.numberOfTrailingZeros(mem.getScale());
            ops.add("TEMP2" + ARROW + regName64(mem.getIndex()));
            ops.add("MAR" + ARROW + "SHIFTER_OUT[SHL, " + scaleLog + "]");
            ops.add("TEMP1" + ARROW + "MAR");

            if (hasD) {
                ops.add("TEMP2" + ARROW + "IR[0:31]");
                ops.add("MAR" + ARROW + "ALU_OUT[ADD]");
                ops.add("TEMP1" + ARROW + "MAR");
            }
            if (hasBp) {
                ops.add("TEMP2" + ARROW + regName64((int) mem.getBase()));
                ops.add("MAR" + ARROW + "ALU_OUT[ADD]");
            }
        } else if (hasBp && hasD) {
            // Base + displacement (no index)
            ops.add("MAR" + ARROW + regName64((int) mem.getBase()));
            ops.add("TEMP1" + ARROW + "MAR");
            ops.add("TEMP2" + ARROW + "IR[0:31]");
            ops.add("MAR" + ARROW + "ALU_OUT[ADD]");
        }
    }

    // =====================================================================
    // Class 0: hlt, nop, int
    // =====================================================================

    private static List<String> generateClass0(InstructionClass0 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        if ("hlt".equals(mn)) {
            ops.add("Enter HALT state");
        } else if ("int".equals(mn)) {
            OperandImmediate ivn = insn.getIvn();
            // Disable interrupts
            ops.add("FLAGS[I]" + ARROW + "0; TEMP1" + ARROW + "RSP");
            // Push RIP first (deeper on stack)
            ops.add("MDR" + ARROW + "RIP");
            addStackPush(ops);
            // Push FLAGS second (top of stack)
            ops.add("MDR" + ARROW + "FLAGS");
            addStackPush(ops);
            // IVT lookup: IVN * 8 = IVN << 3
            ops.add("IACK");
            ops.add("IACK; MDR" + ARROW + "IVN");
            ops.add("TEMP2" + ARROW + "MDR");
            ops.add("MAR" + ARROW + "SHIFTER_OUT[SX, 3]");
            addMemoryRead(ops);
            ops.add("RIP" + ARROW + "MDR");
        }
        // nop: fetch only

        return ops;
    }

    // =====================================================================
    // Class 1: mov, movsX, movzX, lea, push, pop, pushf, popf, movs, stos
    // =====================================================================

    private static List<String> generateClass1(InstructionClass1 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        Operand src = insn.getSource();
        Operand dst = insn.getDestination();

        if ("mov".equals(mn)) {
            generateMov(ops, insn, src, dst);
        } else if ("movsX".equals(mn)) {
            generateMovExtend(ops, insn, src, dst, "Sign Extend");
        } else if ("movzX".equals(mn)) {
            generateMovExtend(ops, insn, src, dst, "Zero Extend");
        } else if ("lea".equals(mn)) {
            generateLea(ops, src, dst);
        } else if ("push".equals(mn)) {
            generatePush(ops, insn, src);
        } else if ("pop".equals(mn)) {
            generatePop(ops, dst);
        } else if ("pushf".equals(mn)) {
            ops.add("MDR" + ARROW + "FLAGS");
            addStackPush(ops);
        } else if ("popf".equals(mn)) {
            addStackPop(ops);
            ops.add("FLAGS" + ARROW + "MDR");
        } else if ("movs".equals(mn)) {
            generateMovs(ops);
        } else if ("stos".equals(mn)) {
            generateStos(ops);
        }

        return ops;
    }

    private static void generateMov(List<String> ops, InstructionClass1 insn, Operand src, Operand dst) {
        boolean hasImm = src instanceof OperandImmediate;
        boolean srcIsMem = src instanceof OperandMemory;
        boolean dstIsMem = dst instanceof OperandMemory;

        // 64-bit immediate fetch + address calculation
        addOperandPreamble(ops, insn.getSize(), hasImm, src, dst);

        // Execute
        if (hasImm && dstIsMem) {
            // MDR already holds the 64-bit immediate (or instruction word for 8-byte)
            addMemoryWrite(ops); // (MAR) ← MDR
        } else if (hasImm && !dstIsMem) {
            // Immediate to register
            String dstName = regName((OperandRegister) dst);
            if (insn.getSize() == 16 && !dstIsMem) {
                ops.add(dstName + ARROW + "MDR");
            } else {
                ops.add(dstName + ARROW + "IR[0:31]");
            }
        } else if (srcIsMem) {
            // Memory to register
            addMemoryRead(ops);
            ops.add(regName((OperandRegister) dst) + ARROW + "MDR");
        } else if (dstIsMem) {
            // Register to memory
            ops.add("MDR" + ARROW + regName((OperandRegister) src));
            addMemoryWrite(ops);
        } else {
            // Register to register
            ops.add("TEMP2" + ARROW + regName((OperandRegister) src));
            ops.add(regName((OperandRegister) dst) + ARROW + "SHIFTER_OUT[SHL, 0]");
        }
    }

    private static void generateMovExtend(List<String> ops, InstructionClass1 insn, Operand src, Operand dst, String extOp) {
        boolean hasImm = src instanceof OperandImmediate;
        boolean srcIsMem = src instanceof OperandMemory;
        boolean dstIsMem = dst instanceof OperandMemory;
        int ss = src != null ? Instruction.sizeToSsDs(src.getSize()) : 0;
        int ds = dst != null ? Instruction.sizeToSsDs(dst.getSize()) : 0;
        String extStr = extOp + ", " + ss + ", " + ds;

        // 64-bit immediate fetch
        if (hasImm && insn.getSize() == 16) {
            addImmediateFetch(ops);
            ops.add("TEMP2" + ARROW + "MDR");
            ops.add("MDR" + ARROW + "ALU_OUT[" + extStr + "]");
        }

        // Address calculation
        if (srcIsMem) {
            addAddressCalc(ops, (OperandMemory) src);
        } else if (dstIsMem) {
            addAddressCalc(ops, (OperandMemory) dst);
        }

        // Execute
        if (hasImm && dstIsMem) {
            addMemoryWrite(ops);
        } else if (hasImm && !dstIsMem) {
            ops.add(regName((OperandRegister) dst) + ARROW + "MDR");
        } else if (srcIsMem) {
            addMemoryRead(ops);
            ops.add("TEMP2" + ARROW + "MDR");
            if (dstIsMem) {
                ops.add("MDR" + ARROW + "ALU_OUT[" + extStr + "]");
                addMemoryWrite(ops);
            } else {
                ops.add(regName((OperandRegister) dst) + ARROW + "ALU_OUT[" + extStr + "]");
            }
        } else {
            // Reg to reg
            ops.add("TEMP2" + ARROW + regName((OperandRegister) src));
            if (dstIsMem) {
                ops.add("MDR" + ARROW + "ALU_OUT[" + extStr + "]");
                addMemoryWrite(ops);
            } else {
                ops.add(regName((OperandRegister) dst) + ARROW + "ALU_OUT[" + extStr + "]");
            }
        }
    }

    private static void generateLea(List<String> ops, Operand src, Operand dst) {
        if (src instanceof OperandMemory) {
            addAddressCalc(ops, (OperandMemory) src);
        }
        if (dst instanceof OperandRegister) {
            ops.add(regName((OperandRegister) dst) + ARROW + "MAR");
        }
    }

    private static void generatePush(List<String> ops, InstructionClass1 insn, Operand src) {
        if (src instanceof OperandImmediate) {
            if (insn.getSize() == 16) {
                addImmediateFetch(ops);
            }
        } else if (src instanceof OperandMemory) {
            addAddressCalc(ops, (OperandMemory) src);
            addMemoryRead(ops);
        } else if (src instanceof OperandRegister) {
            ops.add("MDR" + ARROW + regName((OperandRegister) src));
        }
        addStackPush(ops);
    }

    private static void generatePop(List<String> ops, Operand dst) {
        addStackPop(ops);
        if (dst instanceof OperandMemory) {
            // Save popped value, compute address, store
            ops.add("TEMP1" + ARROW + "MDR");
            addAddressCalc(ops, (OperandMemory) dst);
            ops.add("MDR" + ARROW + "TEMP1");
            addMemoryWrite(ops);
        } else if (dst instanceof OperandRegister) {
            ops.add(regName((OperandRegister) dst) + ARROW + "MDR");
        }
    }

    private static void generateMovs(List<String> ops) {
        ops.add("while RCX != 0");
        ops.add("    MAR" + ARROW + "RSI");
        ops.add("    MDR" + ARROW + "(MAR)");
        ops.add("    MAR" + ARROW + "RDI");
        ops.add("    (MAR)" + ARROW + "MDR");
        addRepDecrement(ops, "    ");
        addDirectionAdvance(ops, "RSI", "    ", 1);
        addDirectionAdvance(ops, "RDI", "    ", 1);
        ops.add("end while");
    }

    private static void generateStos(List<String> ops) {
        ops.add("MDR" + ARROW + "RAX");
        ops.add("while RCX != 0");
        ops.add("    MAR" + ARROW + "RDI");
        ops.add("    (MAR)" + ARROW + "MDR");
        addRepDecrement(ops, "    ");
        addDirectionAdvance(ops, "RDI", "    ", 1);
        ops.add("end while");
    }

    // =====================================================================
    // Class 2: ALU operations
    // =====================================================================

    private static List<String> generateClass2(InstructionClass2 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        Operand src = insn.getSource();
        Operand dst = insn.getDestination();

        // Determine the ALU operation name
        String aluOp = getClass2AluOp(mn);

        // Unary operations: neg, not
        switch (mn) {
            case "neg":
            case "not":
                generateUnary(ops, dst, aluOp);
                return ops;

            // bt (bit test)
            case "bt":
                generateBitTest(ops, src, dst);
                return ops;
        }

        // Discard result for cmp and test
        boolean discardResult = "cmp".equals(mn) || "test".equals(mn);

        // Standard binary operation
        generateBinaryAlu(ops, insn, src, dst, aluOp, discardResult);
        return ops;
    }

    private static String getClass2AluOp(String mn) {
        switch (mn) {
            case "add": return "ADD";
            case "sub": case "cmp": return "SUB";
            case "adc": return "ADC";
            case "sbb": return "SBB";
            case "test": case "and": return "AND";
            case "or": return "OR";
            case "xor": return "XOR";
            case "neg": return "NEG";
            case "not": return "NOT";
            default: return mn.toUpperCase();
        }
    }

    private static void generateBinaryAlu(List<String> ops, InstructionClass2 insn,
                                           Operand src, Operand dst, String aluOp, boolean discardResult) {
        boolean hasImm = src instanceof OperandImmediate;
        boolean srcIsMem = src instanceof OperandMemory;
        boolean dstIsMem = dst instanceof OperandMemory;

        // 64-bit immediate fetch + address calculation
        addOperandPreamble(ops, insn.getSize(), hasImm, src, dst);

        // Execute phase
        if (hasImm && dstIsMem) {
            // Immediate + memory destination: MDR holds the 64-bit immediate
            ops.add("TEMP1" + ARROW + "MDR");
            addMemoryRead(ops);
            ops.add("TEMP2" + ARROW + "MDR");
            if (discardResult) {
                ops.add("ALU_OUT[" + aluOp + "]");
            } else {
                ops.add("MDR" + ARROW + "ALU_OUT[" + aluOp + "]");
                addMemoryWrite(ops);
            }
        } else if (hasImm && !dstIsMem) {
            // Immediate + register destination
            ops.add("TEMP1" + ARROW + "MDR");
            ops.add("TEMP2" + ARROW + regName((OperandRegister) dst));
            if (discardResult) {
                ops.add("ALU_OUT[" + aluOp + "]");
            } else {
                ops.add(regName((OperandRegister) dst) + ARROW + "ALU_OUT[" + aluOp + "]");
            }
        } else if (srcIsMem) {
            // Memory source + register destination (Mem=10)
            addMemoryRead(ops);
            ops.add("TEMP1" + ARROW + "MDR");
            ops.add("TEMP2" + ARROW + regName((OperandRegister) dst));
            if (discardResult) {
                ops.add("ALU_OUT[" + aluOp + "]");
            } else {
                ops.add(regName((OperandRegister) dst) + ARROW + "ALU_OUT[" + aluOp + "]");
            }
        } else if (dstIsMem) {
            // Register source + memory destination (Mem=01)
            ops.add("TEMP1" + ARROW + regName((OperandRegister) src));
            addMemoryRead(ops);
            ops.add("TEMP2" + ARROW + "MDR");
            if (discardResult) {
                ops.add("ALU_OUT[" + aluOp + "]");
            } else {
                ops.add("MDR" + ARROW + "ALU_OUT[" + aluOp + "]");
                addMemoryWrite(ops);
            }
        } else {
            // Register to register
            ops.add("TEMP1" + ARROW + regName((OperandRegister) src));
            ops.add("TEMP2" + ARROW + regName((OperandRegister) dst));
            if (discardResult) {
                ops.add("ALU_OUT[" + aluOp + "]");
            } else {
                ops.add(regName((OperandRegister) dst) + ARROW + "ALU_OUT[" + aluOp + "]");
            }
        }
    }

    private static void generateUnary(List<String> ops, Operand dst, String aluOp) {
        if (dst instanceof OperandMemory) {
            addAddressCalc(ops, (OperandMemory) dst);
            addMemoryRead(ops);
            ops.add("TEMP2" + ARROW + "MDR");
            ops.add("MDR" + ARROW + "ALU_OUT[" + aluOp + "]");
            addMemoryWrite(ops);
        } else if (dst instanceof OperandRegister) {
            String name = regName((OperandRegister) dst);
            ops.add("TEMP2" + ARROW + name);
            ops.add(name + ARROW + "ALU_OUT[" + aluOp + "]");
        }
    }

    private static void generateBitTest(List<String> ops, Operand src, Operand dst) {
        // dst = bit base (register or memory), src = bit offset (immediate)
        if (dst instanceof OperandMemory) {
            addAddressCalc(ops, (OperandMemory) dst);
            addMemoryRead(ops);
            ops.add("TEMP1" + ARROW + "MDR");
        } else if (dst instanceof OperandRegister) {
            ops.add("TEMP1" + ARROW + regName((OperandRegister) dst));
        }
        addImmediateFetch(ops);
        ops.add("TEMP2" + ARROW + "MDR");
        ops.add("ALU_OUT[BIT_TEST]");
    }

        // =====================================================================
    // Class 3: Shift and Rotate
    // =====================================================================

    private static List<String> generateClass3(InstructionClass3 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String regN = regName(insn.getReg());
        String mn = insn.getMnemonic();

        String shiftOp = mn.toUpperCase();;
        if(shiftOp.equals("SAL"))
            shiftOp = "SHL";

        ops.add("TEMP2" + ARROW + regN);
        ops.add(regN + ARROW + "SHIFTER_OUT[" + shiftOp + ", " + insn.getPlaces() + "]");

        return ops;
    }

    // =====================================================================
    // Class 4: Flag manipulation
    // =====================================================================

    private static List<String> generateClass4(InstructionClass4 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        boolean isClear = mn.startsWith("cl");
        String flagCode = mn.substring(2); // "c", "p", "z", "s", "i", "d", "o"
        String flagName = flagCodeToName(flagCode);
        String value = isClear ? "0" : "1";

        ops.add("FLAGS[" + flagName + "]" + ARROW + value);

        return ops;
    }

    private static String flagCodeToName(String code) {
        switch (code) {
            case "c": return "CF";
            case "p": return "PF";
            case "z": return "ZF";
            case "s": return "SF";
            case "i": return "IF";
            case "d": return "DF";
            case "o": return "OF";
            default: return code.toUpperCase() + "F";
        }
    }

    // =====================================================================
    // Class 5: jmp, call, ret, iret
    // =====================================================================

    private static List<String> generateClass5(InstructionClass5 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        Operand target = insn.getTarget();

        if ("jmp".equals(mn)) {
            if (target instanceof OperandMemory) {
                addRelativeJump(ops);
            } else if (target instanceof OperandRegister) {
                // Absolute jump to register
                ops.add("RIP" + ARROW + regName((OperandRegister) target));
            }
        } else if ("call".equals(mn)) {
            // Push return address
            ops.add("MDR" + ARROW + "RIP");
            addStackPush(ops);
            // Jump
            if (target instanceof OperandMemory) {
                addRelativeJump(ops);
            } else if (target instanceof OperandRegister) {
                ops.add("RIP" + ARROW + regName((OperandRegister) target));
            }
        } else if ("ret".equals(mn)) {
            addStackPop(ops);
            ops.add("RIP" + ARROW + "MDR");
        } else if ("iret".equals(mn)) {
            // Pop FLAGS (top of stack — pushed last by int)
            addStackPop(ops);
            ops.add("FLAGS" + ARROW + "MDR");
            // Pop RIP (deeper — pushed first by int)
            addStackPop(ops);
            ops.add("RIP" + ARROW + "MDR");
            // Re-enable interrupts (pushed FLAGS had IF=0)
            ops.add("FLAGS[I]" + ARROW + "1");
        }

        return ops;
    }

    // =====================================================================
    // Class 6: Conditional jumps
    // =====================================================================

    private static List<String> generateClass6(InstructionClass6 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        boolean isNegated = mn.startsWith("jn");
        String flagCode = isNegated ? mn.substring(2) : mn.substring(1);
        String flagName = flagCodeToName(flagCode);
        String condition = isNegated ? "0" : "1";

        ops.add("if FLAGS[" + flagName + "] == " + condition);
        addRelativeJump(ops, "    ");
        ops.add("endif");

        return ops;
    }

    // =====================================================================
    // Class 7: I/O
    // =====================================================================

    private static List<String> generateClass7(InstructionClass7 insn) {
        List<String> ops = new ArrayList<>();
        addFetch(ops);

        String mn = insn.getMnemonic();
        boolean explicitPort = insn.getIoPort() instanceof OperandImmediate;
        String accReg = sizedAccumulator(insn.getTransferSize());

        if ("in".equals(mn)) {
            // Resolve port address into MAR
            if (explicitPort) {
                ops.add("MAR" + ARROW + "IR[I/O Port]");
            } else {
                ops.add("MAR" + ARROW + "DX");
            }
            // I/O read bus cycle
            ops.add("MDR" + ARROW + "IO(MAR)");
            // Transfer to accumulator
            ops.add(accReg + ARROW + "MDR");
        } else if ("out".equals(mn)) {
            // Load accumulator into MDR
            ops.add("MDR" + ARROW + accReg);
            // Resolve port address into MAR
            if (explicitPort) {
                ops.add("MAR" + ARROW + "IR[I/O Port]");
            } else {
                ops.add("MAR" + ARROW + "DX");
            }
            // I/O write bus cycle
            ops.add("IO(MAR)" + ARROW + "MDR");
        } else if ("ins".equals(mn)) {
            // String input: device port in DX → memory at RDI, RCX times
            ops.add("while RCX != 0");
            ops.add("    MAR" + ARROW + "DX");
            ops.add("    MDR" + ARROW + "IO(MAR)");
            ops.add("    MAR" + ARROW + "RDI");
            addIndentedMemoryWrite(ops);
            // Decrement RCX
            addRepDecrement(ops, "    ");
            addDirectionAdvance(ops, "RDI", "    ", insn.getTransferSize());
            ops.add("end while");
        } else if ("outs".equals(mn)) {
            // String output: memory at RSI → device port in DX, RCX times
            ops.add("while RCX != 0");
            ops.add("    MAR" + ARROW + "RSI");
            addIndentedMemoryRead(ops);
            ops.add("    MAR" + ARROW + "DX");
            ops.add("    IO(MAR)" + ARROW + "MDR");
            // Decrement RCX
            addRepDecrement(ops, "    ");
            addDirectionAdvance(ops, "RSI", "    ", insn.getTransferSize());
            ops.add("end while");
        }

        return ops;
    }

    /** Get the accumulator register name for the given transfer size */
    private static String sizedAccumulator(int size) {
        switch (size) {
            case 1: return "AL";
            case 2: return "AX";
            case 4: return "EAX";
            default: return "RAX";
        }
    }

    /** Memory read with indentation (for loops) */
    private static void addIndentedMemoryRead(List<String> ops) {
        ops.add("    MDR" + ARROW + "(MAR)");
    }

    /** Memory write with indentation (for loops) */
    private static void addIndentedMemoryWrite(List<String> ops) {
        ops.add("    (MAR)" + ARROW + "MDR");
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /** Get the 64-bit (quadword) name of a register by code */
    private static String regName64(int code) {
        return stripPercent(Register.getRegisterName(code, 8));
    }

    /** Get the sized name of a register operand */
    private static String regName(OperandRegister reg) {
        return stripPercent(Register.getRegisterName(reg.getRegister(), reg.getSize()));
    }

    /** Strip '%' prefix and uppercase: "%rax" → "RAX" */
    private static String stripPercent(String name) {
        if (name.startsWith("%")) {
            name = name.substring(1);
        }
        return name.toUpperCase();
    }
}
