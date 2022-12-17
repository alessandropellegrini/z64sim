/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class JFilePicker extends JPanel {
    private final JTextField filePath;
    private final JFileChooser fileChooser;

    private int mode;
    public static final int MODE_OPEN = 1;
    public static final int MODE_SAVE = 2;

    public JFilePicker(String textFieldLabel, String buttonLabel) {
        fileChooser = new JFileChooser();
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JLabel label = new JLabel(textFieldLabel);
        filePath = new JTextField(30);
        JButton button = new JButton(buttonLabel);

        button.addActionListener(this::buttonActionPerformed);

        this.add(label);
        this.add(filePath);
        this.add(button);
    }

    private void buttonActionPerformed(ActionEvent evt) {
        if (mode == MODE_OPEN) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else if (mode == MODE_SAVE) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public void addFileTypeFilter(String extension, String description) {
        FileTypeFilter filter = new FileTypeFilter(extension, description);
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getSelectedFilePath() {
        return filePath.getText();
    }

    public void setFilePath(String path) {
        filePath.setText(path);
    }

    public JFileChooser getFileChooser() {
        return this.fileChooser;
    }
}
