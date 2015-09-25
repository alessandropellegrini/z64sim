/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// TODO: this should be rather moved to the simulator package, when implemented
package org.z64sim.program.muops;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class MicroOperation {

    public static final int O = 1;
    public static final int B = 2;
    public static final int I = 3;
    public static final int T = 4;
    public static final int TEMP1 = 5;
    public static final int TEMP2 = 6;
    public static final int EMAR = 7;
    public static final int EMARm = 8;
    public static final int SR_UPDATE1 = 9;
    public static final int EMDR = 10;
    public static final int IR031 = 11;
    public static final int SHIFTER_OUT_SX_0 = 12;
    public static final int SHIFTER_OUT_SX_T = 13;
    public static final int SR_UPDATE0 = 14;
    public static final int RIP = 15;
    public static final int IR = 16;
    public static final int RIP8 = 17;
    public static final int ALU_OUT_ADD = 18;
    public static final int S = 19;
    public static final int D = 20;
    public static final int RSP = 21;
    public static final int ALU_OUT_SUB_X = 22;
    public static final int ALU_OUT_ADD_X = 23;
    public static final int FLAGS = 24;
    public static final int RSI = 25;
    public static final int RDI = 26;
    public static final int RAX = 27;
    public static final int ALU_OUT_SUB = 28;
    public static final int ALU_OUT_ADC = 29;
    public static final int ALU_OUT_SBB = 30;
    public static final int TEMP3 = 31;
    public static final int ALU_OUT_AND = 32;
    public static final int ALU_OUT_NEG = 33;
    public static final int ALU_OUT_OR = 34;
    public static final int ALU_OUT_XOR = 35;
    public static final int ALU_OUT_NOT = 36;
    public static final int SHIFTER_OUT_SAL_K = 37;
    public static final int SHIFTER_OUT_SAL_RCX = 38;
    public static final int SHIFTER_OUT_SHL_K = 39;
    public static final int SHIFTER_OUT_SHL_RCX = 40;
    public static final int SHIFTER_OUT_SAR_K = 41;
    public static final int SHIFTER_OUT_SAR_RCX = 42;
    public static final int SHIFTER_OUT_SHR_K = 43;
    public static final int SHIFTER_OUT_SHR_RCX = 44;
    public static final int SHIFTER_OUT_RCL_K = 45;
    public static final int SHIFTER_OUT_RCL_RCX = 46;
    public static final int SHIFTER_OUT_RCR_K = 47;
    public static final int SHIFTER_OUT_RCR_RCX = 48;
    public static final int SHIFTER_OUT_ROL_K = 49;
    public static final int SHIFTER_OUT_ROL_RCX = 50;
    public static final int SHIFTER_OUT_ROR_K = 51;
    public static final int SHIFTER_OUT_ROR_RCX = 52;
    public static final int FLAGS_CF_S = 53;
    public static final int FLAGS_CF_R = 54;
    public static final int FLAGS_PF_S = 55;
    public static final int FLAGS_PF_R = 56;
    public static final int FLAGS_ZF_S = 57;
    public static final int FLAGS_ZF_R = 58;
    public static final int FLAGS_SF_S = 59;
    public static final int FLAGS_SF_R = 60;
    public static final int FLAGS_IF_S = 61;
    public static final int FLAGS_IF_R = 62;
    public static final int FLAGS_DF_S = 63;
    public static final int FLAGS_DF_R = 64;
    public static final int FLAGS_OF_S = 65;
    public static final int FLAGS_OF_R = 66;
    public static final int R = 67;
    public static final int ALU_OUT_SUB_8 = 68;
    public static final int M = 69;
    public static final int ALU_OUT_ADD_8 = 70;
    public static final int FLAGS_CF_1 = 71;
    public static final int FLAGS_PF_1 = 72;
    public static final int FLAGS_ZF_1 = 73;
    public static final int FLAGS_SF_1 = 74;
    public static final int FLAGS_OF_1 = 75;
    public static final int FLAGS_CF_0 = 76;
    public static final int FLAGS_PF_0 = 77;
    public static final int FLAGS_ZF_0 = 78;
    public static final int FLAGS_SF_0 = 79;
    public static final int FLAGS_OF_0 = 80;

    private final int source;
    private final int destination;
    private final int acc1;
    private final int acc2;

    public MicroOperation(int s) {
        this.source = s;
        this.destination = -1;
        this.acc1 = -1;
        this.acc2 = -1;
    }

    public MicroOperation(int s, int d) {
        this.source = s;
        this.destination = d;
        this.acc1 = -1;
        this.acc2 = -1;
    }

    public MicroOperation(int s, int d, int a1) {
        this.source = s;
        this.destination = d;
        this.acc1 = a1;
        this.acc2 = -1;
    }

    public MicroOperation(int s, int d, int a1, int a2) {
        this.source = s;
        this.destination = d;
        this.acc1 = a1;
        this.acc2 = a2;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public int getAcc1() {
        return acc1;
    }

    public int getAcc2() {
        return acc2;
    }

}
