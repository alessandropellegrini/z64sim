/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

public class InterfaceRenderer {

    private enum TextAlign { START, MIDDLE, END }
    private enum Direction { LEFT, RIGHT, UP, DOWN }

    public Dimension render(Graphics2D g2, DeviceDescriptor desc) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Serif", Font.PLAIN, 14));

        int tempX = 450;
        if (desc.isBusyWaitingEnabled()) {
            IoPortDescriptor status = desc.getIoPort("STATUS");
            if (status != null && status.isReadable()) {
                tempX += 400;
            } else {
                tempX += 200;
            }
        }

        boolean hasMode = false;
        List<IoPortDescriptor> customFfs = new ArrayList<>();
        for (IoPortDescriptor p : desc.getIoPorts()) {
            if (p.isFlipFlop() && !p.getName().equals("STATUS") && !p.getName().equals("INT_REQ")) {
                customFfs.add(p);
                if (p.getName().equals("MODE")) {
                    hasMode = true;
                }
                // Reserve more space for writable FFs (AND4 gates + 4 drop wires)
                tempX += p.isWritable() ? 380 : 280;
            }
        }

        if (desc.isInterruptsEnabled()) {
            tempX += 530;
        }

        List<IoPortDescriptor> customRegs = new ArrayList<>();
        for (IoPortDescriptor p : desc.getIoPorts()) {
            if (p.isRegister()) {
                customRegs.add(p);
                String dir = getDirection(p);
                int xBox = dir.equals("input") ? tempX - 120 : tempX;
                if (dir.equals("output")) {
                    tempX = xBox + 280;
                } else {
                    tempX = xBox + 380;
                }
            }
        }

        int xCu = tempX + 100;
        int yAb = 80;
        int yDb = 120;
        int yCb = 160;

        drawRect(g2, 20, 40, 100, 160, "z64");

        int busEnd = xCu + 100;
        int textEnd = xCu + 110;
        int decEnd = busEnd;

        drawLine(g2, 120, yAb, busEnd, yAb);
        drawText(g2, textEnd, yAb, "I/OAB", TextAlign.START, false);

        drawLine(g2, 120, yDb, busEnd, yDb);
        drawText(g2, textEnd, yDb, "I/ODB", TextAlign.START, false);

        drawLine(g2, 120, yCb, busEnd, yCb);
        drawText(g2, textEnd, yCb, "I/OCB", TextAlign.START, false);

        int xDec = 220;
        int decYTop = yCb + 40;
        int[][] decInOut = drawDecoder(g2, xDec, decYTop, 60, 140, 60);
        int decOutY = decInOut[1][1];

        drawLine(g2, xDec, yAb, xDec, decYTop);
        drawNode(g2, xDec, yAb);

        List<String> decoderComponents = new ArrayList<>();
        if (desc.isBusyWaitingEnabled()) decoderComponents.add("STATUS");
        if (desc.isInterruptsEnabled()) decoderComponents.add("INT_REQ");
        for (IoPortDescriptor p : customFfs) {
            decoderComponents.add(p.getName());
        }
        for (IoPortDescriptor p : customRegs) {
            decoderComponents.add(p.getName());
        }

        int yDecBusStart = decOutY + 30;
        int decBusSpacing = 15;
        Map<String, Integer> compDecY = new HashMap<>();

        int startX = (int) (xDec - (decoderComponents.size() - 1) * 15 / 2.0);
        for (int i = 0; i < decoderComponents.size(); i++) {
            String c = decoderComponents.get(i);
            int xOut = startX + i * 15;
            int yBus = yDecBusStart + i * decBusSpacing;
            compDecY.put(c, yBus);
            drawOrthogonal(g2, new int[][]{ {xOut, decOutY}, {xOut, yBus}, {decEnd, yBus} });
            drawNode(g2, xOut, decOutY);
        }

        int yComponents = yDecBusStart + decoderComponents.size() * decBusSpacing + 150;
        int yCu = yComponents + 320;
        int xCurr = 450;

        if (desc.isBusyWaitingEnabled()) {
            String name = "STATUS";
            int boxW = 60, boxH = 80;
            int xBox = xCurr;
            int yBox = yComponents;
            int xLeft = xBox - boxW / 2;
            int xRight = xBox + boxW / 2;

            drawRect(g2, xLeft, yBox, boxW, boxH, name);
            drawText(g2, xLeft + 10, yBox + 15, "R", TextAlign.START, false);
            drawText(g2, xLeft + 10, yBox + boxH - 15, "S", TextAlign.START, false);
            drawText(g2, xRight - 15, yBox + 15, "Q", TextAlign.START, false);
            drawText(g2, xRight - 20, yBox + boxH - 15, "Q", TextAlign.START, true);

            int andSx = xLeft - 40;
            int andSy = yBox + 15;
            int[][] sOutArr = drawAnd3(g2, andSx, andSy, Direction.RIGHT);
            int[] sIn1 = sOutArr[0], sIn2 = sOutArr[1], sIn3 = sOutArr[2], sOut = sOutArr[3];
            drawLine(g2, sOut[0], sOut[1], xLeft, andSy);

            int[] xs = getDropXs(xLeft - 65, new String[]{"WR", "IO", ""}, Direction.LEFT, 15);
            int xWr = xs[0], xIo = xs[1], xDecSel = xs[2];

            drawOrthogonal(g2, new int[][]{ {sIn1[0], sIn1[1]}, {xWr, sIn1[1]}, {xWr, yCb} });
            drawNode(g2, xWr, yCb);
            drawText(g2, xWr, yCb - 12, "WR", TextAlign.MIDDLE, false);

            drawOrthogonal(g2, new int[][]{ {sIn2[0], sIn2[1]}, {xIo, sIn2[1]}, {xIo, yCb} });
            drawNode(g2, xIo, yCb);
            drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);

