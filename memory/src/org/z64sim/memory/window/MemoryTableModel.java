/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.z64sim.memory.window;

import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.z64sim.memory.Memory;
import org.z64sim.program.MemoryElement;

/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class MemoryTableModel extends AbstractTableModel implements TableModelListener {

    private final ArrayList<MemoryElement> memoryMap = Memory.memoryMap;

    private String[] columnNames = {"Address",
                                    "Hex Val.",
                                    "Dec. Val"};

    @Override
    public int getRowCount() {
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
        // Only the address cannot be edited
        return col >= 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return 0;
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
