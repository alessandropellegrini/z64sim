/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.AppActions;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.UpdateController;
import it.uniroma2.pellegrini.z64sim.model.Devices;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.MemoryElement;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.view.components.JFileDialog;
import it.uniroma2.pellegrini.z64sim.view.components.RegisterBank;
import it.uniroma2.pellegrini.z64sim.view.editor.AsmStyledDocument;
import it.uniroma2.pellegrini.z64sim.view.editor.AsmSyntaxHighlighter;
import it.uniroma2.pellegrini.z64sim.view.editor.FindReplaceBar;
import it.uniroma2.pellegrini.z64sim.view.editor.LineNumberPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainWindow extends View {
    private static final Logger log = LoggerFactory.getLogger();
    private static MainWindow instance = null;
    private final JFrame mainFrame;
    private JPanel mainPanel;
    private JButton assembleButton;
    private JTable memoryView;
    private JTextArea compilerOutput;
    private JTextPane editor;
    private JButton openButton;
    private JButton saveButton;
    private JPanel editorTab;
    private JTabbedPane tabbedPane;
    private JButton newButton;
    private JButton stepButton;
    private RegisterBank cpuView;
    private JButton runButton;
    private JButton stopButton;
    private JLabel editorPositionLabel;
    private JSlider speedSlider;
    private JLabel speedLabel;
    private JTable ivtTable;
    private JButton deviceButton;
    private JButton muOpsButton;

    private File openFile = null;
    private boolean isDirty = false;
    private boolean loading = false;
    private AsmSyntaxHighlighter highlighter;
    private FindReplaceBar findReplaceBar;
    private AsmStyledDocument asmDocument;
    private final UndoManager undoManager = new UndoManager();
    private MuOpAnimationDialog muOpDialog;

    private MainWindow() {
        $$$setupUI$$$();

        // Attach line number gutter to the editor's scroll pane
        JScrollPane editorScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, editor);
        if (editorScrollPane != null) {
            LineNumberPanel lineNumbers = new LineNumberPanel(editor);
            if (SettingsController.getShowLineNumbers()) {
                editorScrollPane.setRowHeaderView(lineNumbers);
            }
            // Listen for runtime toggling of line numbers
            SettingsController.addPropertyChangeListener("showLineNumbers", evt -> {
                if (Boolean.TRUE.equals(evt.getNewValue())) {
                    editorScrollPane.setRowHeaderView(lineNumbers);
                } else {
                    editorScrollPane.setRowHeaderView(null);
                }
                editorScrollPane.revalidate();
                editorScrollPane.repaint();
            });
        }

        // Dynamically insert the FindReplaceBar into editorTab.
        // The form generates editorTab with [scrollPane, statusPanel] in a GridLayout.
        // We re-layout it with BorderLayout: scroll pane CENTER, find bar + status SOUTH.
        {
            Component scrollPane = editorTab.getComponent(0); // editor scroll pane
            Component statusPanel = editorTab.getComponent(1); // position label panel
            editorTab.removeAll();
            editorTab.setLayout(new BorderLayout());
            editorTab.add(scrollPane, BorderLayout.CENTER);
            findReplaceBar = new FindReplaceBar(editor);
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            bottomPanel.add(findReplaceBar);
            bottomPanel.add(statusPanel);
            editorTab.add(bottomPanel, BorderLayout.SOUTH);
        }

        this.memoryView.setModel(Memory.getInstance());
        Memory.getInstance().setView(this.memoryView);
        this.memoryView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = memoryView.rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    long address = row * 8L;
                    MemoryElement elem = Memory.getMemoryElementAt(address);
                    if (elem instanceof Instruction) {
                        InstructionInspector dialog = new InstructionInspector(
                                mainFrame, (Instruction) elem, memoryView);
                        dialog.setVisible(true);
                    }
                }
            }
        });
        this.ivtTable.setModel(Devices.getInstance());
        this.ivtTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        this.ivtTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        this.ivtTable.getColumnModel().getColumn(0).setMaxWidth(40);
        this.compilerOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        this.mainFrame = new JFrame(PropertyBroker.getPropertyValue("z64sim.name"));
        this.mainFrame.setContentPane(this.mainPanel);
        this.mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.mainFrame.setJMenuBar(new MainWindowMenu());
        this.mainFrame.setMinimumSize(new Dimension(Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.minSizeX")), Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.minSizeY"))));
        this.mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component) evt.getSource();
                SettingsController.setWindowSize(new Dimension(c.getWidth(), c.getHeight()));
            }
        });
        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                MainWindow.quit();
            }
        });

        this.setApplicationIcon();
        this.mainFrame.pack();
        // Apply saved window size after pack(), so it is not overridden
        this.mainFrame.setSize(SettingsController.getWindowSize());
        newButton.addActionListener(actionEvent -> this.newFile());
        openButton.addActionListener(actionEvent -> this.openFile());
        saveButton.addActionListener(actionEvent -> this.saveFile());
        assembleButton.addActionListener(AppActions.ASSEMBLE);

        Toolkit tk = Toolkit.getDefaultToolkit();
        final int modKeyMask = tk.getMenuShortcutKeyMask();

        // Attach undo manager to the document
        editor.getDocument().addUndoableEditListener(undoManager);

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!loading) MainWindow.setDirty();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!loading) MainWindow.setDirty();
            }

            @Override
            public void changedUpdate(DocumentEvent e) { /* attribute changes, not content */ }
        });
        editor.addCaretListener(e -> {
            // update the line number view
            Element root = editor.getDocument().getDefaultRootElement();
            int line = root.getElementIndex(e.getDot());
            int col = e.getDot() - root.getElement(line).getStartOffset();
            // both are starting to 1
            editorPositionLabel.setText((line + 1) + ":" + (col + 1));
        });
        SimulatorController.setCpuView(this.cpuView);
        stepButton.addActionListener(AppActions.STEP);
        runButton.addActionListener(AppActions.RUN);
        stopButton.addActionListener(AppActions.STOP);
        deviceButton.addActionListener(e -> {
            DeviceManager dialog = new DeviceManager();
            dialog.setTitle("Device Manager");
            dialog.pack();
            dialog.setLocationRelativeTo(mainFrame);
            dialog.setVisible(true);
        });
        muOpsButton.addActionListener(e -> {
            if (muOpDialog == null || !muOpDialog.isDisplayable()) {
                muOpDialog = new MuOpAnimationDialog(mainFrame);
            }
            muOpDialog.setVisible(true);
            muOpDialog.toFront();
        });

        // Keyboard shortcuts via InputMap/ActionMap
        InputMap im = mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = mainPanel.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, modKeyMask), "save");
        am.put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.saveFile();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, modKeyMask), "new");
        am.put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.newFile();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, modKeyMask), "open");
        am.put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainWindow.this.openFile();
            }
        });

        im.put((KeyStroke) AppActions.ASSEMBLE.getValue(Action.ACCELERATOR_KEY), "assemble");
        am.put("assemble", AppActions.ASSEMBLE);

        im.put((KeyStroke) AppActions.STEP.getValue(Action.ACCELERATOR_KEY), "step");
        am.put("step", AppActions.STEP);

        im.put((KeyStroke) AppActions.RUN.getValue(Action.ACCELERATOR_KEY), "run");
        am.put("run", AppActions.RUN);

        im.put((KeyStroke) AppActions.STOP.getValue(Action.ACCELERATOR_KEY), "stop");
        am.put("stop", AppActions.STOP);

        im.put((KeyStroke) AppActions.UNDO.getValue(Action.ACCELERATOR_KEY), "undo");
        am.put("undo", AppActions.UNDO);

        im.put((KeyStroke) AppActions.REDO.getValue(Action.ACCELERATOR_KEY), "redo");
        am.put("redo", AppActions.REDO);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, modKeyMask), "find");
        am.put("find", AppActions.FIND);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, modKeyMask), "findReplace");
        am.put("findReplace", AppActions.FIND_REPLACE);

        // Listen for theme changes from SettingsController
        SettingsController.addPropertyChangeListener("theme", evt -> {
            if ("light".equals(evt.getNewValue())) {
                this.setTheme(new FlatLightLaf());
            } else {
                this.setTheme(new FlatDarkLaf());
            }
            // Update syntax highlighting colors for the new theme
            highlighter.setDarkTheme(!"light".equals(evt.getNewValue()));
            asmDocument.rehighlight();
        });

        // Listen for update check completion
        UpdateController.addPropertyChangeListener("updateCheckCompleted", evt -> {
            if (UpdateController.isUpdateAvailable()) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this.mainFrame,
                        PropertyBroker.getMessageFromBundle("update.available.0", UpdateController.getUpstreamVersion()),
                        PropertyBroker.getMessageFromBundle("update.available"),
                        JOptionPane.INFORMATION_MESSAGE)
                );
            }
        });
    }

    private void newFile() {
        if (!this.changesToDiscard())
            return;
        this.loading = true;
        this.asmDocument.setHighlightingEnabled(false);
        this.editor.setText("");
        this.asmDocument.setHighlightingEnabled(true);
        this.loading = false;
        this.isDirty = false;
        this.undoManager.discardAllEdits();
        this.openFile = null;
        this.tabbedPane.setTitleAt(0, PropertyBroker.getMessageFromBundle("file.tab.untitled"));
    }

    private void saveFile() {
        if (this.openFile == null) {
            String filePath = new JFileDialog(
                ".asm",
                PropertyBroker.getMessageFromBundle("file.assembly"),
                JFileDialog.MODE_SAVE,
                SettingsController.getFileLastDir()
            ).getFilePath();
            if (filePath == null) {
                return;
            }
            this.openFile = new File(filePath);
        }
        final File fileToSave = this.openFile;
        final String content = this.editor.getText();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Files.write(fileToSave.toPath(), content.getBytes(StandardCharsets.UTF_8));
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    MainWindow.this.isDirty = false;
                    MainWindow.this.tabbedPane.setTitleAt(0, fileToSave.getName());
                    SettingsController.setFileLastDir(fileToSave.getParent());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, PropertyBroker.getMessageFromBundle("file.error.while.saving.0", e.getMessage()), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void openFile() {
        if (!this.changesToDiscard())
            return;
        String filePath = new JFileDialog(
                ".asm",
                PropertyBroker.getMessageFromBundle("file.assembly"),
                JFileDialog.MODE_OPEN,
                SettingsController.getFileLastDir()
        ).getFilePath();
        if (filePath == null) {
            return;
        }
        this.doOpenFile(filePath);
    }

    private void doOpenFile(String filePath) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            }

            @Override
            protected void done() {
                try {
                    String content = get();
                    MainWindow.this.loading = true;
                    MainWindow.this.asmDocument.setHighlightingEnabled(false);
                    MainWindow.this.editor.setText(content);
                    MainWindow.this.asmDocument.setHighlightingEnabled(true);
                    MainWindow.this.loading = false;
                    MainWindow.this.isDirty = false;
                    MainWindow.this.undoManager.discardAllEdits();
                    MainWindow.this.openFile = new File(filePath);
                    MainWindow.this.tabbedPane.setTitleAt(0, MainWindow.this.openFile.getName());
                    SettingsController.setFileLastDir(MainWindow.this.openFile.getParent());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainFrame, PropertyBroker.getMessageFromBundle("file.error.while.opening.0", e.getMessage()), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Return false if the user canceled the action
    private boolean changesToDiscard() {
        if (this.isDirty) {
            int result = JOptionPane.showConfirmDialog(this.mainFrame,
                    PropertyBroker.getMessageFromBundle("file.modified.want.to.save"),
                    PropertyBroker.getMessageFromBundle("file.save.question"),
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                this.saveFile();
                return true;
            }
            // NO means discard changes and proceed; anything else (CANCEL, CLOSED_OPTION) aborts
            return result == JOptionPane.NO_OPTION;
        }
        return true;
    }

    private static void setDirty() {
        final MainWindow instance = getInstance();
        if (!instance.isDirty) {
            instance.isDirty = true;
            instance.tabbedPane.setTitleAt(0, instance.tabbedPane.getTitleAt(0) + "*");
        }
    }

    private static MainWindow getInstance() {
        if (instance == null)
            instance = new MainWindow();
        return instance;
    }

    public static void showMainWindow(String fileToOpen) {
        MainWindow instance = getInstance();
        if (fileToOpen != null) {
            instance.doOpenFile(fileToOpen);
        }
        instance.show();
    }

    public static String getCode() {
        return getInstance().editor.getText();
    }

    public static void undo() {
        UndoManager um = getInstance().undoManager;
        if (um.canUndo()) {
            try {
                um.undo();
            } catch (CannotUndoException ignored) {
            }
        }
    }

    public static void redo() {
        UndoManager um = getInstance().undoManager;
        if (um.canRedo()) {
            try {
                um.redo();
            } catch (CannotRedoException ignored) {
            }
        }
    }

    public static void find() {
        getInstance().findReplaceBar.open(false);
    }

    public static void findReplace() {
        getInstance().findReplaceBar.open(true);
    }

    public static void compileResult(String toString) {
        getInstance().compilerOutput.setText(toString);
    }

    public void show() {
        this.mainFrame.setVisible(true);
    }

    private void setApplicationIcon() {
        // Get the icon
        final Image image = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/frame48.gif"))).getImage();

        // Set the minimized icon for the jar (works out of the box on Windows and Linux)
        this.mainFrame.setIconImage(image);

        // Set the icon in the macOS dock. We use reflection to avoid a compile-time dependency on
        // java.awt.Taskbar (introduced in Java 9), so the project can target Java 8.
        try {
            final Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
            final Method getTaskbar = taskbarClass.getMethod("getTaskbar");
            final Object taskbar = getTaskbar.invoke(null);
            final Method setIconImage = taskbarClass.getMethod("setIconImage", Image.class);
            setIconImage.invoke(taskbar, image);
        } catch (final ClassNotFoundException ignored) {
            // java.awt.Taskbar is not available (Java 8) -- try the legacy Apple API
            try {
                final Class<?> appClass = Class.forName("com.apple.eawt.Application");
                final Method getApp = appClass.getMethod("getApplication");
                final Object app = getApp.invoke(null);
                final Method setDockIcon = appClass.getMethod("setDockIconImage", Image.class);
                setDockIcon.invoke(app, image);
            } catch (final ReflectiveOperationException alsoIgnored) {
                // Not on macOS, or the Apple API is not available -- nothing to do
            }
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                // Taskbar feature not supported on this platform -- nothing to do
            } else if (e.getCause() instanceof SecurityException) {
                log.error(PropertyBroker.getMessageFromBundle("exception.security.while.setting.icon"));
            }
        } catch (final ReflectiveOperationException ignored) {
            // NoSuchMethodException, IllegalAccessException -- should not happen, but fail silently
        }
    }

    /**
     * Quit the application, prompting to save unsaved changes.
     */
    public static void quit() {
        MainWindow mw = getInstance();
        if (!mw.changesToDiscard()) {
            return;
        }
        SettingsController.persist();
        System.exit(0);
    }

    private void setTheme(LookAndFeel theme) {
        try {
            UIManager.setLookAndFeel(theme);
            SwingUtilities.updateComponentTreeUI(this.mainFrame);
            this.mainFrame.pack();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
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
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        Font mainPanelFont = UIManager.getFont("Panel.font");
        if (mainPanelFont != null) mainPanel.setFont(mainPanelFont);
        final JToolBar toolBar1 = new JToolBar();
        Font toolBar1Font = UIManager.getFont("ToolBar.font");
        if (toolBar1Font != null) toolBar1.setFont(toolBar1Font);
        mainPanel.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        newButton = new JButton();
        Font newButtonFont = UIManager.getFont("Button.font");
        if (newButtonFont != null) newButton.setFont(newButtonFont);
        newButton.setIcon(new ImageIcon(getClass().getResource("/images/z64doc.png")));
        newButton.setText("");
        newButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.new.file"));
        toolBar1.add(newButton);
        openButton = new JButton();
        Font openButtonFont = UIManager.getFont("Button.font");
        if (openButtonFont != null) openButton.setFont(openButtonFont);
        openButton.setIcon(new ImageIcon(getClass().getResource("/images/open.png")));
        openButton.setText("");
        openButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.open.file"));
        toolBar1.add(openButton);
        saveButton = new JButton();
        Font saveButtonFont = UIManager.getFont("Button.font");
        if (saveButtonFont != null) saveButton.setFont(saveButtonFont);
        saveButton.setIcon(new ImageIcon(getClass().getResource("/images/save.png")));
        saveButton.setText("");
        saveButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.save.file"));
        toolBar1.add(saveButton);
        assembleButton = new JButton();
        Font assembleButtonFont = UIManager.getFont("Button.font");
        if (assembleButtonFont != null) assembleButton.setFont(assembleButtonFont);
        assembleButton.setIcon(new ImageIcon(getClass().getResource("/images/assemble_icon.png")));
        assembleButton.setText("");
        assembleButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.assemble.program"));
        toolBar1.add(assembleButton);
        stepButton = new JButton();
        Font stepButtonFont = UIManager.getFont("Button.font");
        if (stepButtonFont != null) stepButton.setFont(stepButtonFont);
        stepButton.setIcon(new ImageIcon(getClass().getResource("/images/step.png")));
        stepButton.setText("");
        stepButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.step.instruction"));
        toolBar1.add(stepButton);
        runButton = new JButton();
        Font runButtonFont = UIManager.getFont("Button.font");
        if (runButtonFont != null) runButton.setFont(runButtonFont);
        runButton.setIcon(new ImageIcon(getClass().getResource("/images/run.png")));
        runButton.setText("");
        runButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.run.program"));
        toolBar1.add(runButton);
        stopButton = new JButton();
        Font stopButtonFont = UIManager.getFont("Button.font");
        if (stopButtonFont != null) stopButton.setFont(stopButtonFont);
        stopButton.setIcon(new ImageIcon(getClass().getResource("/images/stop.png")));
        stopButton.setText("");
        stopButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "gui.stop.program"));
        toolBar1.add(stopButton);
        deviceButton = new JButton();
        Font deviceButtonFont = UIManager.getFont("Button.font");
        if (deviceButtonFont != null) deviceButton.setFont(deviceButtonFont);
        deviceButton.setIcon(new ImageIcon(getClass().getResource("/images/iodevice.png")));
        deviceButton.setText("");
        deviceButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "button.devices"));
        toolBar1.add(deviceButton);
        muOpsButton = new JButton();
        Font muOpsButtonFont = UIManager.getFont("Button.font");
        if (muOpsButtonFont != null) muOpsButton.setFont(muOpsButtonFont);
        muOpsButton.setIcon(new ImageIcon(getClass().getResource("/images/mu.png")));
        muOpsButton.setText("");
        muOpsButton.setToolTipText(this.$$$getMessageFromBundle$$$("i18n", "inspect.muops"));
        toolBar1.add(muOpsButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        speedLabel = new JLabel();
        this.$$$loadLabelText$$$(speedLabel, this.$$$getMessageFromBundle$$$("i18n", "gui.simulation.speed"));
        toolBar1.add(speedLabel);
        toolBar1.add(speedSlider);
        final Spacer spacer1 = new Spacer();
        toolBar1.add(spacer1);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerSize(5);
        Font splitPane1Font = UIManager.getFont("Panel.font");
        if (splitPane1Font != null) splitPane1.setFont(splitPane1Font);
        splitPane1.setOrientation(0);
        splitPane1.setResizeWeight(0.9);
        mainPanel.add(splitPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setDividerSize(5);
        Font splitPane2Font = UIManager.getFont("Panel.font");
        if (splitPane2Font != null) splitPane2.setFont(splitPane2Font);
        splitPane2.setResizeWeight(1.0);
        splitPane1.setLeftComponent(splitPane2);
        tabbedPane = new JTabbedPane();
        Font tabbedPaneFont = UIManager.getFont("Panel.font");
        if (tabbedPaneFont != null) tabbedPane.setFont(tabbedPaneFont);
        splitPane2.setLeftComponent(tabbedPane);
        editorTab = new JPanel();
        editorTab.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        Font editorTabFont = UIManager.getFont("TabbedPane.smallFont");
        if (editorTabFont != null) editorTab.setFont(editorTabFont);
        tabbedPane.addTab(this.$$$getMessageFromBundle$$$("i18n", "file.tab.untitled"), editorTab);
        final JScrollPane scrollPane1 = new JScrollPane();
        Font scrollPane1Font = UIManager.getFont("Panel.font");
        if (scrollPane1Font != null) scrollPane1.setFont(scrollPane1Font);
        editorTab.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        Font editorFont = UIManager.getFont("EditorPane.font");
        if (editorFont != null) editor.setFont(editorFont);
        scrollPane1.setViewportView(editor);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        editorTab.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editorPositionLabel = new JLabel();
        editorPositionLabel.setText(" ");
        panel1.add(editorPositionLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane2.setRightComponent(scrollPane2);
        memoryView = new JTable();
        memoryView.setFillsViewportHeight(true);
        scrollPane2.setViewportView(memoryView);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setOrientation(0);
        splitPane1.setRightComponent(splitPane3);
        final JScrollPane scrollPane3 = new JScrollPane();
        Font scrollPane3Font = UIManager.getFont("Panel.font");
        if (scrollPane3Font != null) scrollPane3.setFont(scrollPane3Font);
        splitPane3.setRightComponent(scrollPane3);
        compilerOutput = new JTextArea();
        compilerOutput.setEditable(false);
        compilerOutput.setRows(5);
        compilerOutput.setText("");
        scrollPane3.setViewportView(compilerOutput);
        final JSplitPane splitPane4 = new JSplitPane();
        splitPane4.setResizeWeight(1.0);
        splitPane3.setLeftComponent(splitPane4);
        cpuView = new RegisterBank();
        splitPane4.setLeftComponent(cpuView.$$$getRootComponent$$$());
        final JScrollPane scrollPane4 = new JScrollPane();
        scrollPane4.setPreferredSize(new Dimension(200, 0));
        splitPane4.setRightComponent(scrollPane4);
        ivtTable = new JTable();
        ivtTable.setFillsViewportHeight(true);
        Font ivtTableFont = this.$$$getFont$$$("Monospaced", -1, 12, ivtTable.getFont());
        if (ivtTableFont != null) ivtTable.setFont(ivtTableFont);
        scrollPane4.setViewportView(ivtTable);
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
        speedSlider = new JSlider(0, 1000, 1000);
        speedSlider.setPreferredSize(new Dimension(120, speedSlider.getPreferredSize().height));
        speedSlider.setMaximumSize(new Dimension(120, speedSlider.getPreferredSize().height));
        speedSlider.setToolTipText(PropertyBroker.getMessageFromBundle("gui.speed.slider"));
        speedSlider.addChangeListener(e -> SimulatorController.setTimerDelay(1000 - speedSlider.getValue()));

        // Create the syntax-highlighted editor
        highlighter = new AsmSyntaxHighlighter();
        // Detect initial theme
        highlighter.setDarkTheme(!"light".equals(SettingsController.getTheme()));
        asmDocument = new AsmStyledDocument(highlighter);
        editor = new JTextPane(asmDocument) {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                // Prevent word-wrap: allow horizontal scrolling like a code editor
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        editor.setFont(monoFont);
        // JTextPane renders using the document's styles, not the component font.
        // Set the monospaced font on the document's default style so all text uses it.
        Style defaultStyle = asmDocument.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defaultStyle, Font.MONOSPACED);
        StyleConstants.setFontSize(defaultStyle, 14);
    }
}
