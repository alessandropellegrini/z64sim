/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps µ-op strings to SVG group IDs and control signals.
 * <p>
 * Each overlay name is an SVG element ID (e.g. "reg-read", "data-bus", "alu")
 * that will be highlighted (coloured red) in the architecture diagram.
 */
public class MuOpOverlayMapper {
    public static class MuOpMapping {
        public final List<String> overlays;
        public final String signals;
        public MuOpMapping(List<String> overlays, String signals) {
            this.overlays = overlays;
            this.signals = signals;
        }
    }

    public static MuOpMapping map(String muOpOrig) {
        String muOp = muOpOrig.trim();
        String signals = "";
        
        if (muOp.isEmpty() || muOp.startsWith("if ") || muOp.startsWith("else") ||
            muOp.startsWith("endif") || muOp.startsWith("while ") || muOp.startsWith("end while")) {
            return new MuOpMapping(Collections.emptyList(), "");
        }
        
        List<String> ov = new ArrayList<>();
        
        if (muOp.equals("MAR \u2190 RIP")) {
            ov.addAll(Arrays.asList("rip-read", "mar-write", "data-bus"));
            signals = "B_RIP = 1, W_MAR = 1";
        }
        // Fetch memory read: 3 clock cycles
        else if (muOp.equals("MDR \u2190 (MAR); RIP \u2190 RIP + 8 [1/3]")) {
            ov.addAll(Arrays.asList("mar-read", "rip-inc"));
            signals = "INC_RIP = 1, B_AB = 1";
        } else if (muOp.equals("MDR \u2190 (MAR); RIP \u2190 RIP + 8 [2/3]")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "control-bus"));
            signals = "B_AB = 1, MRD = 1";
        } else if (muOp.equals("MDR \u2190 (MAR); RIP \u2190 RIP + 8 [3/3]")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "control-bus", "mdr-write-outside", "mdr-write", "mdr-outside"));
            signals = "B_AB = 1, MRD = 1, B_DBIN = 1, W_MDR = 1";
        }
        // Legacy single-frame (in case it's called without expansion)
        else if (muOp.equals("MDR \u2190 (MAR); RIP \u2190 RIP + 8")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "control-bus", "mdr-write-outside", "mdr-write", "mdr-outside", "rip-inc"));
            signals = "INC_RIP = 1, B_AB = 1, MRD = 1, B_DBIN = 1, W_MDR = 1";
        } else if (muOp.equals("IR \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-inside", "mdr-read", "ir-write", "data-bus"));
            signals = "W_IR = 1, B_MDROUT = 1";
        }
        // Memory read: 3 clock cycles
        else if (muOp.equals("MDR \u2190 (MAR) [1/3]")) {
            ov.addAll(Arrays.asList("mar-read"));
            signals = "B_AB = 1";
        } else if (muOp.equals("MDR \u2190 (MAR) [2/3]")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "control-bus"));
            signals = "B_AB = 1, MRD = 1";
        } else if (muOp.equals("MDR \u2190 (MAR) [3/3]")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "control-bus", "mdr-write-outside", "mdr-write", "mdr-outside"));
            signals = "B_AB = 1, MRD = 1, B_DBIN = 1, W_MDR = 1";
        }
        // Legacy single-frame
        else if (muOp.equals("MDR \u2190 (MAR)")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "control-bus", "mdr-write-outside", "mdr-write", "mdr-outside"));
            signals = "B_AB = 1, MRD = 1, B_DBIN = 1, W_MDR = 1";
        }
        // Memory write: 3 clock cycles
        else if (muOp.equals("(MAR) \u2190 MDR [1/3]")) {
            ov.addAll(Arrays.asList("mar-read"));
            signals = "B_AB = 1";
        } else if (muOp.equals("(MAR) \u2190 MDR [2/3]")) {
            ov.addAll(Arrays.asList("mar-read", "wr", "control-bus"));
            signals = "B_AB = 1, MWR = 1";
        } else if (muOp.equals("(MAR) \u2190 MDR [3/3]")) {
            ov.addAll(Arrays.asList("mdr-read-outside", "mdr-read", "mdr-outside", "wr", "control-bus", "mar-read"));
            signals = "B_AB = 1, MWR = 1, B_DBOUT = 1";
        }
        // Legacy single-frame
        else if (muOp.equals("(MAR) \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-outside", "mdr-read", "mdr-outside", "wr", "control-bus", "mar-read"));
            signals = "B_AB = 1, MWR = 1, B_DBOUT = 1";
        }
        // I/O
        else if (muOp.equals("MDR \u2190 IO(MAR)")) {
            ov.addAll(Arrays.asList("mar-read", "rd", "io", "control-bus", "mdr-write-outside", "mdr-write", "mdr-outside"));
            signals = "B_AB = 1, IORD = 1, B_DBIN = 1, W_MDR = 1";
        } else if (muOp.equals("IO(MAR) \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-outside", "mdr-read", "mdr-outside", "wr", "io", "control-bus", "mar-read"));
            signals = "B_AB = 1, IOWR = 1, B_DBOUT = 1";
        }
        // FLAGS
        else if (muOp.equals("FLAGS[I] \u2190 0; TEMP1 \u2190 RSP")) {
            ov.addAll(Arrays.asList("flags-write", "reg-read", "t1-write", "data-bus"));
            signals = "W_FLAGS = 1, R_M = 1, W_T1 = 1";
        } else if (muOp.startsWith("FLAGS") && (muOp.endsWith(" \u2190 0") || muOp.endsWith(" \u2190 1"))) {
            ov.addAll(Arrays.asList("flags-write"));
            signals = "W_FLAGS = 1";
        } else if (muOp.equals("MDR \u2190 FLAGS")) {
            ov.addAll(Arrays.asList("flags-read", "alu-shifter-flags-bus", "mdr-write-inside", "mdr-write", "data-bus"));
            signals = "B_FLAGSDB = 1, B_MDRIN = 1, W_MDR = 1";
        } else if (muOp.equals("FLAGS \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-inside", "mdr-read", "flags-from-db", "alu-shifter-flags-bus", "flags-write", "data-bus"));
            signals = "B_MDROUT = 1, B_FLAGSDB = 1, W_FLAGS = 1";
        }
        // RIP
        else if (muOp.equals("RIP \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-inside", "mdr-read", "rip-write", "data-bus"));
            signals = "B_MDROUT = 1, W_RIP = 1";
        } else if (muOp.equals("MDR \u2190 RIP")) {
            ov.addAll(Arrays.asList("rip-read", "mdr-write-inside", "mdr-write", "data-bus"));
            signals = "B_RIP = 1, B_MDRIN = 1, W_MDR = 1";
        } else if (muOp.equals("RIP \u2190 ALU_OUT[ADD]")) {
            ov.addAll(Arrays.asList("alu", "alu-out", "alu-shifter-flags-bus", "rip-write", "data-bus"));
            signals = "A_opcode = 0000, B_A = 1, W_RIP = 1";
        } else if (muOp.matches("RIP \u2190 ([A-Z0-9]+)")) {
            String reg = muOp.substring(6);
            addRegRead(ov, reg);
            ov.addAll(Arrays.asList("rip-write", "data-bus"));
            signals = "R_M = 1, W_RIP = 1";
        }
        // MAR
        else if (muOp.equals("MAR \u2190 RSP") || muOp.equals("MAR \u2190 DX")) {
            addRegRead(ov, muOp.substring(6));
            ov.addAll(Arrays.asList("mar-write", "regmux", "data-bus"));
            signals = "R_M = 1, W_MAR = 1";
        } else if (muOp.equals("MAR \u2190 IR[I/O Port]") || muOp.equals("MAR \u2190 IR[0:31]")) {
            ov.addAll(Arrays.asList("ir-short", "mar-write"));
            signals = "B_SHORT = 1, W_MAR = 1";
        }
        // Special
        else if (muOp.equals("IACK")) {
            ov.addAll(Arrays.asList("iack", "control-bus"));
            signals = "IACK = 1";
        } else if (muOp.equals("IACK; MDR \u2190 IVN")) {
            ov.addAll(Arrays.asList("iack", "control-bus", "mdr-write-outside", "mdr-write", "mdr-outside"));
            signals = "IACK = 1";
        } else if (muOp.equals("Enter HALT state")) {
            // text only
        }
        // ALUs
        else if (muOp.startsWith("MAR \u2190 ALU_OUT[")) {
            String op = extractOp(muOp);
            ov.addAll(Arrays.asList("alu", "alu-out", "alu-shifter-flags-bus", "mar-write", "data-bus"));
            signals = "A_opcode = " + op + ", B_A = 1, W_MAR = 1";
        } else if (muOp.startsWith("RSP \u2190 ALU_OUT[")) {
            String op = extractOp(muOp);
            ov.addAll(Arrays.asList("alu", "alu-out", "alu-shifter-flags-bus", "reg-write", "data-bus"));
            signals = "A_opcode = " + op + ", B_A = 1, W_RSP = 1";
        } else if (muOp.startsWith("ALU_OUT[BIT_TEST]")) {
            ov.addAll(Arrays.asList("alu", "flags-from-alu"));
            signals = "A_opcode = BIT_TEST, B_FLAGSA = 1, W_FLAGS = 1";
        } else if (muOp.startsWith("RAX \u2190 ALU_OUT[") || muOp.startsWith("RDX \u2190 ALU_OUT[")) {
            String op = extractOp(muOp);
            ov.addAll(Arrays.asList("alu", "alu-out", "alu-shifter-flags-bus", "reg-write", "data-bus"));
            signals = "A_opcode = " + op;
        }
        else if (muOp.contains("\u2190 ALU_OUT[")) {
            Matcher m = Pattern.compile("([A-Z0-9]+) \u2190 ALU_OUT\\[(.*?)\\]").matcher(muOp);
            if (m.find()) {
                String op = m.group(2);
                ov.addAll(Arrays.asList("alu", "alu-out", "alu-shifter-flags-bus", "flags-from-alu", "data-bus"));
                ov.addAll(Arrays.asList("reg-write", "smux", "regmux"));
                signals = "A_opcode = " + op + ", B_A = 1, S_MUX = 2, REG_MUX = 1, W_M = 1, B_FLAGSA = 1, W_FLAGS = 1";
            }
        }
        // Shifter
        else if (muOp.contains("\u2190 SHIFTER_OUT[")) {
            Matcher m = Pattern.compile("([A-Z0-9]+) \u2190 SHIFTER_OUT\\[(.*?)\\]").matcher(muOp);
            if (m.find()) {
                String dest = m.group(1);
                String op = m.group(2);
                ov.addAll(Arrays.asList("shifter", "shifter-out", "alu-shifter-flags-bus", "data-bus"));
                if (dest.equals("TEMP1")) {
                    ov.add("t1-write");
                    signals = "S_opcode = " + op + ", B_S = 1, W_T1 = 1";
                } else if (dest.equals("MAR")) {
                    ov.add("mar-write");
                    signals = "S_opcode = " + op + ", B_S = 1, W_MAR = 1";
                } else {
                    ov.add("flags-from-shifter");
                    ov.addAll(Arrays.asList("reg-write", "smux", "regmux"));
                    signals = "S_opcode = " + op + ", B_S = 1, S_MUX = 2, REG_MUX = 1, W_M = 1, B_FLAGSS = 1, W_FLAGS = 1";
                }
            }
        }
        // TEMP/immediate
        else if (muOp.equals("TEMP1 \u2190 IR[0:31]")) {
            ov.addAll(Arrays.asList("ir-short", "t1-write", "data-bus"));
            signals = "B_SHORT = 1, W_T1 = 1";
        } else if (muOp.equals("TEMP2 \u2190 IR[0:31]")) {
            ov.addAll(Arrays.asList("ir-short", "t2-write", "data-bus"));
            signals = "B_SHORT = 1, W_T2 = 1";
        } else if (muOp.equals("TEMP1 \u2190 MAR")) {
            ov.addAll(Arrays.asList("mar-read-inside", "t1-write", "data-bus"));
            signals = "B_MAROUT = 1, W_T1 = 1";
        } else if (muOp.equals("TEMP2 \u2190 MAR")) {
            ov.addAll(Arrays.asList("mar-read-inside", "t2-write", "data-bus"));
            signals = "B_MAROUT = 1, W_T2 = 1";
        } else if (muOp.equals("TEMP1 \u2190 RIP")) {
            ov.addAll(Arrays.asList("rip-read", "t1-write", "data-bus"));
            signals = "B_RIP = 1, W_T1 = 1";
        } else if (muOp.matches("TEMP2 \u2190 -?\\d+")) {
            ov.addAll(Arrays.asList("t2-write"));
            signals = "W_T2 = 1";
        }
        // Register Operations
        else if (muOp.equals("TEMP1 \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-inside", "mdr-read", "t1-write", "data-bus"));
            signals = "B_MDROUT = 1, W_T1 = 1";
        } else if (muOp.equals("TEMP2 \u2190 MDR")) {
            ov.addAll(Arrays.asList("mdr-read-inside", "mdr-read", "t2-write", "data-bus"));
            signals = "B_MDROUT = 1, W_T2 = 1";
        } else if (muOp.startsWith("TEMP1 \u2190 ")) {
            String reg = muOp.substring(8);
            ov.addAll(Arrays.asList("t1-write", "data-bus"));
            addRegRead(ov, reg);
            ov.addAll(Arrays.asList("smux", "regmux"));
            signals = "S_MUX = 0, REG_MUX = 0, R_M = 1, W_T1 = 1";
        } else if (muOp.startsWith("TEMP2 \u2190 ")) {
            String reg = muOp.substring(8);
            ov.addAll(Arrays.asList("t2-write", "data-bus"));
            addRegRead(ov, reg);
            ov.addAll(Arrays.asList("smux", "regmux"));
            signals = "S_MUX = 0, REG_MUX = 0, R_M = 1, W_T2 = 1";
        } else if (muOp.startsWith("MDR \u2190 ")) { // MDR <- <reg>
            String reg = muOp.substring(6);
            addRegRead(ov, reg);
            ov.addAll(Arrays.asList("mdr-write-inside", "mdr-write", "smux", "regmux", "data-bus"));
            signals = "S_MUX = 1, REG_MUX = 0, R_M = 1, B_MDRIN = 1, W_MDR = 1";
        } else if (muOp.startsWith("MAR \u2190 ")) { // MAR <- <reg>
            String reg = muOp.substring(6);
            addRegRead(ov, reg);
            ov.addAll(Arrays.asList("mar-write", "smux", "regmux", "data-bus"));
            signals = "R_M = 1, W_MAR = 1";
        } else if (muOp.endsWith(" \u2190 MDR")) { // <reg> <- MDR
            String reg = muOp.substring(0, muOp.indexOf(" \u2190"));
            ov.addAll(Arrays.asList("mdr-read-inside", "mdr-read", "data-bus"));
            addRegWrite(ov, reg);
            ov.addAll(Arrays.asList("smux", "regmux"));
            signals = "S_MUX = 2, REG_MUX = 1, W_M = 1, B_MDROUT = 1";
        } else if (muOp.endsWith(" \u2190 IR[0:31]")) { // <reg> <- IR[0:31]
            String reg = muOp.substring(0, muOp.indexOf(" \u2190"));
            ov.addAll(Arrays.asList("ir-short", "data-bus"));
            addRegWrite(ov, reg);
            ov.addAll(Arrays.asList("smux", "regmux"));
            signals = "S_MUX = 2, REG_MUX = 1, W_M = 1, B_SHORT = 1";
        }
        
        return new MuOpMapping(ov, signals);
    }
    
    private static String extractOp(String muOp) {
        int start = muOp.indexOf('[');
        int end = muOp.indexOf(']');
        if (start != -1 && end != -1) {
            return muOp.substring(start + 1, end);
        }
        return "";
    }
    
    /**
     * Add a register-bank read overlay.
     * All GPRs map to the unified "reg-read" group.
     */
    private static void addRegRead(List<String> ov, String reg) {
        if (isGPR(reg)) {
            ov.add("reg-read");
        }
    }
    
    /**
     * Add a register-bank write overlay.
     * All GPRs map to the unified "reg-write" group.
     */
    private static void addRegWrite(List<String> ov, String reg) {
        if (isGPR(reg)) {
            ov.add("reg-write");
        }
    }
    
    /** Check whether a register name is a general-purpose register */
    private static boolean isGPR(String reg) {
        reg = reg.toUpperCase();
        // Classic 8 GPRs and all their sub-register names
        if (reg.matches("R[ABCD]X|E[ABCD]X|[ABCD][XHL]")) return true;
        if (reg.matches("RSP|ESP|SP|SPL")) return true;
        if (reg.matches("RBP|EBP|BP|BPL")) return true;
        if (reg.matches("RSI|ESI|SI|SIL")) return true;
        if (reg.matches("RDI|EDI|DI|DIL")) return true;
        // Extended registers R8–R15
        if (reg.matches("R(8|9|1[0-5])[DWB]?")) return true;
        return false;
    }
}
