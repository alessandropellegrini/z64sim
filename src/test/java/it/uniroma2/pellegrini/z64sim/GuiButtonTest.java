/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.instructions.Instruction;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass1;
import it.uniroma2.pellegrini.z64sim.isa.operands.OperandRegister;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.view.InstructionInspector;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import it.uniroma2.pellegrini.z64sim.view.MuOpAnimationDialog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIf;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisabledIf("isHeadless")
public class GuiButtonTest {

    private static MainWindow mainWindowInstance;

    static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    @BeforeAll
    static void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                // Initialize the controller first
                SimulatorController.init();

                // Call showMainWindow to construct the UI and set the singleton instance
                MainWindow.showMainWindow(null);

                // Use reflection to get the instance
                Field instanceField = MainWindow.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                mainWindowInstance = (MainWindow) instanceField.get(null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set up GUI tests", e);
            }
        });
    }

    @AfterAll
    static void tearDown() throws Exception {
        // Stop any running simulation to prevent lingering worker threads
        SimulatorController.stop();
        Thread.sleep(100);

        if (mainWindowInstance != null) {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    Field frameField = MainWindow.class.getDeclaredField("mainFrame");
                    frameField.setAccessible(true);
                    JFrame frame = (JFrame) frameField.get(mainWindowInstance);
                    if (frame != null) {
                        frame.dispose();
                    }
                } catch (Exception e) {
                    // Ignore teardown errors
                }
            });
        }
    }

    private <T> T getPrivateField(String fieldName) throws Exception {
        Field field = MainWindow.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(mainWindowInstance);
    }

    @Test
    void testButtonsWiring() throws Exception {
        List<String> buttonNames = Arrays.asList(
                "newButton", "openButton", "saveButton",
                "assembleButton", "stepButton", "runButton", "stopButton"
        );

        SwingUtilities.invokeAndWait(() -> {
            try {
                for (String btnName : buttonNames) {
                    JButton button = getPrivateField(btnName);
                    assertNotNull(button, "Button " + btnName + " should not be null");

                    String tooltip = button.getToolTipText();
                    assertNotNull(tooltip, "Tooltip for " + btnName + " should not be null");
                    assertFalse(tooltip.trim().isEmpty(), "Tooltip for " + btnName + " should not be empty");

                    assertTrue(button.getActionListeners().length > 0,
                            "Button " + btnName + " should have at least one ActionListener");
                }
            } catch (Exception e) {
                fail("Exception while testing buttons wiring: " + e.getMessage());
            }
        });
    }

    @Test
    void testKeyboardActionsRegistered() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JPanel mainPanel = getPrivateField("mainPanel");
                assertNotNull(mainPanel, "mainPanel should not be null");

                // Check that some keystrokes are registered for WHEN_IN_FOCUSED_WINDOW
                KeyStroke[] keyStrokes = mainPanel.getRegisteredKeyStrokes();
                assertNotNull(keyStrokes, "Registered keystrokes should not be null");
                assertTrue(keyStrokes.length > 0, "There should be registered keystrokes on mainPanel");

                boolean hasAssembleKeystroke = false;
                for (KeyStroke ks : keyStrokes) {
                    if (ks.getKeyCode() == java.awt.event.KeyEvent.VK_B) {
                        hasAssembleKeystroke = true;
                        break;
                    }
                }
                assertTrue(hasAssembleKeystroke, "Assemble keyboard shortcut (VK_B) should be registered");

            } catch (Exception e) {
                fail("Exception while testing keyboard actions: " + e.getMessage());
            }
        });
    }

    @Test
    void testEndToEndAssembleAndStep() throws Exception {
        String program = ".org 0x800\n" +
                ".text\n" +
                "main:\n" +
                "    mov $42, %rax\n" +
                "    hlt\n";

        // Set editor text and click assemble on the EDT
        SwingUtilities.invokeAndWait(() -> {
            try {
                JEditorPane editor = getPrivateField("editor");
                assertNotNull(editor, "Editor should not be null");
                editor.setText(program);

                JButton assembleButton = getPrivateField("assembleButton");
                assembleButton.doClick();
            } catch (Exception e) {
                fail("Exception during assembly setup: " + e.getMessage());
            }
        });

        // Assembly runs on a SwingWorker background thread; wait for it to complete
        Thread.sleep(100);

        // Verify assembly succeeded, then step and check result.
        // step() now runs synchronously on the EDT, so we can verify immediately.
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTextArea compilerOutput = getPrivateField("compilerOutput");
                assertNotNull(compilerOutput, "Compiler output should not be null");
                String expectedMsg = PropertyBroker.getMessageFromBundle("gui.assembly.successful");
                assertTrue(compilerOutput.getText().contains(expectedMsg),
                        "Compiler output should contain assembly success message. Actual: " + compilerOutput.getText());

                JButton stepButton = getPrivateField("stepButton");
                stepButton.doClick();

                // After stepping once, %rax should be 42
                long raxValue = SimulatorController.getCpuState().getRegisterValue(Register.RAX);
                assertEquals(42L, raxValue, "RAX register should have value 42 after step");
            } catch (Exception e) {
                fail("Exception during step: " + e.getMessage());
            }
        });
    }

    @Test
    @Timeout(15)
    void testStopHaltsExecution() throws Exception {
        // Read the infinite loop program from test resources
        String program = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource("infinite_loop.asm").toURI())),
                StandardCharsets.UTF_8);

        // Set editor text and click assemble
        SwingUtilities.invokeAndWait(() -> {
            try {
                JEditorPane editor = getPrivateField("editor");
                editor.setText(program);

                JButton assembleButton = getPrivateField("assembleButton");
                assembleButton.doClick();
            } catch (Exception e) {
                fail("Exception during assembly: " + e.getMessage());
            }
        });

        // Wait for assembly to complete
        Thread.sleep(100);

        // Verify assembly succeeded, then click run
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTextArea compilerOutput = getPrivateField("compilerOutput");
                String expectedMsg = PropertyBroker.getMessageFromBundle("gui.assembly.successful");
                assertTrue(compilerOutput.getText().contains(expectedMsg),
                        "Assembly should succeed. Actual: " + compilerOutput.getText());

                JButton runButton = getPrivateField("runButton");
                runButton.doClick();
            } catch (Exception e) {
                fail("Exception during run: " + e.getMessage());
            }
        });

        // Let the Timer-driven infinite loop run for a bit
        Thread.sleep(100);

        // Click stop via the GUI. This works correctly because the Timer-based
        // execution yields the EDT between ticks, keeping it responsive.
        SwingUtilities.invokeAndWait(() -> {
            try {
                JButton stopButton = getPrivateField("stopButton");
                stopButton.doClick();
            } catch (Exception e) {
                fail("Exception during stop: " + e.getMessage());
            }
        });

        // RAX should be > 1 (the loop ran at least once) and finite (stop worked)
        long raxValue = SimulatorController.getCpuState().getRegisterValue(Register.RAX);
        assertTrue(raxValue > 1, "RAX should be > 1 after the loop ran. Actual: " + raxValue);
    }

    @Test
    void testInstructionInspectorMuOpsButton() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame mainFrame = getPrivateField("mainFrame");
                JTable memoryView = getPrivateField("memoryView");
                Instruction insn = new InstructionClass1(
                        "mov",
                        new OperandRegister(Register.RAX, 8),
                        new OperandRegister(Register.RCX, 8),
                        -1
                );

                InstructionInspector inspector = new InstructionInspector(mainFrame, insn, memoryView);
                inspector.setVisible(true);

                Field btnField = InstructionInspector.class.getDeclaredField("muOpsAnimButton");
                btnField.setAccessible(true);
                JButton muOpsAnimButton = (JButton) btnField.get(inspector);
                assertNotNull(muOpsAnimButton, "muOpsAnimButton should not be null");
                assertTrue(muOpsAnimButton.isShowing(), "muOpsAnimButton should be visible on screen");
                assertTrue(muOpsAnimButton.getWidth() > 0 && muOpsAnimButton.getHeight() > 0,
                        "muOpsAnimButton should have non-zero size");

                // Click the button to launch the µ-ops animation window for this instruction
                muOpsAnimButton.doClick();

                Field dlgField = InstructionInspector.class.getDeclaredField("muOpDialog");
                dlgField.setAccessible(true);
                MuOpAnimationDialog muOpDialog = (MuOpAnimationDialog) dlgField.get(inspector);
                assertNotNull(muOpDialog, "MuOpAnimationDialog should be created on click");
                assertTrue(muOpDialog.isVisible(), "MuOpAnimationDialog should be visible");
                assertTrue(muOpDialog.getTitle().contains(insn.toString()),
                        "Dialog title should contain the specific instruction mnemonic");

                Field opsField = MuOpAnimationDialog.class.getDeclaredField("currentOps");
                opsField.setAccessible(true);
                List<?> ops = (List<?>) opsField.get(muOpDialog);
                assertFalse(ops.isEmpty(), "Micro-ops should be loaded for the inspected instruction");

                inspector.dispose();
            } catch (Exception e) {
                fail("Exception testing InstructionInspector muOpsAnimButton: " + e.getMessage());
            }
        });
    }

    @Test
    void testMuOpAnimationDialogExportPng() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame mainFrame = getPrivateField("mainFrame");
                Instruction insn = new InstructionClass1(
                        "mov",
                        new OperandRegister(Register.RAX, 8),
                        new OperandRegister(Register.RCX, 8),
                        -1
                );

                MuOpAnimationDialog dialog = new MuOpAnimationDialog(mainFrame, insn);
                dialog.setVisible(true);

                JButton exportBtn = dialog.getExportButton();
                assertNotNull(exportBtn, "Export button should not be null");
                assertTrue(exportBtn.isShowing(), "Export button should be visible");
                assertNotNull(exportBtn.getToolTipText(), "Export button should have tooltip");
                assertNotNull(exportBtn.getIcon(), "Export button should have save icon");

                BufferedImage frameImg = dialog.getCurrentFrameImage();
                assertNotNull(frameImg, "Frame image should not be null");
                assertTrue(frameImg.getWidth() > 0 && frameImg.getHeight() > 0,
                        "Frame image should have positive dimensions");

                File tempFile = File.createTempFile("muop_test_", ".png");
                tempFile.deleteOnExit();

                dialog.exportPng(tempFile);
                assertTrue(tempFile.exists() && tempFile.length() > 0,
                        "Exported PNG file should exist and have non-zero size");

                BufferedImage readBack = ImageIO.read(tempFile);
                assertNotNull(readBack, "Exported file should be a readable PNG image");
                assertEquals(frameImg.getWidth(), readBack.getWidth(), "Width should match frame image");
                assertEquals(frameImg.getHeight(), readBack.getHeight(), "Height should match frame image");

                tempFile.delete();
                dialog.dispose();
            } catch (Exception e) {
                fail("Exception testing MuOpAnimationDialog export PNG: " + e.getMessage());
            }
        });
    }

    @Test
    void testMuOpAnimationDialogExportGif() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame mainFrame = getPrivateField("mainFrame");
                Instruction insn = new InstructionClass1(
                        "mov",
                        new OperandRegister(Register.RAX, 8),
                        new OperandRegister(Register.RCX, 8),
                        -1
                );

                MuOpAnimationDialog dialog = new MuOpAnimationDialog(mainFrame, insn);
                dialog.setVisible(true);

                JButton exportGifBtn = dialog.getExportGifButton();
                assertNotNull(exportGifBtn, "Export GIF button should not be null");
                assertTrue(exportGifBtn.isShowing(), "Export GIF button should be visible");
                assertNotNull(exportGifBtn.getToolTipText(), "Export GIF button should have tooltip");
                assertNotNull(exportGifBtn.getIcon(), "Export GIF button should have save icon");

                List<BufferedImage> frames = dialog.getAllAnimationFrames();
                assertNotNull(frames, "Animation frames should not be null");
                assertTrue(frames.size() > 1, "There should be multiple animation frames for an instruction");

                for (BufferedImage frame : frames) {
                    assertNotNull(frame, "Frame should not be null");
                    assertTrue(frame.getWidth() > 0 && frame.getHeight() > 0,
                            "Frame should have positive dimensions");
                }

                File tempGif = File.createTempFile("muop_anim_test_", ".gif");
                tempGif.deleteOnExit();

                dialog.exportGif(tempGif);
                assertTrue(tempGif.exists() && tempGif.length() > 0,
                        "Exported GIF file should exist and have non-zero size");

                try (ImageInputStream iis = ImageIO.createImageInputStream(tempGif)) {
                    java.util.Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                    assertTrue(readers.hasNext(), "Should find a GIF ImageReader for exported file");
                    ImageReader reader = readers.next();
                    reader.setInput(iis);
                    int readFrameCount = reader.getNumImages(true);
                    assertEquals(frames.size(), readFrameCount, "Number of frames in GIF should match animation frames");

                    BufferedImage readFirstFrame = reader.read(0);
                    assertNotNull(readFirstFrame, "First frame should be readable");
                    assertEquals(frames.get(0).getWidth(), readFirstFrame.getWidth(), "First frame width should match");
                    assertEquals(frames.get(0).getHeight(), readFirstFrame.getHeight(), "First frame height should match");
                    reader.dispose();
                }

                tempGif.delete();
                dialog.dispose();

                // Test standalone dialog has export GIF button visible too
                MuOpAnimationDialog standaloneDialog = new MuOpAnimationDialog(mainFrame);
                standaloneDialog.setVisible(true);
                assertNotNull(standaloneDialog.getExportGifButton(), "Standalone dialog should have export GIF button");
                assertTrue(standaloneDialog.getExportGifButton().isShowing(), "Standalone export GIF button should be visible");
                standaloneDialog.dispose();

            } catch (Exception e) {
                fail("Exception testing MuOpAnimationDialog export GIF: " + e.getMessage());
            }
        });
    }
}
