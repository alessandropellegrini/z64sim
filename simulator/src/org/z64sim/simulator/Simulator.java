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

    // Clicking 'run' on a Simulator TopComponent sets one of these
    // variables. They are in turn used by rnu() methods of instructions
    // to use/alter the values of the registers.
    public static SimulatorMulticycleTopComponent multicycle = null;

    private Simulator() {}

}
