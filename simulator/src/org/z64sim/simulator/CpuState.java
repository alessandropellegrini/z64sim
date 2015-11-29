/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.simulator;

import java.beans.*;
import java.io.Serializable;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class CpuState implements Serializable {

    // Properties
    public static final String PROP_RAX = "RAX";
    public static final String PROP_RCX = "RCX";
    public static final String PROP_RDX = "RDX";
    public static final String PROP_RBX = "RBX";
    public static final String PROP_RSP = "RSP";
    public static final String PROP_RBP = "RBP";
    public static final String PROP_RSI = "RSI";
    public static final String PROP_RDI = "RDI";
    public static final String PROP_R8 = "R9";
    public static final String PROP_R9 = "R9";
    public static final String PROP_R10 = "R10";
    public static final String PROP_R11 = "R11";
    public static final String PROP_R12 = "R12";
    public static final String PROP_R13 = "R13";
    public static final String PROP_R14 = "R14";
    public static final String PROP_R15 = "R15";
    public static final String PROP_RIP = "RIP";
    public static final String PROP_FLAGS = "FLAGS";

    // Fields
    private final Register RAX = new Register();
    private final Register RCX = new Register();
    private final Register RDX = new Register();
    private final Register RBX = new Register();
    private final Register RSP = new Register();
    private final Register RBP = new Register();
    private final Register RSI = new Register();
    private final Register RDI = new Register();
    private final Register R8 = new Register();
    private final Register R9 = new Register();
    private final Register R10 = new Register();
    private final Register R11 = new Register();
    private final Register R12 = new Register();
    private final Register R13 = new Register();
    private final Register R14 = new Register();
    private final Register R15 = new Register();
    private final Register RIP = new Register();
    private final RegisterFlags FLAGS = new RegisterFlags();

    // Change property
    private final PropertyChangeSupport propertySupport;

    public CpuState() {
        propertySupport = new PropertyChangeSupport(this);
    }

    public Long getFLAGS() {
        return FLAGS.getQuadword();
    }

    public void setRIP(Long newValue) {
        long oldValue = RIP.getQuadword();
        RIP.setQuadword(newValue);
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public Long getRIP() {
        return RIP.getQuadword();
    }

    public Long getRAX() {
        return RAX.getQuadword();
    }

    public Long getRCX() {
        return RCX.getQuadword();
    }

    public Long getRDX() {
        return RDX.getQuadword();
    }

    public Long getRBX() {
        return RBX.getQuadword();
    }

    public Long getRSP() {
        return RSP.getQuadword();
    }

    public Long getRBP() {
        return RBP.getQuadword();
    }

    public Long getRSI() {
        return RSI.getQuadword();
    }

    public Long getRDI() {
        return RDI.getQuadword();
    }

    public Long getR8() {
        return R8.getQuadword();
    }

    public Long getR9() {
        return R9.getQuadword();
    }

    public Long getR10() {
        return R10.getQuadword();
    }

    public Long getR11() {
        return R11.getQuadword();
    }

    public Long getR12() {
        return R12.getQuadword();
    }

    public Long getR13() {
        return R13.getQuadword();
    }

    public Long getR14() {
        return R14.getQuadword();
    }

    public Long getR15() {
        return R15.getQuadword();
    }

    public void setRAX(Long v) {
        long oldValue = RAX.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RAX, oldValue, newValue);
    }

    public void setRCX(Long v) {
        long oldValue = RCX.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RCX, oldValue, newValue);
    }

    public void setRDX(Long v) {
        long oldValue = RDX.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RDX, oldValue, newValue);
    }

    public void setRBX(Long v) {
        long oldValue = RBX.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RBX, oldValue, newValue);
    }

    public void setRSP(Long v) {
        long oldValue = RSP.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RSP, oldValue, newValue);
    }

    public void setRBP(Long v) {
        long oldValue = RBP.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RBP, oldValue, newValue);
    }

    public void setRSI(Long v) {
        long oldValue = RSI.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RSI, oldValue, newValue);
    }

    public void setRDI(Long v) {
        long oldValue = RDI.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_RDI, oldValue, newValue);
    }

    public void setR8(Long v) {
        long oldValue = R8.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R8, oldValue, newValue);
    }

    public void setR9(Long v) {
        long oldValue = R9.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R9, oldValue, newValue);
    }

    public void setR10(Long v) {
        long oldValue = R10.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R10, oldValue, newValue);
    }

    public void setR11(Long v) {
        long oldValue = R11.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R11, oldValue, newValue);
    }

    public void setR12(Long v) {
        long oldValue = R12.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R12, oldValue, newValue);
    }

    public void setR13(Long v) {
        long oldValue = R13.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R13, oldValue, newValue);
    }

    public void setR14(Long v) {
        long oldValue = R14.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R14, oldValue, newValue);
    }

    public void setR15(Long v) {
        long oldValue = R15.setQuadword(v);
        long newValue = v;
        propertySupport.firePropertyChange(PROP_R15, oldValue, newValue);
    }

    public boolean getCF() {
        return FLAGS.getCF();
    }

    public boolean getPF() {
        return FLAGS.getPF();
    }

    public boolean getZF() {
        return FLAGS.getZF();
    }

    public boolean getSF() {
        return FLAGS.getSF();
    }

    public boolean getIF() {
        return FLAGS.getIF();
    }

    public boolean getDF() {
        return FLAGS.getDF();
    }

    public boolean getOF() {
        return FLAGS.getOF();
    }

    public void setCF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setCF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void setPF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setPF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void setZF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setZF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void setSF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setSF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void setIF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setIF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void setDF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setDF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void setOF(boolean flag) {
        long oldValue = FLAGS.getQuadword();
        FLAGS.setOF(flag);
        long newValue = FLAGS.getQuadword();
        propertySupport.firePropertyChange(PROP_FLAGS, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
