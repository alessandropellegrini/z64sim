/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.MicrocodeGenerator;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Modal dialog that displays the binary encoding breakdown of a single
 * machine instruction. Opened by double-clicking an instruction row in
 * the Memory Table.
 */
public class InstructionInspector extends JDialog {
    private JLabel opcode;
    private JTextArea muOps;
    private JLabel mode;
    private JLabel sib;
    private JLabel rm;
    private JLabel insnMnemonic;
    private JLabel displacement;
    private JPanel extendedInstructionPanel;
    private JLabel immediate;
    private JPanel mainPanel;
    private JLabel hexEncoding;

    private final JTable memoryView;
    private final ListSelectionListener selectionListener;
    private Instruction currentInsn;

    public InstructionInspector(Window owner, Instruction insn, long address, JTable memoryView) {
        super(owner, "Instruction Inspector", ModalityType.MODELESS);
        this.memoryView = memoryView;

        $$$setupUI$$$();
        setContentPane(mainPanel);

        // Use monospace font for all labels and the micro-ops area
        Font mono = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        Font monoBold = new Font(Font.MONOSPACED, Font.BOLD, 16);
        opcode.setFont(mono);
        mode.setFont(mono);
        sib.setFont(mono);
        rm.setFont(mono);
        displacement.setFont(mono);
        immediate.setFont(mono);
        hexEncoding.setFont(mono);
        insnMnemonic.setFont(monoBold);
        muOps.setFont(mono);
        muOps.setEditable(false);

        // µ-ops animation button below the micro-ops area
        JButton muOpsAnimButton = new JButton();
        muOpsAnimButton.setIcon(new ImageIcon(getClass().getResource("/images/mu.png")));
        muOpsAnimButton.setToolTipText(PropertyBroker.getMessageFromBundle("inspect.muops"));
        muOpsAnimButton.addActionListener(e -> {
            if (currentInsn != null) {
                MuOpAnimationDialog dialog = new MuOpAnimationDialog(this, currentInsn);
                dialog.setVisible(true);
            }
        });
        // Add right-aligned button panel after the µ-ops scroll pane
        JPanel muOpsButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        muOpsButtonPanel.add(muOpsAnimButton);
        mainPanel.add(muOpsButtonPanel, new GridConstraints(5, 0, 1, 1,
                GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));

        // Populate from the initially double-clicked instruction
        populateFromInstruction(insn);

        // Listen for selection changes on the memory table
        selectionListener = this::onMemorySelectionChanged;
        memoryView.getSelectionModel().addListSelectionListener(selectionListener);

        // Close on Escape
        mainPanel.registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        // Close on window X — also removes the selection listener
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Update the dialog fields from the given instruction.
     */
    private void populateFromInstruction(Instruction insn) {
        insnMnemonic.setText(insn.toString());

        byte[] encoded = insn.getValue();

        // Set the four main byte fields
        opcode.setText(formatByte(encoded[0]));
        mode.setText(formatByte(encoded[1]));
        sib.setText(formatByte(encoded[2]));
        rm.setText(formatByte(encoded[3]));

        // Displacement / Short Immediate (bytes 4–7)
        StringBuilder dispStr = new StringBuilder();
        for (int i = 4; i < 8; i++) {
            if (i > 4) dispStr.append(" ");
            dispStr.append(formatByte(encoded[i]));
        }
        displacement.setText(dispStr.toString());

        // Hex encoding of the full instruction
        StringBuilder hexStr = new StringBuilder();
        for (int i = 0; i < encoded.length; i++) {
            if (i > 0) hexStr.append(" ");
            hexStr.append(String.format("%02x", encoded[i] & 0xFF));
        }
        hexEncoding.setText(hexStr.toString());

        // Immediate (bytes 8–15) — only for 16-byte instructions
        if (insn.getSize() == 16 && encoded.length >= 16) {
            StringBuilder immStr = new StringBuilder();
            for (int i = 8; i < 16; i++) {
                if (i > 8) immStr.append(" ");
                immStr.append(formatByte(encoded[i]));
            }
            immediate.setText(immStr.toString());
            extendedInstructionPanel.setVisible(true);
        } else {
            extendedInstructionPanel.setVisible(false);
        }

        // Microoperations
        java.util.List<String> muOpsList = MicrocodeGenerator.generate(insn);
        StringBuilder muOpsText = new StringBuilder();
        for (int i = 0; i < muOpsList.size(); i++) {
            if (i > 0) muOpsText.append("\n");
            muOpsText.append(muOpsList.get(i));
        }
        this.muOps.setText(muOpsText.toString());
        this.muOps.setCaretPosition(0);
        this.currentInsn = insn;

        pack();
    }

    /**
     * Format a byte as two space-separated 4-bit groups: e.g. 0x10 → "0001 0000"
     */
    private static String formatByte(byte b) {
        int val = b & 0xFF;
        String bits = String.format("%8s", Integer.toBinaryString(val)).replace(' ', '0');
        return bits.substring(0, 4) + " " + bits.substring(4, 8);
    }


    /**
     * Called when the memory table selection changes while the dialog is open.
     */
    private void onMemorySelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = memoryView.getSelectedRow();
        if (row < 0) return;
        long address = row * 8L;
        MemoryElement elem = Memory.getMemoryElementAt(address);
        if (elem instanceof Instruction) {
            populateFromInstruction((Instruction) elem);
        }
    }

    private void cleanup() {
        memoryView.getSelectionModel().removeListSelectionListener(selectionListener);
    }

    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(7, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, -1), null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Opcode", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        opcode = new JLabel();
        opcode.setText("0000 0000");
        panel2.add(opcode, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, -1), null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Mode", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        mode = new JLabel();
        mode.setText("0000 0000");
        panel3.add(mode, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, -1), null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "SIB", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        sib = new JLabel();
        sib.setText("0000 0000");
        panel4.add(sib, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(100, -1), null, null, 0, false));
        panel5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "R/M", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        rm = new JLabel();
        rm.setText("0000 0000");
        panel5.add(rm, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel6, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(350, -1), null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Displacement / Short Immediate", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        displacement = new JLabel();
        displacement.setText("0000 0000 0000 0000 0000 0000 0000 0000 ");
        panel6.add(displacement, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(muOps);
        final Spacer spacer1 = new Spacer();
        mainPanel.add(spacer1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        insnMnemonic = new JLabel();
        Font insnMnemonicFont = this.$$$getFont$$$(null, Font.BOLD, 16, insnMnemonic.getFont());
        if (insnMnemonicFont != null) insnMnemonic.setFont(insnMnemonicFont);
        insnMnemonic.setText("movl %eax, %ebx");
        mainPanel.add(insnMnemonic, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extendedInstructionPanel = new JPanel();
        extendedInstructionPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(extendedInstructionPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        extendedInstructionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Immediate", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
        immediate = new JLabel();
        immediate.setText("0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 ");
        extendedInstructionPanel.add(immediate, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hexEncoding = new JLabel();
        Font hexEncodingFont = this.$$$getFont$$$(null, -1, 14, hexEncoding.getFont());
        if (hexEncodingFont != null) hexEncoding.setFont(hexEncodingFont);
        hexEncoding.setText("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
        mainPanel.add(hexEncoding, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("i18n", "microprogram"));
        mainPanel.add(label1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
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
        return mainPanel;
    }


    private void createUIComponents() {
        muOps = new JTextArea();
    }

}
