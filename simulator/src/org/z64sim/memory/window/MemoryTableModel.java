/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory.window;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.z64sim.memory.Memory;
import org.z64sim.program.Instruction;
import org.z64sim.memory.MemoryElement;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class MemoryTableModel extends AbstractTableModel implements TableModelListener {

    private final ArrayList<MemoryElement> memoryMap = Memory.getMemory();

    private final String[] columnNames = {"Address",
                                          "Instruction",
                                          "Hex Val.",
                                          "Dec. Val"};

    @Override
    public int getRowCount() {
        if(memoryMap == null)
            return 0;

        return memoryMap.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // Cannot edit an instruction at all, so check if at that address there is one
        if(Memory.getElementFromAddress(row * 8) instanceof Instruction)
            return false;

        // Only the address and the mnemonic cannot be edited
        return col > 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object ret;

        long address = rowIndex * 8; // address is managed at a quadword basis
        MemoryElement el = Memory.getElementFromAddress(address);
        byte value[] = el.getValue();
        ByteBuffer wrapped = ByteBuffer.wrap(value);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);


        switch(columnIndex) {
            case 0: // Address
                ret = String.format("%016x", address);
                break;
            case 1: // Mnemonic
                if(el instanceof Instruction) {
                    ret = ((Instruction)el).toString();
                } else {
                    ret = "-";
                }
                break;
            case 2: // Hex
                ret = String.format("%016x", wrapped.getLong());
                break;
            case 3: // Int
                ret = wrapped.getInt();
                break;
            default:
                ret = 0;
        }
        return ret;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        TableModel model = (TableModel) e.getSource();
        String columnName = model.getColumnName(column);
        Object data = model.getValueAt(row, column);
    }

}
