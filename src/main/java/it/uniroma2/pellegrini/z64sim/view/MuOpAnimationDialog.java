/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.isa.instructions.*;
import it.uniroma2.pellegrini.z64sim.isa.operands.*;
import it.uniroma2.pellegrini.z64sim.isa.registers.*;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MuOpAnimationDialog extends JDialog {

    private JComboBox<ComboItem> instructionCombo;
    private ArchitectureCanvas canvas;
    private JLabel muOpLabel;
    private JLabel signalsLabel;
    private JLabel stepLabel;
    private JButton prevBtn, nextBtn, playBtn;
    private JSlider speedSlider;
    private JPanel topPanel;
    private Timer timer;

    private List<String> currentOps = new ArrayList<>();
    private int currentStep = -1;

    public MuOpAnimationDialog(Window parent) {
        super(parent, PropertyBroker.getMessageFromBundle("muop.dialog.title"), ModalityType.MODELESS);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadInstructions();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Creates the dialog preloaded with a specific instruction's µ-ops.
     * The instruction combobox is hidden since the instruction is fixed.
     */
    public MuOpAnimationDialog(Window parent, Instruction insn) {
        super(parent, PropertyBroker.getMessageFromBundle("muop.dialog.title") + " \u2014 " + insn.toString(), ModalityType.MODELESS);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        // Hide the combobox panel — instruction is fixed
        topPanel.setVisible(false);
        loadMicrocode(insn);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        instructionCombo = new JComboBox<>();
        instructionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ComboItem) {
                    ComboItem item = (ComboItem) value;
                    if (item.isSeparator) {
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        lbl.setEnabled(false);
                    } else {
                        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                        lbl.setEnabled(true);
                    }
                    lbl.setText(item.text);
                }
                return lbl;
            }
        });
        instructionCombo.addActionListener(e -> {
            ComboItem item = (ComboItem) instructionCombo.getSelectedItem();
            if (item != null && !item.isSeparator) {
                loadMicrocode(item.insn);
            } else {
                if (instructionCombo.getSelectedIndex() + 1 < instructionCombo.getItemCount()) {
                    instructionCombo.setSelectedIndex(instructionCombo.getSelectedIndex() + 1);
                }
            }
        });

        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel(PropertyBroker.getMessageFromBundle("muop.instruction.label")));
        topPanel.add(instructionCombo);
        add(topPanel, BorderLayout.NORTH);

        canvas = new ArchitectureCanvas();
        canvas.setPreferredSize(new Dimension(1020, 700));
        add(canvas, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        muOpLabel = new JLabel(PropertyBroker.getMessageFromBundle("muop.prefix") + " ");
        muOpLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        muOpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        signalsLabel = new JLabel(PropertyBroker.getMessageFromBundle("muop.signals.prefix") + " ");
        signalsLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        signalsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textPanel.add(muOpLabel);
        textPanel.add(signalsLabel);

        JPanel ctrlPanel = new JPanel(new FlowLayout());
        prevBtn = new JButton("\u25C0");
        nextBtn = new JButton("\u25B6");
        playBtn = new JButton("\u25B6\u25B6 / \u23F8");
        stepLabel = new JLabel(PropertyBroker.getMessageFromBundle("muop.step.counter", "0", "0"));
        speedSlider = new JSlider(100, 3000, 1000);
        speedSlider.setInverted(true); // Left=Fast, Right=Slow

        prevBtn.addActionListener(e -> { if (currentStep > 0) setStep(currentStep - 1); });
        nextBtn.addActionListener(e -> { if (!currentOps.isEmpty() && currentStep < currentOps.size() - 1) setStep(currentStep + 1); });
        playBtn.addActionListener(e -> togglePlay());

        ctrlPanel.add(prevBtn);
        ctrlPanel.add(playBtn);
        ctrlPanel.add(nextBtn);
        ctrlPanel.add(stepLabel);
        ctrlPanel.add(new JLabel(PropertyBroker.getMessageFromBundle("muop.speed.label")));
        ctrlPanel.add(speedSlider);

        bottomPanel.add(textPanel);
        bottomPanel.add(ctrlPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> {
            if (currentStep < currentOps.size() - 1) {
                setStep(currentStep + 1);
            } else {
                togglePlay();
            }
        });
    }

    private void togglePlay() {
        if (timer.isRunning()) {
            timer.stop();
        } else {
            if (currentOps.isEmpty()) return;
            if (currentStep >= currentOps.size() - 1) {
                setStep(-1);
            }
            timer.setDelay(speedSlider.getValue());
            timer.start();
        }
    }

    private void loadMicrocode(Instruction insn) {
        if (timer.isRunning()) timer.stop();
        currentOps = expandMultiCycle(MicrocodeGenerator.generate(insn));
        currentStep = -1;
        muOpLabel.setText(PropertyBroker.getMessageFromBundle("muop.prefix") + " " + PropertyBroker.getMessageFromBundle("muop.press.to.start"));
        signalsLabel.setText(PropertyBroker.getMessageFromBundle("muop.signals.prefix") + " ");
        stepLabel.setText(PropertyBroker.getMessageFromBundle("muop.step.counter", "0", String.valueOf(currentOps.size())));
        canvas.setHighlightedGroups(new ArrayList<>());
    }

    /**
     * Expands multi-clock-cycle µ-ops into individual sub-step frames.
     * Memory accesses take 3 clock cycles with progressive signal activation.
     */
    private static List<String> expandMultiCycle(List<String> ops) {
        List<String> expanded = new ArrayList<>();
        for (String op : ops) {
            if (op.equals("MDR \u2190 (MAR); RIP \u2190 RIP + 8")
                    || op.equals("MDR \u2190 (MAR)")
                    || op.equals("(MAR) \u2190 MDR")) {
                expanded.add(op + " [1/3]");
                expanded.add(op + " [2/3]");
                expanded.add(op + " [3/3]");
            } else {
                expanded.add(op);
            }
        }
        return expanded;
    }

    private void setStep(int step) {
        if (currentOps == null || currentOps.isEmpty() || step < 0) {
            muOpLabel.setText(currentOps.isEmpty() ? PropertyBroker.getMessageFromBundle("muop.prefix") + " " : PropertyBroker.getMessageFromBundle("muop.prefix") + " " + PropertyBroker.getMessageFromBundle("muop.press.to.start"));
            signalsLabel.setText(PropertyBroker.getMessageFromBundle("muop.signals.prefix") + " ");
            stepLabel.setText(PropertyBroker.getMessageFromBundle("muop.step.counter", "0", String.valueOf(currentOps == null ? 0 : currentOps.size())));
            canvas.setHighlightedGroups(new ArrayList<>());
            currentStep = -1;
            return;
        }
        if (step >= currentOps.size()) step = currentOps.size() - 1;
        currentStep = step;

        String op = currentOps.get(currentStep);
        muOpLabel.setText(PropertyBroker.getMessageFromBundle("muop.prefix") + " " + op);
        stepLabel.setText(PropertyBroker.getMessageFromBundle("muop.step.counter", String.valueOf(currentStep + 1), String.valueOf(currentOps.size())));

        MuOpOverlayMapper.MuOpMapping mapping = MuOpOverlayMapper.map(op);
        signalsLabel.setText(PropertyBroker.getMessageFromBundle("muop.signals.prefix") + " " + mapping.signals);
        canvas.setHighlightedGroups(mapping.overlays);
    }

    private void loadInstructions() {
        try {
            addPlaceholder();

            // --- Class 0 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class0.system"));
            addC0("hlt", null);
            addC0("nop", null);
            addC0("int", new OperandImmediate(0x80));

            // --- Class 1 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class1.data"));
            addC1("movq", new OperandRegister(Register.RAX, 8), new OperandRegister(Register.RCX, 8), -1);
            addC1("movq", new OperandRegister(Register.RAX, 8), new OperandMemory(Register.RCX, 8, -1, -1, 0x100, 8), -1);
            addC1("movq", new OperandMemory(Register.RCX, 8, -1, -1, 0x100, 8), new OperandRegister(Register.RAX, 8), -1);
            addC1("movq", new OperandImmediate(42), new OperandRegister(Register.RAX, 8), -1);
            addC1("leaq", new OperandMemory(Register.RCX, 8, -1, -1, 0x100, 8), new OperandRegister(Register.RAX, 8), -1);
            addC1("pushq", new OperandRegister(Register.RAX, 8), null, 8);
            addC1("pushq", new OperandImmediate(42), null, 8);
            addC1("popq", null, new OperandRegister(Register.RCX, 8), 8);
            addC1("pushf", null, null, 8);
            addC1("popf", null, null, 8);

            // --- Class 2 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class2.alu"));
            String[] class2 = {"addq", "subq", "adcq", "sbbq", "cmpq", "testq", "andq", "orq", "xorq", "btq"};
            for (String m : class2) {
                addC2(m, new OperandRegister(Register.RAX, 8), new OperandRegister(Register.RCX, 8));
            }
            addC2("negq", null, new OperandRegister(Register.RAX, 8));
            addC2("notq", null, new OperandRegister(Register.RAX, 8));
            addC2("mulq", null, new OperandRegister(Register.RCX, 8));
            addC2("imulq", null, new OperandRegister(Register.RCX, 8));
            addC2("divq", null, new OperandRegister(Register.RCX, 8));
            addC2("idivq", null, new OperandRegister(Register.RCX, 8));

            // --- Class 3 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class3.shift"));
            String[] class3 = {"salq", "sarq", "shrq", "rclq", "rcrq", "rolq", "rorq"};
            for (String m : class3) {
                addC3(m, 3, new OperandRegister(Register.RAX, 8));
            }

            // --- Class 4 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class4.flags"));
            String[] class4 = {"clc", "stc", "clp", "stp", "clz", "stz", "cls", "sts", "cli", "sti", "cld", "std", "clo", "sto"};
            for (String m : class4) {
                addC4(m);
            }

            // --- Class 5 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class5.control"));
            addC5("jmp", new OperandMemory(-1, -1, -1, -1, 0x1000, 8));
            addC5("jmp", new OperandRegister(Register.RAX, 8));
            addC5("call", new OperandMemory(-1, -1, -1, -1, 0x1000, 8));
            addC5("ret", null);
            addC5("iret", null);

            // --- Class 6 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class6.conditional"));
            String[] class6 = {"jc", "jnc", "jp", "jnp", "jz", "jnz", "js", "jns", "jo", "jno"};
            for (String m : class6) {
                addC6(m, new OperandMemory(-1, -1, -1, -1, 0x100, 8));
            }

            // --- Class 7 ---
            addSep(PropertyBroker.getMessageFromBundle("muop.class7.io"));
            addC7("inb", 1, new OperandImmediate(0x60));
            addC7("inb", 1, null);
            addC7("outb", 1, new OperandImmediate(0x60));
            addC7("outb", 1, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (instructionCombo.getItemCount() > 0) {
            instructionCombo.setSelectedIndex(0);
        }
    }

    private void addSep(String text) {
        instructionCombo.addItem(new ComboItem(true, text, null));
    }
    private void addPlaceholder() {
        instructionCombo.addItem(new ComboItem(true, PropertyBroker.getMessageFromBundle("muop.select.instruction"), null));
    }
    private void addC0(String m, OperandImmediate ivn) throws Exception {
        instructionCombo.addItem(new ComboItem(false, m + (ivn != null ? " $0x80" : ""), new InstructionClass0(m, ivn)));
    }
    private void addC1(String m, Operand s, Operand d, int size) throws Exception {
        String txt = m + (s != null ? " " + s : "") + (d != null ? ", " + d : "");
        if (m.equals("pushf") || m.equals("popf")) txt = m;
        String base = m.equals("pushf") || m.equals("popf") ? m : (m.endsWith("q") ? m.substring(0, m.length()-1) : m);
        instructionCombo.addItem(new ComboItem(false, txt, new InstructionClass1(base, s, d, size)));
    }
    private void addC2(String m, Operand s, Operand d) throws Exception {
        String txt = m + (s != null ? " " + s : "") + (d != null ? ", " + d : "");
        String base = m.endsWith("q") ? m.substring(0, m.length()-1) : m;
        instructionCombo.addItem(new ComboItem(false, txt, new InstructionClass2(base, s, d)));
    }
    private void addC3(String m, int p, OperandRegister r) {
        String base = m.endsWith("q") ? m.substring(0, m.length()-1) : m;
        instructionCombo.addItem(new ComboItem(false, m + " $" + p + ", " + r, new InstructionClass3(base, p, r)));
    }
    private void addC4(String m) {
        instructionCombo.addItem(new ComboItem(false, m, new InstructionClass4(m)));
    }
    private void addC5(String m, Operand t) {
        String txt;
        if (t instanceof OperandImmediate) {
            txt = m + " 0x" + Long.toHexString(((OperandImmediate) t).getValue());
        } else if (t instanceof OperandMemory && ((OperandMemory) t).getBase() == -1 && ((OperandMemory) t).getIndex() == -1) {
            txt = m + " 0x" + Long.toHexString(((OperandMemory) t).getDisplacement());
        } else {
            txt = m + (t != null ? " " + t : "");
        }
        instructionCombo.addItem(new ComboItem(false, txt, new InstructionClass5(m, t)));
    }
    private void addC6(String m, OperandMemory t) throws Exception {
        instructionCombo.addItem(new ComboItem(false, m + " 0x100", new InstructionClass6(m, t)));
    }
    private void addC7(String m, int size, Operand ioport) throws Exception {
        String txt = m + (ioport != null ? " " + ioport : "");
        String base = m.endsWith("b") ? m.substring(0, m.length()-1) : m;
        instructionCombo.addItem(new ComboItem(false, txt, new InstructionClass7(base, size, ioport)));
    }

    private static class ComboItem {
        final boolean isSeparator;
        final String text;
        final Instruction insn;

        ComboItem(boolean isSeparator, String text, Instruction insn) {
            this.isSeparator = isSeparator;
            this.text = text;
            this.insn = insn;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    // =====================================================================
    // SVG-based architecture canvas
    // =====================================================================

    /**
     * Renders the z64 architecture SVG with highlighted groups.
     * <p>
     * The base SVG is loaded once as a W3C DOM. For each animation frame,
     * the DOM is cloned, selected groups get a red stroke/fill style override,
     * the modified XML is serialized, and JSVG renders it to Graphics2D.
     */
    private static class ArchitectureCanvas extends JPanel {

        /** The highlight colour applied to active µ-op groups */
        private static final String HIGHLIGHT_COLOR = "#C62828";

        /** Cached base SVG XML (parsed once at construction) */
        private Document baseSvgDom;
        /** Pre-serialized base SVG bytes (no highlights) for the default view */
        private byte[] baseSvgBytes;
        /** The JSVG document for the current frame */
        private SVGDocument currentSvg;
        /** Intrinsic SVG dimensions */
        private float svgW, svgH;
        /** XML transformer (reused) */
        private Transformer transformer;

        public ArchitectureCanvas() {
            try {
                // Parse the SVG into a W3C DOM
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                InputStream svgStream = getClass().getResourceAsStream("/images/z64-arch.svg");
                if (svgStream == null) {
                    throw new RuntimeException("SVG resource not found: /images/z64-arch.svg");
                }
                baseSvgDom = dbf.newDocumentBuilder().parse(svgStream);
                svgStream.close();

                // Register 'id' attributes so getElementById() works
                // (non-validating parsers don't know 'id' is an ID attribute)
                registerIdAttributes(baseSvgDom.getDocumentElement());

                // Cache serialized base bytes
                transformer = TransformerFactory.newInstance().newTransformer();
                baseSvgBytes = serializeDom(baseSvgDom);

                // Load the base SVG with JSVG to get dimensions and default render
                currentSvg = loadSvgFromBytes(baseSvgBytes);
                if (currentSvg != null) {
                    FloatSize size = currentSvg.size();
                    svgW = size.width;
                    svgH = size.height;
                } else {
                    svgW = 593;
                    svgH = 445;
                }
            } catch (Exception e) {
                System.err.println("Could not load SVG: " + e.getMessage());
                e.printStackTrace();
                svgW = 593;
                svgH = 445;
            }
        }

        /**
         * Highlight the given SVG groups (by ID) in the architecture diagram.
         * Empty list = show the base diagram with no highlights.
         */
        public void setHighlightedGroups(List<String> groupIds) {
            try {
                if (groupIds == null || groupIds.isEmpty()) {
                    // Fast path: use cached base bytes
                    currentSvg = loadSvgFromBytes(baseSvgBytes);
                } else {
                    // Clone the DOM and apply highlights
                    Document clone = (Document) baseSvgDom.cloneNode(true);
                    Set<String> ids = new HashSet<>(groupIds);

                    for (String id : ids) {
                        Element el = clone.getElementById(id);
                        if (el != null) {
                            applyHighlight(el);
                        }
                    }

                    byte[] modifiedBytes = serializeDom(clone);
                    currentSvg = loadSvgFromBytes(modifiedBytes);
                }
            } catch (Exception e) {
                System.err.println("SVG highlight error: " + e.getMessage());
            }
            repaint();
        }

        /**
         * Apply highlight styling to an SVG element and all its children.
         * Shape elements (path, line, rect, etc.) get their stroke turned red.
         * Text elements get their fill turned red.
         */
        private void applyHighlight(Element group) {
            highlightElement(group);
            NodeList children = group.getElementsByTagName("*");
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    highlightElement((Element) children.item(i));
                }
            }
        }

        private void highlightElement(Element el) {
            String tag = el.getLocalName();
            if (tag == null) tag = el.getTagName();
            if ("g".equals(tag) || "defs".equals(tag) || "svg".equals(tag)) {
                return;
            }

            String style = el.getAttribute("style");
            boolean hasStroke = false;
            boolean hasFill = false;

            // Change stroke attribute to red if present and not "none"
            String stroke = el.getAttribute("stroke");
            if (!stroke.isEmpty() && !"none".equals(stroke)) {
                el.setAttribute("stroke", HIGHLIGHT_COLOR);
                hasStroke = true;
            }

            // Change fill attribute to red if present and not "none"
            String fill = el.getAttribute("fill");
            if (!fill.isEmpty() && !"none".equals(fill)) {
                el.setAttribute("fill", HIGHLIGHT_COLOR);
                hasFill = true;
            }

            // Check inline style for stroke/fill
            if (!style.isEmpty()) {
                if (style.contains("stroke:") && !style.contains("stroke:none")) {
                    hasStroke = true;
                }
                if (style.contains("fill:") && !style.contains("fill:none")) {
                    hasFill = true;
                }
                style = style.replaceAll("stroke\\s*:\\s*(?!none)[^;]+", "stroke:" + HIGHLIGHT_COLOR);
                style = style.replaceAll("fill\\s*:\\s*(?!none)[^;]+", "fill:" + HIGHLIGHT_COLOR);
                el.setAttribute("style", style);
            }

            // If no explicit stroke or fill was found, the element uses
            // SVG default fill (black) — e.g. text converted to paths.
            // Set fill to highlight colour.
            if (!hasStroke && !hasFill) {
                el.setAttribute("fill", HIGHLIGHT_COLOR);
            }
        }

        private byte[] serializeDom(Document doc) throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));
            return baos.toByteArray();
        }

        /** Load an SVGDocument from serialized XML bytes */
        private SVGDocument loadSvgFromBytes(byte[] bytes) {
            SVGLoader loader = new SVGLoader();
            return loader.load(
                    new ByteArrayInputStream(bytes),
                    URI.create("z64sim://arch"),
                    LoaderContext.builder().build());
        }

        /**
         * Walk the DOM tree and call setIdAttribute("id", true) on every
         * element that carries an id="..." attribute. This makes
         * Document.getElementById() work with non-validating parsers.
         */
        private static void registerIdAttributes(Element root) {
            if (root.hasAttribute("id")) {
                root.setIdAttribute("id", true);
            }
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    registerIdAttributes((Element) children.item(i));
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (currentSvg == null) return;

            int pw = getWidth();
            int ph = getHeight();
            double scale = Math.min((double) pw / svgW, (double) ph / svgH);
            int dw = (int) (svgW * scale);
            int dh = (int) (svgH * scale);
            int ox = (pw - dw) / 2;
            int oy = (ph - dh) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.translate(ox, oy);
            g2.scale(scale, scale);

            currentSvg.render(this, g2);
            g2.dispose();
        }
    }
}
