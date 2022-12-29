/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.components;

import javax.swing.*;
import java.io.File;

public class JFileDialog extends JPanel {
    private final JFileChooser fileChooser;

    private int mode;
    public static final int MODE_OPEN = 1;
    public static final int MODE_SAVE = 2;

    public JFileDialog(String extension, String description, int mode, String current_dir) {
        this.fileChooser = new JFileChooser();
        File cd = new File(current_dir);
        if(cd.exists()) {
            this.fileChooser.setCurrentDirectory(cd);
        }
        FileTypeFilter filter = new FileTypeFilter(extension, description);
        this.fileChooser.addChoosableFileFilter(filter);
        this.fileChooser.setFileFilter(filter);
        this.mode = mode;
    }

    public String getFilePath() {
        if (mode == MODE_OPEN) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile().getAbsolutePath();
            }
        } else if (mode == MODE_SAVE) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile().getAbsolutePath();
            }
        }
        return null;
    }
}
