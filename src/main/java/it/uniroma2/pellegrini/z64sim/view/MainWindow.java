/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.UpdateController;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Dispatcher;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.components.JFileDialog;
import it.uniroma2.pellegrini.z64sim.view.components.RegisterBank;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainWindow extends View {
    private static final Logger log = LoggerFactory.getLogger();
    private static MainWindow instance = null;
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JButton assembleButton;
    private JTable memoryView;
    private JTextArea compilerOutput;
    private JEditorPane editor;
    private JButton openButton;
    private JButton saveButton;
    private JPanel editorTab;
    private JTabbedPane tabbedPane;
    private JButton newButton;
    private JButton stepButton;
    private RegisterBank cpuView;
    private JButton runButton;
    private JLabel editorPositionLabel;

    private File openFile = null;
    private boolean isDirty = false;

    private MainWindow() {
        $$$setupUI$$$();

        this.memoryView.setModel(Memory.getInstance());
        Memory.getInstance().setView(this.memoryView);
        this.compilerOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        this.mainFrame = new JFrame(PropertyBroker.getPropertyValue("z64sim.name"));
        this.mainFrame.setContentPane(this.mainPanel);
        this.mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.mainFrame.setJMenuBar(new MainWindowMenu());
        this.mainFrame.setMinimumSize(new Dimension(Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.minSizeX")), Integer.parseInt(PropertyBroker.getPropertyValue("z64sim.ui.minSizeY"))));
        this.mainFrame.setSize(SettingsController.getWindowSize());
        this.mainFrame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component) evt.getSource();
                SettingsController.setWindowSize(new Dimension(c.getWidth(), c.getHeight()));
            }
        });
        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                Dispatcher.dispatch(Events.QUIT);
            }
        });

        this.setApplicationIcon();
        this.mainFrame.pack();
        newButton.addActionListener(actionEvent -> this.newFile());
        openButton.addActionListener(actionEvent -> this.openFile());
        saveButton.addActionListener(actionEvent -> this.saveFile());
        assembleButton.addActionListener(actionEvent -> Dispatcher.dispatch(Events.ASSEMBLE_PROGRAM));

        Toolkit tk = Toolkit.getDefaultToolkit();
        final int modKeyMask = tk.getMenuShortcutKeyMaskEx();

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                if (e.getModifiersEx() != modKeyMask) {
                    MainWindow.setDirty();
                }
            }
        });
        editor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                // update the line number view
                Element root = editor.getDocument().getDefaultRootElement();
                int line = root.getElementIndex(e.getDot());
                int col = e.getDot() - root.getElement(line).getStartOffset();
                // both are starting to 1
                editorPositionLabel.setText((line + 1) + ":" + (col + 1));
            }
        });
        SimulatorController.setCpuView(this.cpuView);
        stepButton.addActionListener(actionEvent -> {
            SimulatorController.step();
        });
        runButton.addActionListener(actionEvent -> {
            SimulatorController.run();
        });

        mainPanel.registerKeyboardAction(
                e -> this.saveFile(),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, modKeyMask),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mainPanel.registerKeyboardAction(
                e -> this.newFile(),
                KeyStroke.getKeyStroke(KeyEvent.VK_N, modKeyMask),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mainPanel.registerKeyboardAction(
                e -> this.openFile(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, modKeyMask),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mainPanel.registerKeyboardAction(
                e -> {
                    SimulatorController.step();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mainPanel.registerKeyboardAction(
                e -> {
                    SimulatorController.run();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mainPanel.registerKeyboardAction(
                e -> Dispatcher.dispatch(Events.ASSEMBLE_PROGRAM),
                KeyStroke.getKeyStroke(KeyEvent.VK_B, modKeyMask),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void newFile() {
        if (!this.changesToDiscard())
            return;
        this.editor.setText("");
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
        try {
            Files.writeString(this.openFile.toPath(), this.editor.getText());
            this.isDirty = false;
            this.tabbedPane.setTitleAt(0, this.openFile.getName());
            SettingsController.setFileLastDir(this.openFile.getParent());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.mainFrame, PropertyBroker.getMessageFromBundle("file.error.while.saving.0", e.getMessage()), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        }
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
        try {
            this.doOpenFile(filePath);
            SettingsController.setFileLastDir(this.openFile.getParent());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.mainFrame, PropertyBroker.getMessageFromBundle("file.error.while.opening.0", e.getMessage()), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doOpenFile(String filePath) throws IOException {
        this.editor.setText(Files.readString(Path.of(filePath)));
        this.openFile = new File(filePath);
        this.tabbedPane.setTitleAt(0, this.openFile.getName());
    }

    // Return false if the user canceled the action
    private boolean changesToDiscard() {
        if (this.isDirty) {
            int result = JOptionPane.showConfirmDialog(this.mainFrame, PropertyBroker.getMessageFromBundle("file.modified.want.to.save"), PropertyBroker.getMessageFromBundle("file.save.question"), JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                this.saveFile();
            } else return result != JOptionPane.CANCEL_OPTION;
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
            try {
                instance.doOpenFile(fileToOpen);
            } catch (IOException e) {
                log.error(PropertyBroker.getMessageFromBundle("file.error.while.opening"), e);
            }
        }
        instance.show();
    }

    public static String getCode() {
        return getInstance().editor.getText();
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

        // Now the macOS shit to set the icon in the docker. This is the only place that requires us to
        // run on Java >8, otherwise it'd be ugly and nasty to check if com.apple.eawt.Application is accessible.
        try {
            final Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(image);
        } catch (final UnsupportedOperationException ignored) {
        } catch (final SecurityException e) {
            log.error(PropertyBroker.getMessageFromBundle("exception.security.while.setting.icon"));
        }
    }

    @Override
    public boolean dispatch(Events command) {
        switch (command) {
            case SET_THEME_LIGHT:
                this.setTheme(new FlatLightLaf());
                break;
            case SET_THEME_DARK:
                this.setTheme(new FlatDarkLaf());
                break;
            case UPDATE_CHECK_COMPLETED:
                if (UpdateController.isUpdateAvailable()) {
                    JOptionPane.showMessageDialog(this.mainFrame, PropertyBroker.getMessageFromBundle("update.available.0", UpdateController.getUpstreamVersion()), PropertyBroker.getMessageFromBundle("update.available"), JOptionPane.INFORMATION_MESSAGE);
                }
        }
        return true;
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
        editor = new JEditorPane();
        Font editorFont = UIManager.getFont("EditorPane.font");
        if (editorFont != null) editor.setFont(editorFont);
        scrollPane1.setViewportView(editor);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        editorTab.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        editorPositionLabel = new JLabel();
        editorPositionLabel.setText(" ");
        panel1.add(editorPositionLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane2.setRightComponent(scrollPane2);
        memoryView = new JTable();
        memoryView.setFillsViewportHeight(true);
        scrollPane2.setViewportView(memoryView);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setOrientation(0);
        splitPane1.setRightComponent(splitPane3);
        cpuView = new RegisterBank();
        splitPane3.setLeftComponent(cpuView.$$$getRootComponent$$$());
        final JScrollPane scrollPane3 = new JScrollPane();
        Font scrollPane3Font = UIManager.getFont("Panel.font");
        if (scrollPane3Font != null) scrollPane3.setFont(scrollPane3Font);
        splitPane3.setRightComponent(scrollPane3);
        compilerOutput = new JTextArea();
        compilerOutput.setEditable(false);
        compilerOutput.setRows(5);
        compilerOutput.setText("");
        scrollPane3.setViewportView(compilerOutput);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

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
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
