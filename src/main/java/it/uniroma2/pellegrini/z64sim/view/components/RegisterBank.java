/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view.components;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.uniroma2.pellegrini.z64sim.isa.registers.FlagsRegister;
import it.uniroma2.pellegrini.z64sim.model.CpuState;

import it.uniroma2.pellegrini.z64sim.view.View;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RegisterBank extends View implements PropertyChangeListener {

    private JPanel multicycleCPU;
    private JLabel rsp;
    private JLabel rbp;
    private JLabel rsi;
    private JLabel rdi;
    private JLabel r8;
    private JLabel r9;
    private JLabel r10;
    private JLabel r11;
    private JLabel r12;
    private JLabel r13;
    private JLabel r14;
    private JLabel r15;
    private JLabel rip;
    private JLabel rflags;
    private JLabel rax;
    private JLabel rcx;
    private JLabel rdx;
    private JLabel rbx;
    private JLabel regFormatLabel;
    private JRadioButton regFormatHex;
    private JRadioButton regFormatDec;

    /**
     * Maps each CpuState property name to its corresponding display label.
     */
    private final Map<String, JLabel> registerLabels = new HashMap<>();

    /**
     * Retained so the format-toggle listener can re-read live register values.
     */
    private CpuState boundCpuState;


    /**
     * Bind this RegisterBank to a CpuState instance, listening for property changes
     * on all registers, RIP, and flags. UI updates are marshalled to the EDT.
     */
    public void bind(CpuState cpuState) {
        this.boundCpuState = cpuState;

        // Build the property-name --> label lookup table (FLAGS is intentionally absent).
        registerLabels.put(CpuState.PROP_RAX, rax);
        registerLabels.put(CpuState.PROP_RCX, rcx);
        registerLabels.put(CpuState.PROP_RDX, rdx);
        registerLabels.put(CpuState.PROP_RBX, rbx);
        registerLabels.put(CpuState.PROP_RSP, rsp);
        registerLabels.put(CpuState.PROP_RBP, rbp);
        registerLabels.put(CpuState.PROP_RSI, rsi);
        registerLabels.put(CpuState.PROP_RDI, rdi);
        registerLabels.put(CpuState.PROP_R8, r8);
        registerLabels.put(CpuState.PROP_R9, r9);
        registerLabels.put(CpuState.PROP_R10, r10);
        registerLabels.put(CpuState.PROP_R11, r11);
        registerLabels.put(CpuState.PROP_R12, r12);
        registerLabels.put(CpuState.PROP_R13, r13);
        registerLabels.put(CpuState.PROP_R14, r14);
        registerLabels.put(CpuState.PROP_R15, r15);
        registerLabels.put(CpuState.PROP_RIP, rip);

        // Use a monospaced font so that both hex (20 digits) and decimal (20 digits)
        // produce identical pixel widths, preventing any layout shift on format change.
        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, rax.getFont().getSize());
        for (JLabel label : registerLabels.values()) {
            label.setFont(monoFont);
        }

        // Enforce mutual exclusion between the two format radio buttons.
        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(regFormatHex);
        formatGroup.add(regFormatDec);

        // When the format changes, re-render every register label.
        ActionListener onFormatChange = e -> refreshAllRegisters();
        regFormatHex.addActionListener(onFormatChange);
        regFormatDec.addActionListener(onFormatChange);

        // Listen for register and RIP changes via CpuState's PropertyChangeSupport.
        cpuState.addPropertyChangeListener(this);
        // Listen for flag bit changes directly on the FlagsRegister.
        cpuState.getFlagsRegister().addPropertyChangeListener(this);
    }

    /**
     * Re-reads every general-purpose register from the bound CpuState and
     * updates the display labels using the currently selected format.
     * Called when the user toggles the hex/dec radio buttons.
     */
    private void refreshAllRegisters() {
        if (boundCpuState == null) return;
        rax.setText(formatRegister(boundCpuState.getRAX()));
        rcx.setText(formatRegister(boundCpuState.getRCX()));
        rdx.setText(formatRegister(boundCpuState.getRDX()));
        rbx.setText(formatRegister(boundCpuState.getRBX()));
        rsp.setText(formatRegister(boundCpuState.getRSP()));
        rbp.setText(formatRegister(boundCpuState.getRBP()));
        rsi.setText(formatRegister(boundCpuState.getRSI()));
        rdi.setText(formatRegister(boundCpuState.getRDI()));
        r8.setText(formatRegister(boundCpuState.getR8()));
        r9.setText(formatRegister(boundCpuState.getR9()));
        r10.setText(formatRegister(boundCpuState.getR10()));
        r11.setText(formatRegister(boundCpuState.getR11()));
        r12.setText(formatRegister(boundCpuState.getR12()));
        r13.setText(formatRegister(boundCpuState.getR13()));
        r14.setText(formatRegister(boundCpuState.getR14()));
        r15.setText(formatRegister(boundCpuState.getR15()));
        rip.setText(formatRegister(boundCpuState.getRIP()));
    }

    /**
     * Formats a register value according to the currently selected display mode.
     * Both formats produce exactly 20 characters, ensuring identical label widths.
     *
     * @param value the raw 64-bit register value
     * @return the formatted string (20-digit hex or 20-digit decimal)
     */
    private String formatRegister(long value) {
        if (regFormatHex.isSelected()) {
            return String.format("%020X", value);
        } else {
            return String.format("%020d", value);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Marshal all UI updates to the EDT
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> handlePropertyChange(evt));
        } else {
            handlePropertyChange(evt);
        }
    }

    private void handlePropertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String property = evt.getPropertyName();

        if (source instanceof CpuState) {
            if (CpuState.PROP_FLAGS.equals(property)) {
                updateFlagsDisplay((CpuState) source);
                return;
            }
            JLabel label = registerLabels.get(property);
            if (label != null) {
                label.setText(formatRegister((Long) evt.getNewValue()));
            }
        } else if (source instanceof FlagsRegister) {
            updateFlagsDisplay((Long) evt.getNewValue());
        }
    }

    /**
     * Updates the RFLAGS display from a live {@link CpuState}.
     */
    private void updateFlagsDisplay(CpuState cpuState) {
        updateFlagsDisplay(cpuState.getFlags());
    }

    /**
     * Updates the RFLAGS display from a raw 64-bit flags value.
     */
    private void updateFlagsDisplay(long value) {
        boolean OF = (value & (1 << 11)) != 0;
        boolean DF = (value & (1 << 10)) != 0;
        boolean IF = (value & (1 << 9)) != 0;
        boolean SF = (value & (1 << 7)) != 0;
        boolean ZF = (value & (1 << 6)) != 0;
        boolean PF = (value & (1 << 2)) != 0;
        boolean CF = (value & 1) != 0;
        this.rflags.setText(
            String.format("%05d ", value)
                + (OF ? "[OF]" : "")
                + (DF ? "[DF]" : "")
                + (IF ? "[IF]" : "")
                + (SF ? "[SF]" : "")
                + (ZF ? "[ZF]" : "")
                + (PF ? "[PF]" : "")
                + (CF ? "[CF]" : "")
        );
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        multicycleCPU = new JPanel();
        multicycleCPU.setLayout(new GridLayoutManager(7, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "RAX", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rax = new JLabel();
        rax.setText("00000000000000000000");
        panel1.add(rax, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(null, "RCX", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rcx = new JLabel();
        rcx.setText("00000000000000000000");
        panel2.add(rcx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(null, "RDX", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rdx = new JLabel();
        rdx.setText("00000000000000000000");
        panel3.add(rdx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(null, "R12", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r12 = new JLabel();
        r12.setText("00000000000000000000");
        panel4.add(r12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel5, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(null, "RFLAGS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rflags = new JLabel();
        rflags.setText("00002");
        panel5.add(rflags, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(148, 20), null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel6, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(null, "R13", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r13 = new JLabel();
        r13.setText("00000000000000000000");
        panel6.add(r13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel7, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel7.setBorder(BorderFactory.createTitledBorder(null, "R14", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r14 = new JLabel();
        r14.setText("00000000000000000000");
        panel7.add(r14, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder(null, "RBX", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rbx = new JLabel();
        rbx.setText("00000000000000000000");
        panel8.add(rbx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel9, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel9.setBorder(BorderFactory.createTitledBorder(null, "RSP", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rsp = new JLabel();
        rsp.setText("00000000000000000000");
        panel9.add(rsp, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel10, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(null, "RBP", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rbp = new JLabel();
        rbp.setText("00000000000000000000");
        panel10.add(rbp, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel11, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel11.setBorder(BorderFactory.createTitledBorder(null, "RSI", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rsi = new JLabel();
        rsi.setText("00000000000000000000");
        panel11.add(rsi, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel12, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel12.setBorder(BorderFactory.createTitledBorder(null, "RDI", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rdi = new JLabel();
        rdi.setText("00000000000000000000");
        panel12.add(rdi, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel13, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel13.setBorder(BorderFactory.createTitledBorder(null, "R8", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r8 = new JLabel();
        r8.setText("00000000000000000000");
        panel13.add(r8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel14, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel14.setBorder(BorderFactory.createTitledBorder(null, "R9", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r9 = new JLabel();
        r9.setText("00000000000000000000");
        panel14.add(r9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel15, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel15.setBorder(BorderFactory.createTitledBorder(null, "R10", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r10 = new JLabel();
        r10.setText("00000000000000000000");
        panel15.add(r10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel16, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel16.setBorder(BorderFactory.createTitledBorder(null, "R11", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r11 = new JLabel();
        r11.setText("00000000000000000000");
        panel16.add(r11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel17, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel17.setBorder(BorderFactory.createTitledBorder(null, "RIP", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        rip = new JLabel();
        rip.setText("00000000000000000000");
        panel17.add(rip, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel18 = new JPanel();
        panel18.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel18, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(150, 51), null, 0, false));
        panel18.setBorder(BorderFactory.createTitledBorder(null, "R15", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        r15 = new JLabel();
        r15.setText("00000000000000000000");
        panel18.add(r15, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(140, 20), null, 0, false));
        final JPanel panel19 = new JPanel();
        panel19.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        multicycleCPU.add(panel19, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        regFormatLabel = new JLabel();
        this.$$$loadLabelText$$$(regFormatLabel, this.$$$getMessageFromBundle$$$("i18n", "gui.reg.format"));
        panel19.add(regFormatLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), null, null, 0, false));
        regFormatHex = new JRadioButton();
        regFormatHex.setSelected(true);
        regFormatHex.setText("hex");
        panel19.add(regFormatHex, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), null, null, 0, false));
        regFormatDec = new JRadioButton();
        regFormatDec.setText("dec");
        panel19.add(regFormatDec, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(50, -1), null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel19.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, 1, new Dimension(10, -1), new Dimension(10, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        multicycleCPU.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        multicycleCPU.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    /**
     * @noinspection ALL
     */
    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return multicycleCPU;
    }

    private void createUIComponents() {
    }
}
