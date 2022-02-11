/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;


import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 *
 * @author Alessandro Pellegrini <pellegrini@dis.uniroma1.it>
 */
public class Memory implements TableModel {
    private static Memory instance = null;

    private Program program = null;

    private Memory() {}

    public static Memory getInstance() {
        if(instance == null)
            instance = new Memory();
        return instance;
    }

    public static Program getProgram() {
        return getInstance().program;
    }

    public static void setProgram(Program program) {
        getInstance().program = program;
    }

    @Override
    public int getRowCount() {
        return 200;// (int) Math.pow(2, 64);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int col) {
        return col == 0 ? "Address" : "Value";
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
        if(col == 0)
            return String.format("%016x", row); // 64-bit address
        else if(col == 1)
            return "Seconda";
        else
            throw new IllegalStateException("Unexpected column request");
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        throw new IllegalStateException("Cannot set cell value");
    }

    @Override
    public void addTableModelListener(TableModelListener tableModelListener) {

    }

    @Override
    public void removeTableModelListener(TableModelListener tableModelListener) {

    }
}
