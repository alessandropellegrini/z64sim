/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.program;

/**
 * The purpose of this class is just to allow for a different representation
 * of strings in the GUI. A string is nevertheless encoded exactly as a DataElement,
 * namely as a byte vector.
 * 
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class StringDataElement extends DataElement {

    public StringDataElement(byte[] value) {
        super(value);
    }
}
