/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program.instructions;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public abstract class Operand {
    public abstract int getIndex();
    public abstract int getDisplacement();
    public abstract int getScale();
    public abstract int getBase();
}