            int decY = compDecY.get(name);
            drawOrthogonal(g2, new int[][]{ {sIn3[0], sIn3[1]}, {xDecSel, sIn3[1]}, {xDecSel, decY} });
            drawNode(g2, xDecSel, decY);

            IoPortDescriptor status = desc.getIoPort("STATUS");
            boolean isReadable = (status != null && status.isReadable());
            if (isReadable) {
                int bufX = xRight + 20;
                int bufY = yBox - 40;
                int[][] bOutArr = drawBuffer(g2, bufX, bufY, Direction.UP, "right");
                int[] bIn = bOutArr[0], bEn = bOutArr[1], bOut = bOutArr[2];

                drawOrthogonal(g2, new int[][]{ {xRight, yBox + 15}, {bufX, yBox + 15}, {bIn[0], bIn[1]} });
                drawNode(g2, xRight + 10, yBox + 15);
                drawLine(g2, bOut[0], bOut[1], bOut[0], yDb);
                drawNode(g2, bOut[0], yDb);

                int andRx = bufX + 60;
                int andRy = bEn[1];
                int[][] rOutArr = drawAnd3(g2, andRx, andRy, Direction.LEFT);
                int[] rIn1 = rOutArr[0], rIn2 = rOutArr[1], rIn3 = rOutArr[2], rOut = rOutArr[3];
                drawLine(g2, rOut[0], rOut[1], bEn[0], bEn[1]);

                int[] xs2 = getDropXs(bufX + 85, new String[]{"RD", "IO", ""}, Direction.RIGHT, 15);
                int xRd = xs2[0], xIo2 = xs2[1], xDecSel2 = xs2[2];

                drawOrthogonal(g2, new int[][]{ {rIn1[0], rIn1[1]}, {xRd, rIn1[1]}, {xRd, yCb} });
                drawNode(g2, xRd, yCb);
                drawText(g2, xRd, yCb - 12, "RD", TextAlign.MIDDLE, false);

                drawOrthogonal(g2, new int[][]{ {rIn2[0], rIn2[1]}, {xIo2, rIn2[1]}, {xIo2, yCb} });
                drawNode(g2, xIo2, yCb);
                drawText(g2, xIo2, yCb - 12, "IO", TextAlign.MIDDLE, false);

                drawOrthogonal(g2, new int[][]{ {rIn3[0], rIn3[1]}, {xDecSel2, rIn3[1]}, {xDecSel2, decY} });
                drawNode(g2, xDecSel2, decY);
            }

            int yStartBus = yCu - 130;
            drawOrthogonal(g2, new int[][]{ {xRight, yBox + 15}, {xRight + 10, yBox + 15}, {xRight + 10, yStartBus}, {xCu - 25, yStartBus}, {xCu - 25, yCu - 52} });
            drawText(g2, xRight + 15, yStartBus - 10, "START", TextAlign.START, false);

            int xComp = xLeft - 15;
            int yCompBus = yCu + 100;
            drawOrthogonal(g2, new int[][]{ {xCu + 20, yCu + 52}, {xCu + 20, yCompBus}, {xComp, yCompBus}, {xComp, yBox + boxH - 15}, {xLeft, yBox + boxH - 15} });
            drawText(g2, xCu + 25, yCompBus - 10, "COMPLETE", TextAlign.START, false);

