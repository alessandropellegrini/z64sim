/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import it.uniroma2.pellegrini.z64sim.controller.MainController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow {
    private static MainWindow instance = null;

    private MainController controller = null;

    private JPanel mainPanel;
    private JButton quitButton;

    private MainWindow(MainController controller) {
        this.controller = controller;

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(null, "Prova");
            }
        });
    }

    public static MainWindow getInstance(MainController controller) {
        if(instance == null)
            instance = new MainWindow(controller);

        return instance;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


    public void show() {
        JFrame frame = new JFrame("App");
        frame.setContentPane(this.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        MainWindow mw = new MainWindow(null);
        mw.show();
    }
}
