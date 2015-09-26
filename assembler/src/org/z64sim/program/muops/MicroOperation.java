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
    private final int source;
    private final int destination;
    private final static int TEMP1;
    private final static int TEMP2;
    
    
    public MicroOperation(String s, String d){
        this.source = s;
        this.destination = d;
        this.acc1 = "";
        this.acc2 = "";
    }
    
    public MicroOperation(String s, String d, String a1){
        this.source = s;
        this.destination = d;
        this.acc1 = a1;
        this.acc2 = "";
    }
    
    public MicroOperation(String s, String d, String a1, String a2){
        this.source = s;
        this.destination = d;
        this.acc1 = a1;
        this.acc2 = a2;
    }
    
    
}