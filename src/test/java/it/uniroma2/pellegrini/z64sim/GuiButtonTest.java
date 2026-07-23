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
import org.junit.jupiter.api.condition.DisabledIf;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
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
                "assembleButton", "stepButton", "runButton"
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

        SwingUtilities.invokeAndWait(() -> {
            try {
                JEditorPane editor = getPrivateField("editor");
                assertNotNull(editor, "Editor should not be null");
                editor.setText(program);

                JButton assembleButton = getPrivateField("assembleButton");
                assembleButton.doClick();

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
                fail("Exception during end-to-end test: " + e.getMessage());
            }
        });
    }
}
