/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.simulator;

import org.z64sim.simulator.multicycle.SimulatorMulticycleTopComponent;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Simulator {
    
    private static SimulatorMulticycleTopComponent multicycleWindow = null;
    
    private Simulator() {}
    
    public static synchronized void setMulticycle(SimulatorMulticycleTopComponent mc)  {
        Simulator.multicycleWindow = mc;
    }
    
    public static synchronized void resetMulticycle(SimulatorMulticycleTopComponent mc)  {
        if(Simulator.multicycleWindow == mc)
            Simulator.multicycleWindow = null;
    }
    
    public static boolean activate() {
        if(multicycleWindow != null) {
            multicycleWindow.requestFocusInWindow();
            multicycleWindow.checkEnabled();
            return true;
        }
        return false;
    }
    
}
