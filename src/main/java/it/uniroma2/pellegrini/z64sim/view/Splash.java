/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import java.awt.*;

public class Splash {
    private final static int STATUS_BOX_HEIGHT = 23;
    private final static int PROGRESS_BAR_HEIGHT = 5;
    private final static int PADDING = 3;
    private final static int TEXT_SIZE = 10;

    private final SplashScreen splash = java.awt.SplashScreen.getSplashScreen();
    private final Graphics2D g = splash.createGraphics();
    private final int width = splash.getBounds().width;
    private final int height = splash.getBounds().height;
    private final Color backgroundColor = new Color(238, 238, 238); // #EEEEEE
    private final Color textColor = new Color(96, 96, 96); // #606060
    private final Color barColor = new Color(96, 190, 255); // #60BDFF

    private final int totalSteps;
    private int currentStep = 0;


    public Splash(int totalSteps) {
        this.totalSteps = totalSteps;

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_SIZE));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setPaintMode();
    }

    public void step(String description) {
        this.currentStep++;

        g.setColor(backgroundColor); // Bottom rectangle
        g.fillRect(0, this.height - STATUS_BOX_HEIGHT, this.width, STATUS_BOX_HEIGHT);
        g.setColor(textColor); // Status text
        g.drawString(description + "...", PADDING, this.height - STATUS_BOX_HEIGHT + PADDING + TEXT_SIZE);
        g.setColor(barColor); // Status text
        g.fillRect(0, this.height - PROGRESS_BAR_HEIGHT, (int)((double)this.currentStep / this.totalSteps * this.width), PROGRESS_BAR_HEIGHT);

        splash.update();
    }

    public void close() {
        splash.close();
    }
}