/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.components;

import it.uniroma2.pellegrini.z64sim.controller.SettingsController;

import javax.swing.*;
import java.awt.*;

public class LineNumbers extends JList<String> {

    private Font font;

    public LineNumbers(String[] initialData, Font font) {
        super(initialData);

        this.font = font;

        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(2, 12, 0, 2));
        setFont(font);
        setBackground(UIManager.getColor("Component.background"));

        setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) { }
            @Override
            public void addSelectionInterval(int index0, int index1) { }
        });

        setCellRenderer(new ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(
                JList<? extends String> list,
                String value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

                JLabel label = new JLabel(value);

                label.setOpaque(true);
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
                label.setFont(list.getFont());
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                label.setEnabled(false);

                return label;
            }
        });

        if (!SettingsController.getLineNumbers()) {
            setVisible(false);
        }
    }

    public void patchTheme() {
        this.setBackground(UIManager.getColor("Component.background"));
        this.setFont(font);
    }

    public void updateLineNumbers(int n) {
        String[] lines = new String[n];
        for (int i = 0; i < n; i++) {
            lines[i] = String.valueOf(i + 1);
        }

        setListData(lines);
    }
}
