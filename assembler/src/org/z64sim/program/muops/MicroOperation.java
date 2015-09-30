/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// TODO: this should be rather moved to the simulator package, when implemented
package org.z64sim.program.muops;
import org.z64sim.program.instructions.*;
import java.util.ArrayList;

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
    
    
    private final int source;
    private final int destination;
    private final int acc1;
    private final int acc2;
    
    public MicroOperation(int s, int d){
        this.source = s;
        this.destination = d;
        this.acc1 = -1;
        this.acc2 = -1;
    }
    
    public MicroOperation(int s, int d, int a1){
        this.source = s;
        this.destination = d;
        this.acc1 = a1;
        this.acc2 = -1;
    }
    
    public MicroOperation(int s, int d, int a1, int a2){
        this.source = s;
        this.destination = d;
        this.acc1 = a1;
        this.acc2 = a2;
    }
    
    public int getSource(){
        return source;
    }
    
    public int getDestination(){
        return destination;
    }
    
    public int getAcc1(){
        return acc1;
    }
    
    public int getAcc2(){
        return acc2;
    }
    
    
}