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