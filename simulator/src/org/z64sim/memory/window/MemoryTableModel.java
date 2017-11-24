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
import org.z64sim.program.instructions.*;
/**
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class MemoryTableModel extends AbstractTableModel implements TableModelListener {
   
    // These are keys in the bundle file
    private final String[] columnNames = {"MemoryTableModel.address",
                                          "MemoryTableModel.instruction",
                                          "MemoryTableModel.hex",
                                          "MemoryTableModel.dec"};

    @Override
    public int getRowCount() {
        if(Memory.getProgram().program == null)
               return 0;
        
        return Memory.getProgram().program.length/ 8;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return org.openide.util.NbBundle.getMessage(MemoryTableModel.class, columnNames[col]);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if(row < Memory.getProgram()._dataStart)
            return false;

        // Only the address and the mnemonic cannot be edited
        return col > 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object ret = null;
        int address = rowIndex * 8; // address is managed at a quadword basis

        byte value[] = new byte[8];
        for(int i = 0; i < 8; i++) {
            value[i] = Memory.getProgram().program[address + i];    
        }
        
        ByteBuffer wrapped = ByteBuffer.wrap(value);
        //wrapped.order(ByteOrder.LITTLE_ENDIAN);
        
        switch(columnIndex) {
            case 0: // Address
                ret = String.format("%016x", address);
                break;
            case 1: // Mnemonic
                if(address < Memory.getProgram()._start || address >= Memory.getProgram()._dataStart)
                    ret = "";
                else
                    ret = Instruction.disassemble(address);
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
    public static byte byteToBits(byte b, int start, int end){
       byte mask = 0;
        if(start < end || start > 7 || end < 0 ) throw new RuntimeException("No valid start || end");
        
        for (int i = 7-start; i <= 7-end; i++) {
            mask += 1 << 7-i;
        }
        byte ret = (byte) (mask & b);
        for (int i = 0; i < end; i++) {
            ret /= 2;
        }
        if (ret < 0) {
            ret += Math.pow(2, start-end+1);
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
