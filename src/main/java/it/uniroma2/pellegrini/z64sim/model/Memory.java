/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;


import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass1;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass2;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandImmediate;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory extends AbstractTableModel {
    private static Memory instance = null;
    private Program program = null;
    private JTable memoryView;

    private Memory() {
    }

    public static Memory getInstance() {
        if(instance == null)
            instance = new Memory();
        return instance;
    }

    public static void selectAddress(long address) {
        int row = (int) (address / 8);
        getInstance().memoryView.getSelectionModel().setSelectionInterval(row, row);
        getInstance().memoryView.scrollRectToVisible(getInstance().memoryView.getCellRect(row, 0, true));
        // TODO: probably overkill and leaky
        getInstance().memoryView.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                getInstance().memoryView.getSelectionModel().setSelectionInterval(row, row);
                getInstance().memoryView.scrollRectToVisible(getInstance().memoryView.getCellRect(row, 0, true));
            }
        });
    }

    public static void setProgram(Program program) {
        getInstance().program = program;
        getInstance().fireTableChanged(null);
    }

    public static void setValueAt(long address, byte srcValue) {
        getInstance().program.binary.put((int) address, new MemoryData(srcValue));
        getInstance().fireTableRowsUpdated((int) address / 8, (int) address / 8);
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
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if(col == 0) {
            return String.format("%#016x", row * 8); // 64-bit address
        } else if(col == 1) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < 8; i++) {
                if(this.program == null) {
                    sb.append("00 ");
                } else {
                    final MemoryElement memoryElement = this.program.getMemoryElementAt(row * 8L + i);
                    if(memoryElement == null) {
                        // Check for 16-byte instructions
                        // TODO: all this should be made safer!
                        final MemoryElement memoryElement2 = this.program.getMemoryElementAt((row - 1) * 8L);
                        if(memoryElement2 instanceof InstructionClass1) {
                            final OperandImmediate source = (OperandImmediate) ((InstructionClass1) memoryElement2).getSource();
                            sb.append(source.toBytesString());
                            i += 8;
                        } else if(memoryElement2 instanceof InstructionClass2) {
                            final OperandImmediate source = (OperandImmediate) ((InstructionClass2) memoryElement2).getSource();
                            sb.append(source.toBytesString());
                            i += 8;
                        } else {
                            sb.append("00 ");
                        }
                    } else {
                        sb.append(memoryElement).append(" ");
                        i += memoryElement.getSize() - 1;
                    }
                }
            }
            return sb.toString();
        } else {
            throw new IllegalStateException("Internal error: unexpected column request");
        }
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        throw new IllegalStateException("Internal error: cannot set cell value");
    }

    @Override
    public void addTableModelListener(TableModelListener tableModelListener) {

    }

    @Override
    public void removeTableModelListener(TableModelListener tableModelListener) {

    }

    public static byte getValueAt(long address) {
        return getInstance().program.getMemoryElementAt(address).getValue()[0]; // TODO: make it safer!
    }
    public void setView(JTable memoryView) {
        this.memoryView = memoryView;
    }
}
