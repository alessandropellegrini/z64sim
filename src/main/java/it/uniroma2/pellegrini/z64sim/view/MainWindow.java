/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.view;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import it.uniroma2.pellegrini.z64sim.PropertyBroker;
import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.util.log.Logger;
import it.uniroma2.pellegrini.z64sim.util.log.LoggerFactory;
import it.uniroma2.pellegrini.z64sim.util.queue.Dispatcher;
import it.uniroma2.pellegrini.z64sim.util.queue.Events;
import it.uniroma2.pellegrini.z64sim.view.components.JFileDialog;
import it.uniroma2.pellegrini.z64sim.view.components.MulticycleCpu;

import javax.swing.*;
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

    private File openFile = null;
    private boolean isDirty = false;

    private MainWindow() {
        $$$setupUI$$$();

        this.memoryView.setModel(Memory.getInstance());
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

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                MainWindow.setDirty();
            }
        });
    }

    private void newFile() {
        if(!this.changesToDiscard())
            return;
        this.editor.setText("");
        this.openFile = null;
        this.tabbedPane.setTitleAt(0, PropertyBroker.getMessageFromBundle("file.tab.untitled"));
    }

    private void saveFile() {
        if(this.openFile == null) {
            String filePath = (new JFileDialog(".asm", PropertyBroker.getMessageFromBundle("file.assembly"), JFileDialog.MODE_SAVE)).getFilePath();
            this.openFile = new File(filePath);
        }
        try {
            Files.writeString(this.openFile.toPath(), this.editor.getText());
            this.isDirty = false;
            this.tabbedPane.setTitleAt(0, this.openFile.getName());
        } catch(IOException e) {
            JOptionPane.showMessageDialog(this.mainFrame, PropertyBroker.getMessageFromBundle("file.error.while.saving.0", e.getMessage()), PropertyBroker.getMessageFromBundle("dialog.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFile() {
        if(!this.changesToDiscard())
            return;
        String filePath = (new JFileDialog(".asm", PropertyBroker.getMessageFromBundle("file.assembly"), JFileDialog.MODE_OPEN)).getFilePath();
        try {
            this.doOpenFile(filePath);
        } catch(IOException e) {
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
        if(this.isDirty) {
            int result = JOptionPane.showConfirmDialog(this.mainFrame, PropertyBroker.getMessageFromBundle("file.modified.want.to.save"), PropertyBroker.getMessageFromBundle("file.save.question"), JOptionPane.YES_NO_CANCEL_OPTION);
            if(result == JOptionPane.YES_OPTION) {
                this.saveFile();
            } else return result != JOptionPane.CANCEL_OPTION;
        }
        return true;
    }

    private static void setDirty() {
        final MainWindow instance = getInstance();
        if(!instance.isDirty) {
            instance.isDirty = true;
            instance.tabbedPane.setTitleAt(0, instance.tabbedPane.getTitleAt(0) + "*");
        }
    }

    private static MainWindow getInstance() {
        if(instance == null)
            instance = new MainWindow();
        return instance;
    }

    public static void showMainWindow(String fileToOpen) {
        MainWindow instance = getInstance();
        if(fileToOpen != null) {
            try {
                instance.doOpenFile(fileToOpen);
            } catch(IOException e) {
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
        } catch(final UnsupportedOperationException ignored) {
        } catch(final SecurityException e) {
            log.error(PropertyBroker.getMessageFromBundle("exception.security.while.setting.icon"));
        }
    }

    @Override
    public boolean dispatch(Events command) {
        switch(command) {
            case SET_THEME_LIGHT:
                this.setTheme(new FlatLightLaf());
                break;
            case SET_THEME_DARK:
                this.setTheme(new FlatDarkLaf());
                break;
        }
        return true;
    }

    private void setTheme(LookAndFeel theme) {
        try {
            UIManager.setLookAndFeel(theme);
            SwingUtilities.updateComponentTreeUI(this.mainFrame);
            this.mainFrame.pack();
        } catch(UnsupportedLookAndFeelException e) {
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
        if(mainPanelFont != null) mainPanel.setFont(mainPanelFont);
        final JToolBar toolBar1 = new JToolBar();
        Font toolBar1Font = UIManager.getFont("ToolBar.font");
        if(toolBar1Font != null) toolBar1.setFont(toolBar1Font);
        mainPanel.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        newButton = new JButton();
        Font newButtonFont = UIManager.getFont("Button.font");
        if(newButtonFont != null) newButton.setFont(newButtonFont);
        newButton.setIcon(new ImageIcon(getClass().getResource("/images/z64doc.png")));
        newButton.setText("");
        newButton.setToolTipText("New file");
        toolBar1.add(newButton);
        openButton = new JButton();
        Font openButtonFont = UIManager.getFont("Button.font");
        if(openButtonFont != null) openButton.setFont(openButtonFont);
        openButton.setIcon(new ImageIcon(getClass().getResource("/images/open.png")));
        openButton.setText("");
        openButton.setToolTipText("Open file");
        toolBar1.add(openButton);
        saveButton = new JButton();
        Font saveButtonFont = UIManager.getFont("Button.font");
        if(saveButtonFont != null) saveButton.setFont(saveButtonFont);
        saveButton.setIcon(new ImageIcon(getClass().getResource("/images/save.png")));
        saveButton.setText("");
        saveButton.setToolTipText("Save file");
        toolBar1.add(saveButton);
        assembleButton = new JButton();
        Font assembleButtonFont = UIManager.getFont("Button.font");
        if(assembleButtonFont != null) assembleButton.setFont(assembleButtonFont);
        assembleButton.setIcon(new ImageIcon(getClass().getResource("/images/assemble_icon.png")));
        assembleButton.setText("");
        assembleButton.setToolTipText("Assemble program");
        toolBar1.add(assembleButton);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerSize(5);
        Font splitPane1Font = UIManager.getFont("Panel.font");
        if(splitPane1Font != null) splitPane1.setFont(splitPane1Font);
        splitPane1.setOrientation(0);
        splitPane1.setResizeWeight(0.9);
        mainPanel.add(splitPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setDividerSize(5);
        Font splitPane2Font = UIManager.getFont("Panel.font");
        if(splitPane2Font != null) splitPane2.setFont(splitPane2Font);
        splitPane2.setResizeWeight(1.0);
        splitPane1.setLeftComponent(splitPane2);
        tabbedPane = new JTabbedPane();
        Font tabbedPaneFont = UIManager.getFont("Panel.font");
        if(tabbedPaneFont != null) tabbedPane.setFont(tabbedPaneFont);
        splitPane2.setLeftComponent(tabbedPane);
        editorTab = new JPanel();
        editorTab.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        Font editorTabFont = UIManager.getFont("TabbedPane.smallFont");
        if(editorTabFont != null) editorTab.setFont(editorTabFont);
        tabbedPane.addTab(this.$$$getMessageFromBundle$$$("i18n", "file.tab.untitled"), editorTab);
        final JScrollPane scrollPane1 = new JScrollPane();
        Font scrollPane1Font = UIManager.getFont("Panel.font");
        if(scrollPane1Font != null) scrollPane1.setFont(scrollPane1Font);
        editorTab.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        editor = new JEditorPane();
        Font editorFont = UIManager.getFont("EditorPane.font");
        if(editorFont != null) editor.setFont(editorFont);
        scrollPane1.setViewportView(editor);
        final JScrollPane scrollPane2 = new JScrollPane();
        splitPane2.setRightComponent(scrollPane2);
        memoryView = new JTable();
        memoryView.setFillsViewportHeight(true);
        scrollPane2.setViewportView(memoryView);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setOrientation(0);
        splitPane1.setRightComponent(splitPane3);
        final MulticycleCpu nestedForm1 = new MulticycleCpu();
        splitPane3.setLeftComponent(nestedForm1.$$$getRootComponent$$$());
        final JScrollPane scrollPane3 = new JScrollPane();
        Font scrollPane3Font = UIManager.getFont("Panel.font");
        if(scrollPane3Font != null) scrollPane3.setFont(scrollPane3Font);
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
            if($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch(Exception e) {
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
