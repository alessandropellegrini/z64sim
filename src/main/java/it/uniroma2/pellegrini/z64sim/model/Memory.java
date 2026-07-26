/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;


import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass1;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass2;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory extends AbstractTableModel {
    private static volatile Memory instance = null;
    private volatile Program program = null;
    private JTable memoryView;

    // Observable support for model-view decoupling
    private final transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public static final String PROP_PROGRAM = "program";

    private Memory() {
    }

    public static synchronized Memory getInstance() {
        if(instance == null)
            instance = new Memory();
        return instance;
    }

    public static void selectAddress(long address) {
        if(getInstance().memoryView == null) return;
        int row = (int) (address / 8);
        // Ensure JTable operations happen on the EDT
        if (SwingUtilities.isEventDispatchThread()) {
            doSelectAddress(row);
        } else {
            SwingUtilities.invokeLater(() -> doSelectAddress(row));
        }
    }

    private static void doSelectAddress(int row) {
        JTable view = getInstance().memoryView;
        if (view == null) return;
        view.getSelectionModel().setSelectionInterval(row, row);
        view.scrollRectToVisible(view.getCellRect(row, 0, true));
    }

    public static void setProgram(Program program) {
        Program oldProgram = getInstance().program;
        getInstance().program = program;
        getInstance().fireTableDataChanged();
        getInstance().pcs.firePropertyChange(PROP_PROGRAM, oldProgram, program);
    }

    public static void setValueAt(long address, byte srcValue) {
        getInstance().program.binary.put((int) address, new MemoryData(srcValue));
        int row = (int) address / 8;
        getInstance().fireTableRowsUpdated(row, row);
    }

    @Override
    public int getRowCount() {
        return this.program == null ? 200 : this.program.getLargestAddress() / 8 + 1;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int col) {
        return col == 0 ? PropertyBroker.getMessageFromBundle("memory.table.address") : PropertyBroker.getMessageFromBundle("memory.table.value");
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if(col == 0) {
            return getAddress(row);
        } else if(col == 1) {
            return getData(row);
        } else {
            throw new IllegalStateException("Internal error: unexpected column request");
        }
    }

    public String getAddress(int row) {
        return String.format("%#016x", row * 8); // 64-bit address
    }

    public String getData(int row) {
        // if program is not set, then just fill with zeroes
        if(this.program == null) {
            return "00 00 00 00 00 00 00 00";
        }

        final MemoryElement firstMemoryElement = this.program.getMemoryElementAt(row * 8L);
        // If first element is null, then check if previous first element was an immediate instruction
        if(firstMemoryElement == null) {
            final MemoryElement immediate = this.program.getMemoryElementAt((row - 1) * 8L);
            if(immediate instanceof InstructionClass1) {
                final OperandImmediate source = (OperandImmediate) ((InstructionClass1) immediate).getSource();
                return source.toBytesString();
            } else if(immediate instanceof InstructionClass2) {
                final OperandImmediate source = (OperandImmediate) ((InstructionClass2) immediate).getSource();
                return source.toBytesString();
            } else {
                return "00 00 00 00 00 00 00 00";
            }
        }

        // if first element is an instruction, then print it
        if(firstMemoryElement instanceof Instruction) {
            return firstMemoryElement.toString();
        }

        // if not previous, try to write all single byte value of this row
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 8; i++) {
            final MemoryElement memoryElement = this.program.getMemoryElementAt(row * 8L + i);
            if(memoryElement == null) {
                sb.append("00 ");
            } else {
                sb.append(memoryElement).append(" ");
                i += memoryElement.getSize() - 1;
            }
        }

        return sb.toString();
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        throw new IllegalStateException("Internal error: cannot set cell value");
    }

    public static byte getValueAt(long address) {
        return getInstance().program.getMemoryElementAt(address).getValue()[0]; // TODO: make it safer!
    }

    /**
     * Returns the {@link MemoryElement} stored at the given byte address,
     * or {@code null} if no program is loaded.
     */
    public static MemoryElement getMemoryElementAt(long address) {
        Program p = getInstance().program;
        if (p == null) return null;
        return p.getMemoryElementAt(address);
    }
    public void setView(JTable memoryView) {
        this.memoryView = memoryView;
    }
}
