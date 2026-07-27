/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.controller;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import it.uniroma2.pellegrini.z64sim.view.SettingsWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Shared {@link javax.swing.Action} instances for the application.
 * Each action encapsulates its handler, tooltip, and accelerator key.
 * Actions can be shared across buttons, menus, and keyboard bindings.
 *
 * @author Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 */
public final class AppActions {

    private AppActions() {}

    public static final Action ASSEMBLE = new AbstractAction() {
        {
            putValue(SHORT_DESCRIPTION, PropertyBroker.getMessageFromBundle("gui.assemble.program"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B,
                    java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SimulatorController.assembleProgram();
        }
    };

    public static final Action STEP = new AbstractAction() {
        {
            putValue(SHORT_DESCRIPTION, PropertyBroker.getMessageFromBundle("gui.step.instruction"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SimulatorController.step();
        }
    };

    public static final Action RUN = new AbstractAction() {
        {
            putValue(SHORT_DESCRIPTION, PropertyBroker.getMessageFromBundle("gui.run.program"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SimulatorController.run();
        }
    };

    public static final Action STOP = new AbstractAction() {
        {
            putValue(SHORT_DESCRIPTION, PropertyBroker.getMessageFromBundle("gui.stop.program"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.SHIFT_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SimulatorController.stop();
        }
    };

    public static final Action SHOW_SETTINGS = new AbstractAction(
            PropertyBroker.getMessageFromBundle("menu.file.settings")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            SettingsWindow dialog = new SettingsWindow();
            dialog.setTitle(PropertyBroker.getMessageFromBundle("menu.file.settings"));
            dialog.pack();
            dialog.setVisible(true);
        }
    };

    public static final Action QUIT = new AbstractAction(
            PropertyBroker.getMessageFromBundle("menu.file.quit")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.quit();
        }
    };

    public static final Action UNDO = new AbstractAction(
            PropertyBroker.getMessageFromBundle("menu.edit.undo")) {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                    java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.undo();
        }
    };

    public static final Action REDO = new AbstractAction(
            PropertyBroker.getMessageFromBundle("menu.edit.redo")) {
        {
            // Mac: Cmd+Shift+Z, Windows/Linux: Ctrl+Y
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                                | InputEvent.SHIFT_DOWN_MASK));
            } else {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                        java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.redo();
        }
    };

    public static final Action FIND = new AbstractAction(
            PropertyBroker.getMessageFromBundle("menu.edit.find")) {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,
                    java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.find();
        }
    };

    public static final Action FIND_REPLACE = new AbstractAction(
            PropertyBroker.getMessageFromBundle("menu.edit.replace")) {
        {
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H,
                    java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow.findReplace();
        }
    };
}
