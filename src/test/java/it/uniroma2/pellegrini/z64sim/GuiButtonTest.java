/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.view.MainWindow;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIf;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
                assertTrue(compilerOutput.getText().toLowerCase().contains("success"),
                        "Compiler output should contain 'success' or 'successful'. Actual: " + compilerOutput.getText());

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
                assertTrue(compilerOutput.getText().toLowerCase().contains("success"),
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
}