            if (isReadable) {
                xCurr += 400;
            } else {
                xCurr += 200;
            }
        }

        int cuSigIdx = 0;
        for (IoPortDescriptor p : customFfs) {
            String name = p.getName();
            int boxW = 60, boxH = 80;
            int xBox = xCurr;
            int yBox = yComponents;
            int xLeft = xBox - boxW / 2;
            int xRight = xBox + boxW / 2;

            drawRect(g2, xLeft, yBox, boxW, boxH, name);
            drawText(g2, xLeft + 10, yBox + 15, "R", TextAlign.START, false);
            drawText(g2, xLeft + 10, yBox + boxH - 15, "S", TextAlign.START, false);
            drawText(g2, xRight - 15, yBox + 15, "Q", TextAlign.START, false);
            drawText(g2, xRight - 20, yBox + boxH - 15, "Q", TextAlign.START, true);

            int decY = compDecY.containsKey(name) ? compDecY.get(name) : yDecBusStart;

            if (p.isWritable()) {
                // Writable FF: CPU writes 0/1 via data bus.
                // S = AND4(WR, IO, DEC_SEL, D0)       — set when D0=1
                // R = AND4(WR, IO, DEC_SEL, NOT(D0))   — reset when D0=0
                // All 4 input wires drop vertically from the buses, left of the gates.

                // S gate centered on S pin (yBox + boxH - 15)
                int andSx = xLeft - 45;
                int andSy = yBox + boxH - 15;
                int[][] sArr = drawAnd4(g2, andSx, andSy, Direction.RIGHT, false);
                int[] sIn1 = sArr[0], sIn2 = sArr[1], sIn3 = sArr[2], sIn4 = sArr[3], sOut = sArr[4];
                drawLine(g2, sOut[0], sOut[1], xLeft, andSy);

                // R gate centered on R pin (yBox + 15)
                int andRx = xLeft - 45;
                int andRy = yBox + 15;
                int[][] rArr = drawAnd4(g2, andRx, andRy, Direction.RIGHT, true);
                int[] rIn1 = rArr[0], rIn2 = rArr[1], rIn3 = rArr[2], rIn4 = rArr[3], rOut = rArr[4];
                drawLine(g2, rOut[0], rOut[1], xLeft, andRy);

                // Drop positions: fixed 18px spacing, right to left.
                // D0 is rightmost (from data bus), then WR, IO, DEC_SEL.
                // This keeps all wires safely to the left of the gate body.
                int xD0  = andSx - 15 - 15;    // well left of gate input pins
                int xWr  = xD0 - 18;
                int xIo  = xWr - 18;
                int xDecSel = xIo - 18;

                // WR drops from control bus — shared by both gates
                drawLine(g2, xWr, yCb, xWr, sIn1[1]);
                drawNode(g2, xWr, yCb);
                drawText(g2, xWr, yCb - 12, "WR", TextAlign.MIDDLE, false);
                drawOrthogonal(g2, new int[][]{ {sIn1[0], sIn1[1]}, {xWr, sIn1[1]} });
                drawOrthogonal(g2, new int[][]{ {rIn1[0], rIn1[1]}, {xWr, rIn1[1]} });
                drawNode(g2, xWr, rIn1[1]);
                drawNode(g2, xWr, sIn1[1]);

                // IO drops from control bus — shared by both gates
                drawLine(g2, xIo, yCb, xIo, sIn2[1]);
                drawNode(g2, xIo, yCb);
                drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);
                drawOrthogonal(g2, new int[][]{ {sIn2[0], sIn2[1]}, {xIo, sIn2[1]} });
                drawOrthogonal(g2, new int[][]{ {rIn2[0], rIn2[1]}, {xIo, rIn2[1]} });
                drawNode(g2, xIo, rIn2[1]);
                drawNode(g2, xIo, sIn2[1]);

                // DEC_SEL drops from decoder bus — shared by both gates
                drawLine(g2, xDecSel, decY, xDecSel, sIn3[1]);
                drawOrthogonal(g2, new int[][]{ {sIn3[0], sIn3[1]}, {xDecSel, sIn3[1]} });
                drawOrthogonal(g2, new int[][]{ {rIn3[0], rIn3[1]}, {xDecSel, rIn3[1]} });
                drawNode(g2, xDecSel, decY);
                drawNode(g2, xDecSel, rIn3[1]);
                drawNode(g2, xDecSel, sIn3[1]);

                // D0 drops from data bus — shared by both gates
                drawLine(g2, xD0, yDb, xD0, sIn4[1]);
                drawNode(g2, xD0, yDb);
                drawBusStroke(g2, xD0, (yDb + yCb) / 2 + 10, "1");
                drawOrthogonal(g2, new int[][]{ {sIn4[0], sIn4[1]}, {xD0, sIn4[1]} });
                drawOrthogonal(g2, new int[][]{ {rIn4[0], rIn4[1]}, {xD0, rIn4[1]} });
                drawNode(g2, xD0, rIn4[1]);
                drawNode(g2, xD0, sIn4[1]);

                // Redraw the negation bubble on the R gate after wires,
                // so the D0 vertical wire does not hide it.
                drawWhiteNode(g2, andRx - 15 - 4, rIn4[1]);
            } else {
                // Non-writable FF: original 3-input AND on S
                int andSx = xLeft - 40;
                int andSy = yBox + boxH - 15;
                int[][] sOutArr = drawAnd3(g2, andSx, andSy, Direction.RIGHT);
                int[] sIn1 = sOutArr[0], sIn2 = sOutArr[1], sIn3 = sOutArr[2], sOut = sOutArr[3];
                drawLine(g2, sOut[0], sOut[1], xLeft, andSy);

                int[] xs = getDropXs(xLeft - 65, new String[]{"WR", "IO", ""}, Direction.LEFT, 15);
                int xWr = xs[0], xIo = xs[1], xDecSel = xs[2];

                drawOrthogonal(g2, new int[][]{ {sIn1[0], sIn1[1]}, {xWr, sIn1[1]}, {xWr, yCb} });
                drawNode(g2, xWr, yCb);
                drawText(g2, xWr, yCb - 12, "WR", TextAlign.MIDDLE, false);

                drawOrthogonal(g2, new int[][]{ {sIn2[0], sIn2[1]}, {xIo, sIn2[1]}, {xIo, yCb} });
                drawNode(g2, xIo, yCb);
                drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);

                drawOrthogonal(g2, new int[][]{ {sIn3[0], sIn3[1]}, {xDecSel, sIn3[1]}, {xDecSel, decY} });
                drawNode(g2, xDecSel, decY);
            }

            if (p.isConnectedToCU()) {
                // Use the flip-flop name as the CU wire label;
                // MODE is a special case: its CU signal is called OPCODE.
                String sig = name;
                if (name.equals("MODE")) {
                    sig = "OPCODE";
                }
                int yOpBus = yCu - 85 - (cuSigIdx * 15);
                int cuXEntry = xCu - 15 + (cuSigIdx * 20);
                drawOrthogonal(g2, new int[][]{ {xRight, yBox + 15}, {xRight + 20, yBox + 15}, {xRight + 20, yOpBus}, {cuXEntry, yOpBus}, {cuXEntry, yCu - 52} });
                drawText(g2, cuXEntry - 5, yOpBus - 5, sig, TextAlign.END, false);
                cuSigIdx++;
            }
            // Writable FFs need more horizontal space for AND4 gates + drop wires
            xCurr += p.isWritable() ? 380 : 280;
        }

        if (desc.isInterruptsEnabled()) {
            String name = "INT_REQ";

            int boxW = 60, boxH = 80;
            int xBox = xCurr;
            int yBox = yComponents;
            int xLeft = xBox - boxW / 2;
            int xRight = xBox + boxW / 2;

            drawRect(g2, xLeft, yBox, boxW, boxH, name);
            drawText(g2, xLeft + 10, yBox + 15, "R", TextAlign.START, false);
            drawText(g2, xLeft + 10, yBox + boxH - 15, "S", TextAlign.START, false);
            drawText(g2, xRight - 15, yBox + 15, "Q", TextAlign.START, false);
            drawText(g2, xRight - 20, yBox + boxH - 15, "Q", TextAlign.START, true);

            int andRx = xLeft - 40;
            int andRy = yBox + 15;
            int[][] rOutArr = drawAnd3(g2, andRx, andRy, Direction.RIGHT);
            int[] rIn1 = rOutArr[0], rIn2 = rOutArr[1], rIn3 = rOutArr[2], rOut = rOutArr[3];
            drawLine(g2, rOut[0], rOut[1], xLeft, andRy);

            int[] xs = getDropXs(xLeft - 65, new String[]{"WR", "IO", ""}, Direction.LEFT, 15);
            int xWr = xs[0], xIo = xs[1], xDecSel = xs[2];

            drawOrthogonal(g2, new int[][]{ {rIn1[0], rIn1[1]}, {xWr, rIn1[1]}, {xWr, yCb} });
            drawNode(g2, xWr, yCb);
            drawText(g2, xWr, yCb - 12, "WR", TextAlign.MIDDLE, false);

            drawOrthogonal(g2, new int[][]{ {rIn2[0], rIn2[1]}, {xIo, rIn2[1]}, {xIo, yCb} });
            drawNode(g2, xIo, yCb);
            drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);

            int decY = compDecY.get(name);
            drawOrthogonal(g2, new int[][]{ {rIn3[0], rIn3[1]}, {xDecSel, rIn3[1]}, {xDecSel, decY} });
            drawNode(g2, xDecSel, decY);

            int yCompBus = yCu + 100;
            drawOrthogonal(g2, new int[][]{ {xLeft, yBox + boxH - 15}, {xLeft - 40, yBox + boxH - 15}, {xLeft - 40, yCompBus} });
            if (desc.isBusyWaitingEnabled()) {
                drawNode(g2, xLeft - 40, yCompBus);
            } else {
                drawOrthogonal(g2, new int[][]{ {xLeft - 40, yCompBus}, {xCu + 20, yCompBus}, {xCu + 20, yCu + 52} });
                drawText(g2, xCu + 25, yCompBus - 10, "COMPLETE", TextAlign.START, false);
            }

            int ocX = xRight + 30;
            int ocY = yBox - 40;
            int[][] ocOutArr = drawNotOC(g2, ocX, ocY, Direction.UP);
            int[] ocIn = ocOutArr[0], ocOut = ocOutArr[1];

            int qY = yBox + 15;
            drawOrthogonal(g2, new int[][]{ {xRight, qY}, {ocX, qY}, {ocIn[0], ocIn[1]} });
            drawNode(g2, ocX, qY);

            drawLine(g2, ocOut[0], ocOut[1], ocOut[0], yCb);
            drawNode(g2, ocOut[0], yCb);
            drawText(g2, ocOut[0], yCb - 12, "IRQ", TextAlign.MIDDLE, true);

            int xDc = xRight + 150;
            int yDc = yBox - 15;

            int a1X = xDc - 40;
            int[][] a1OutArr = drawAnd(g2, a1X, yDc, Direction.LEFT, 2, false, true);
            int[] a1In1 = a1OutArr[0], a1In2 = a1OutArr[1], a1Out = a1OutArr[2];

            int a2X = xDc + 40;
            int[][] a2OutArr = drawAnd(g2, a2X, yDc, Direction.RIGHT, 2, false, false);
            int[] a2In1 = a2OutArr[0], a2In2 = a2OutArr[1], a2Out = a2OutArr[2];

            int yIackSplit = yDc - 8;
            drawOrthogonal(g2, new int[][]{ {xDc, yCb}, {xDc, yIackSplit} });
            drawNode(g2, xDc, yCb);
            drawText(g2, xDc, yCb - 12, "IACK_IN", TextAlign.MIDDLE, false);
            drawNode(g2, xDc, yIackSplit);

            drawLine(g2, xDc, yIackSplit, a1In1[0], a1In1[1]);
            drawLine(g2, xDc, yIackSplit, a2In1[0], a2In1[1]);

            int yQRoute = yDc + 35;
            drawOrthogonal(g2, new int[][]{ {ocX, qY}, {ocX, yQRoute}, {xDc, yQRoute} });

            int yIn2 = a1In2[1];
            drawLine(g2, xDc, yQRoute, xDc, yIn2);
            drawNode(g2, xDc, yIn2);

            drawLine(g2, xDc, yIn2, a1In2[0], yIn2);
            drawLine(g2, xDc, yIn2, a2In2[0], yIn2);

            int xIackOut = xDc - 70;
            drawOrthogonal(g2, new int[][]{ {a1Out[0], a1Out[1]}, {xIackOut, a1Out[1]}, {xIackOut, yCb} });
            drawNode(g2, xIackOut, yCb);
            drawText(g2, xIackOut, yCb - 12, "IACK_OUT", TextAlign.MIDDLE, false);

            // IVN register — hardwired at registration time, not a CPU-writable port.
            // During IACK, the right AND gate enables the tri-state buffer to put
            // the IVN value onto the data bus.
            int xIvn = xDc + 110;
            int yIvn = yBox;
            drawRect(g2, xIvn - 30, yIvn, 60, 40, "IVN");

            int bufIvnX = xIvn;
            int bufIvnY = yIvn - 45;
            int[][] biOutArr = drawBuffer(g2, bufIvnX, bufIvnY, Direction.UP, "left");
            int[] biIn = biOutArr[0], biEn = biOutArr[1], biOut = biOutArr[2];

            drawLine(g2, xIvn, yIvn, biIn[0], biIn[1]);
            drawLine(g2, biOut[0], biOut[1], biOut[0], yDb);
            drawNode(g2, biOut[0], yDb);
            int myY = 135;
            drawBusStroke(g2, biOut[0], myY, "8");

            drawOrthogonal(g2, new int[][]{ {a2Out[0], a2Out[1]}, {biEn[0] - 20, a2Out[1]}, {biEn[0] - 20, biEn[1]}, {biEn[0], biEn[1]} });

            xCurr += 530;
        }

        for (IoPortDescriptor p : customRegs) {
            String name = p.getName();
            int boxW = 80, boxH = 60;
            String dir = getDirection(p);
            int xBox = dir.equals("input") ? xCurr - 120 : xCurr;
            int yBox = yComponents;
            int xLeft = xBox - boxW / 2;
            int xRight = xBox + boxW / 2;

            drawRect(g2, xLeft, yBox, boxW, boxH, name);
            int decY = compDecY.get(name);

            if (dir.equals("input") || dir.equals("inout")) {
                int bufX = xRight + 20;
                int bufY = yBox - 40;
                int[][] bOutArr = drawBuffer(g2, bufX, bufY, Direction.UP, "right");
                int[] bIn = bOutArr[0], bEn = bOutArr[1], bOut = bOutArr[2];

                drawOrthogonal(g2, new int[][]{ {xRight, yBox + 15}, {bufX, yBox + 15}, {bIn[0], bIn[1]} });

                drawLine(g2, bOut[0], bOut[1], bOut[0], yDb);
                drawNode(g2, bOut[0], yDb);
                int myY = 135;
                drawBusStroke(g2, bOut[0], myY, String.valueOf(p.getWidthBytes() * 8));

                int andX = bufX + 40;
                int andY = bEn[1];
                int[][] aOutArr = drawAnd3(g2, andX, andY, Direction.LEFT);
                int[] aIn1 = aOutArr[0], aIn2 = aOutArr[1], aIn3 = aOutArr[2], aOut = aOutArr[3];
                drawLine(g2, aOut[0], aOut[1], bEn[0], bEn[1]);

                int[] xs = getDropXs(bufX + 85, new String[]{"RD", "IO", ""}, Direction.RIGHT, 15);
                int xRd = xs[0], xIo = xs[1], xDecSel2 = xs[2];

                drawOrthogonal(g2, new int[][]{ {aIn1[0], aIn1[1]}, {xRd, aIn1[1]}, {xRd, yCb} });
                drawNode(g2, xRd, yCb);
                drawText(g2, xRd, yCb - 12, "RD", TextAlign.MIDDLE, false);

                drawOrthogonal(g2, new int[][]{ {aIn2[0], aIn2[1]}, {xIo, aIn2[1]}, {xIo, yCb} });
                drawNode(g2, xIo, yCb);
                drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);

                drawOrthogonal(g2, new int[][]{ {aIn3[0], aIn3[1]}, {xDecSel2, aIn3[1]}, {xDecSel2, decY} });
                drawNode(g2, xDecSel2, decY);
            }

            if (dir.equals("output") || dir.equals("inout")) {
                int dbX = xLeft + 15;
                drawLine(g2, dbX, yDb, dbX, yBox);
                drawNode(g2, dbX, yDb);

                if (dir.equals("output")) {
                    int andX = xLeft - 30;
                    int andY = yBox + boxH / 2;
                    int[][] aOutArr = drawAnd3(g2, andX, andY, Direction.RIGHT);
                    int[] aIn1 = aOutArr[0], aIn2 = aOutArr[1], aIn3 = aOutArr[2], aOut = aOutArr[3];

                    int[] xs = getDropXs(xLeft - 65, new String[]{"WR", "IO", ""}, Direction.LEFT, 15);
                    int xWr = xs[0], xIo = xs[1], xDecSel2 = xs[2];

                    drawOrthogonal(g2, new int[][]{ {aIn1[0], aIn1[1]}, {xWr, aIn1[1]}, {xWr, yCb} });
                    drawNode(g2, xWr, yCb);
                    drawText(g2, xWr, yCb - 12, "WR", TextAlign.MIDDLE, false);

                    drawOrthogonal(g2, new int[][]{ {aIn2[0], aIn2[1]}, {xIo, aIn2[1]}, {xIo, yCb} });
                    drawNode(g2, xIo, yCb);
                    drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);

                    drawOrthogonal(g2, new int[][]{ {aIn3[0], aIn3[1]}, {xDecSel2, aIn3[1]}, {xDecSel2, decY} });
                    drawNode(g2, xDecSel2, decY);

                    drawOrthogonal(g2, new int[][]{ {aOut[0], aOut[1]}, {xLeft, aOut[1]} });
                    drawText(g2, xLeft - 5, aOut[1] - 15, "LD", TextAlign.END, false);
                } else {
                    int orX = xLeft - 30;
                    int orY = yBox + boxH / 2;
                    int[][] oOutArr = drawOr(g2, orX, orY, Direction.RIGHT);
                    int[] oIn1 = oOutArr[0], oIn2 = oOutArr[1], oOut = oOutArr[2];
                    drawLine(g2, oOut[0], oOut[1], xLeft, oOut[1]);
                    drawText(g2, xLeft - 5, oOut[1] - 15, "LD", TextAlign.END, false);

                    int andX = xLeft - 70;
                    int andY = oIn1[1];
                    int[][] aOutArr = drawAnd3(g2, andX, andY, Direction.RIGHT);
                    int[] aIn1 = aOutArr[0], aIn2 = aOutArr[1], aIn3 = aOutArr[2], aOut = aOutArr[3];

                    drawOrthogonal(g2, new int[][]{ {aOut[0], aOut[1]}, {oIn1[0], oIn1[1]} });

                    int[] xs = getDropXs(xLeft - 90, new String[]{"WR", "IO", ""}, Direction.LEFT, 15);
                    int xWr = xs[0], xIo = xs[1], xDecSel2 = xs[2];

                    drawOrthogonal(g2, new int[][]{ {aIn1[0], aIn1[1]}, {xWr, aIn1[1]}, {xWr, yCb} });
                    drawNode(g2, xWr, yCb);
                    drawText(g2, xWr, yCb - 12, "WR", TextAlign.MIDDLE, false);

                    drawOrthogonal(g2, new int[][]{ {aIn2[0], aIn2[1]}, {xIo, aIn2[1]}, {xIo, yCb} });
                    drawNode(g2, xIo, yCb);
                    drawText(g2, xIo, yCb - 12, "IO", TextAlign.MIDDLE, false);

                    drawOrthogonal(g2, new int[][]{ {aIn3[0], aIn3[1]}, {xDecSel2, aIn3[1]}, {xDecSel2, decY} });
                    drawNode(g2, xDecSel2, decY);

                    int xCuld = xLeft - 45;
                    int yCuldBus = yCu - 145;
                    drawOrthogonal(g2, new int[][]{ {xCu + 20, yCu - 52}, {xCu + 20, yCuldBus}, {xCuld, yCuldBus}, {xCuld, oIn2[1]}, {oIn2[0], oIn2[1]} });
                    drawText(g2, xCuld - 5, yCuldBus - 10, "CU_LD", TextAlign.END, false);
                }
            }

            if (p.isConnectedToPU()) {
                drawHexagon(g2, xBox, yBox + 120, 35, "PU");
                drawLine(g2, xBox - 10, yBox + boxH, xBox - 10, (int) (yBox + 120 - 35 * 0.866));
                drawLine(g2, xBox + 10, yBox + boxH, xBox + 10, (int) (yBox + 120 - 35 * 0.866));
            }

            if (dir.equals("output")) {
                xCurr = xBox + 280;
            } else {
                xCurr = xBox + 380;
            }
        }

        // Only draw CU if something connects to it
        boolean needsCu = desc.isBusyWaitingEnabled()
                || desc.isInterruptsEnabled()
                || cuSigIdx > 0;
        for (IoPortDescriptor p : customRegs) {
            if (p.isReadable() && p.isWritable()) { // inout registers have CU_LD
                needsCu = true;
                break;
            }
        }

        int finalHeight;
        if (needsCu) {
            drawHexagon(g2, xCu, yCu, 60, "CU");
            finalHeight = yCu + 150;
        } else {
            // No CU: canvas ends below the component row
            finalHeight = yComponents + 200;
        }

        return new Dimension(xCu + 180, finalHeight);
    }

    private String getDirection(IoPortDescriptor p) {
        if (p.isReadable() && p.isWritable()) return "inout";
        if (p.isReadable()) return "input";
        if (p.isWritable()) return "output";
        return "inout";
    }

    private void drawRect(Graphics2D g2, int x, int y, int w, int h, String text) {
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, w, h);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(x, y, w, h);
        if (text != null && !text.isEmpty()) {
            String[] lines = text.split("\n");
            int dy = y + h / 2 - (lines.length - 1) * 8;
            for (String line : lines) {
                drawText(g2, x + w / 2, dy, line, TextAlign.MIDDLE, false);
                dy += 16;
            }
        }
    }

    private void drawLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(x1, y1, x2, y2);
    }

    private void drawOrthogonal(Graphics2D g2, int[][] points) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        for (int i = 0; i < points.length - 1; i++) {
            g2.drawLine(points[i][0], points[i][1], points[i + 1][0], points[i + 1][1]);
        }
    }

    private void drawNode(Graphics2D g2, int x, int y) {
        g2.setColor(Color.BLACK);
        g2.fillOval(x - 4, y - 4, 8, 8);
    }

    private void drawText(Graphics2D g2, int x, int y, String text, TextAlign align, boolean overline) {
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        int drawX = x;
        if (align == TextAlign.MIDDLE) drawX = x - w / 2;
        else if (align == TextAlign.END) drawX = x - w;
        int drawY = y + (fm.getAscent() - fm.getDescent()) / 2;

        g2.drawString(text, drawX, drawY);

        if (overline) {
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(drawX, y - 10, drawX + w, y - 10);
        }
    }

    private int[][] drawAnd(Graphics2D g2, int x, int y, Direction dir, int inputs, boolean inv1, boolean inv2) {
        Path2D path = new Path2D.Double();
        if (dir == Direction.RIGHT) {
            path.moveTo(x - 15, y - 15);
            path.lineTo(x, y - 15);
            path.append(new Arc2D.Double(x - 15, y - 15, 30, 30, 90, -180, Arc2D.OPEN), true);
            path.lineTo(x - 15, y + 15);
            path.closePath();
        } else if (dir == Direction.LEFT) {
            path.moveTo(x + 15, y - 15);
            path.lineTo(x, y - 15);
            path.append(new Arc2D.Double(x - 15, y - 15, 30, 30, 90, 180, Arc2D.OPEN), true);
            path.lineTo(x + 15, y + 15);
            path.closePath();
        }
        g2.setColor(Color.WHITE);
        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(path);

        int[] in1, in2, out;
        if (dir == Direction.RIGHT) {
            in1 = inputs >= 2 ? new int[]{x - 15, y - 8} : new int[]{x - 15, y};
            in2 = inputs >= 2 ? new int[]{x - 15, y + 8} : new int[]{x - 15, y};
            out = new int[]{x + 15, y};
            if (inv1) {
                drawWhiteNode(g2, in1[0] - 4, in1[1]);
                in1[0] -= 8;
            }
            if (inv2) {
                drawWhiteNode(g2, in2[0] - 4, in2[1]);
                in2[0] -= 8;
            }
        } else {
            in1 = inputs >= 2 ? new int[]{x + 15, y - 8} : new int[]{x + 15, y};
            in2 = inputs >= 2 ? new int[]{x + 15, y + 8} : new int[]{x + 15, y};
            out = new int[]{x - 15, y};
            if (inv1) {
                drawWhiteNode(g2, in1[0] + 4, in1[1]);
                in1[0] += 8;
            }
            if (inv2) {
                drawWhiteNode(g2, in2[0] + 4, in2[1]);
                in2[0] += 8;
            }
        }
        return new int[][]{in1, in2, out};
    }

    private void drawWhiteNode(Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - 4, cy - 4, 8, 8);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(cx - 4, cy - 4, 8, 8);
    }

    private int[][] drawAnd3(Graphics2D g2, int x, int y, Direction dir) {
        drawAnd(g2, x, y, dir, 0, false, false);
        if (dir == Direction.RIGHT) {
            return new int[][]{ {x - 15, y - 8}, {x - 15, y}, {x - 15, y + 8}, {x + 15, y} };
        } else {
            return new int[][]{ {x + 15, y - 8}, {x + 15, y}, {x + 15, y + 8}, {x - 15, y} };
        }
    }

    /**
     * Draw a 4-input AND gate (taller body: 40px high, 30px wide).
     * Returns {{in1}, {in2}, {in3}, {in4}, {out}}.
     * Inputs are evenly spaced at y-12, y-4, y+4, y+12.
     */
    private int[][] drawAnd4(Graphics2D g2, int x, int y, Direction dir, boolean invertIn4) {
        Path2D path = new Path2D.Double();
        if (dir == Direction.RIGHT) {
            path.moveTo(x - 15, y - 20);
            path.lineTo(x, y - 20);
            path.append(new Arc2D.Double(x - 20, y - 20, 40, 40, 90, -180, Arc2D.OPEN), true);
            path.lineTo(x - 15, y + 20);
            path.closePath();
        } else {
            path.moveTo(x + 15, y - 20);
            path.lineTo(x, y - 20);
            path.append(new Arc2D.Double(x - 20, y - 20, 40, 40, 90, 180, Arc2D.OPEN), true);
            path.lineTo(x + 15, y + 20);
            path.closePath();
        }
        g2.setColor(Color.WHITE);
        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(path);

        if (dir == Direction.RIGHT) {
            int[] in1 = {x - 15, y - 12};
            int[] in2 = {x - 15, y - 4};
            int[] in3 = {x - 15, y + 4};
            int[] in4 = {x - 15, y + 12};
            int[] out = {x + 20, y};
            if (invertIn4) {
                drawWhiteNode(g2, in4[0] - 4, in4[1]);
                in4 = new int[]{in4[0] - 8, in4[1]};
            }
            return new int[][]{in1, in2, in3, in4, out};
        } else {
            int[] in1 = {x + 15, y - 12};
            int[] in2 = {x + 15, y - 4};
            int[] in3 = {x + 15, y + 4};
            int[] in4 = {x + 15, y + 12};
            int[] out = {x - 20, y};
            if (invertIn4) {
                drawWhiteNode(g2, in4[0] + 4, in4[1]);
                in4 = new int[]{in4[0] + 8, in4[1]};
            }
            return new int[][]{in1, in2, in3, in4, out};
        }
    }

    private int[] getDropXs(int startX, String[] labels, Direction dir, int padding) {
        int[] xs = new int[labels.length];
        double currX = startX;
        for (int i = 0; i < labels.length; i++) {
            if (i == 0) {
                xs[i] = (int) currX;
            } else {
                int prevLen = labels[i - 1].length();
                int currLen = labels[i].length();
                double dist = (prevLen + currLen) / 2.0 * 9 + padding;
                if (dir == Direction.LEFT) {
                    currX -= dist;
                } else {
                    currX += dist;
                }
                xs[i] = (int) currX;
            }
        }
        return xs;
    }

    private int[][] drawOr(Graphics2D g2, int x, int y, Direction dir) {
        if (dir == Direction.RIGHT) {
            Path2D path = new Path2D.Double();
            path.moveTo(x - 15, y - 15);
            path.quadTo(x - 5, y - 15, x + 15, y);
            path.quadTo(x - 5, y + 15, x - 15, y + 15);
            path.quadTo(x - 7, y, x - 15, y - 15);
            path.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(path);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.draw(path);
            return new int[][]{ {x - 10, y - 8}, {x - 10, y + 8}, {x + 15, y} };
        }
        return null;
    }

    private int[][] drawBuffer(Graphics2D g2, int x, int y, Direction dir, String enSide) {
        if (dir == Direction.UP) {
            Path2D path = new Path2D.Double();
            path.moveTo(x - 12, y + 10);
            path.lineTo(x + 12, y + 10);
            path.lineTo(x, y - 15);
            path.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(path);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.draw(path);
            int enX = enSide.equals("right") ? x + 6 : x - 6;
            return new int[][]{ {x, y + 10}, {enX, y}, {x, y - 15} };
        }
        return null;
    }

    private int[][] drawNotOC(Graphics2D g2, int x, int y, Direction dir) {
        if (dir == Direction.UP) {
            Path2D path = new Path2D.Double();
            path.moveTo(x - 12, y + 10);
            path.lineTo(x + 12, y + 10);
            path.lineTo(x, y - 10);
            path.closePath();
            g2.setColor(Color.WHITE);
            g2.fill(path);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.draw(path);
            drawWhiteNode(g2, x, y - 14);
            drawText(g2, x + 18, y + 5, "OC", TextAlign.START, false);
            return new int[][]{ {x, y + 10}, {x, y - 18} };
        }
        return null;
    }

    private int[][] drawDecoder(Graphics2D g2, int x, int y, int wTop, int wBot, int h) {
        Path2D path = new Path2D.Double();
        path.moveTo(x - wTop / 2.0, y);
        path.lineTo(x + wTop / 2.0, y);
        path.lineTo(x + wBot / 2.0, y + h);
        path.lineTo(x - wBot / 2.0, y + h);
        path.closePath();
        g2.setColor(Color.WHITE);
        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(path);
        drawText(g2, x, y + h / 2, "DECODER", TextAlign.MIDDLE, false);
        return new int[][]{ {x, y}, {x, y + h} };
    }

    private void drawHexagon(Graphics2D g2, int x, int y, int r, String text) {
        Path2D path = new Path2D.Double();
        path.moveTo(x - r, y);
        path.lineTo(x - r / 2.0, y - r * 0.866);
        path.lineTo(x + r / 2.0, y - r * 0.866);
        path.lineTo(x + r, y);
        path.lineTo(x + r / 2.0, y + r * 0.866);
        path.lineTo(x - r / 2.0, y + r * 0.866);
        path.closePath();
        g2.setColor(Color.WHITE);
        g2.fill(path);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(path);
        drawText(g2, x, y, text, TextAlign.MIDDLE, false);
    }

    private void drawBusStroke(Graphics2D g2, int x, int y, String widthText) {
        drawLine(g2, x - 4, y + 4, x + 4, y - 4);
        drawText(g2, x + 8, y - 5, widthText, TextAlign.START, false);
    }
}
