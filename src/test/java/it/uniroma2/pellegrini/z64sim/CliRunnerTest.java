/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import it.uniroma2.pellegrini.z64sim.cli.CliRunner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CliRunner headless CLI integration tests")
public class CliRunnerTest {

    private File getResourceFile(String resourceName) throws Exception {
        URI uri = Objects.requireNonNull(getClass().getResource(resourceName)).toURI();
        return new File(uri);
    }

    @Test
    @DisplayName("CLI --help returns 0")
    public void testHelp() {
        int code = CliRunner.run(new String[]{"CLI", "--help"});
        assertEquals(0, code);
    }

    @Test
    @DisplayName("CLI assemble on valid file returns 0")
    public void testAssembleValid() throws Exception {
        File asmFile = getResourceFile("/test.asm");
        int code = CliRunner.run(new String[]{"CLI", "assemble", "-asm", asmFile.getAbsolutePath()});
        assertEquals(0, code);
    }

    @Test
    @DisplayName("CLI assemble on invalid file returns 1")
    public void testAssembleInvalid() throws Exception {
        File badAsm = File.createTempFile("z64_bad_", ".asm");
        badAsm.deleteOnExit();
        try (FileWriter fw = new FileWriter(badAsm, StandardCharsets.UTF_8)) {
            fw.write(".org 0x800\n.text\nmain:\n  invalid_instruction_here\n");
        }
        int code = CliRunner.run(new String[]{"CLI", "assemble", "-asm", badAsm.getAbsolutePath()});
        assertEquals(1, code);
    }

    @Test
    @DisplayName("CLI run keyboard_terminal echoes characters to terminal in headless mode")
    public void testKeyboardTerminalHeadless() throws Exception {
        File asmFile = getResourceFile("/keyboard_terminal.asm");

        // Create temporary config JSON for keyboard + terminal
        File tempConfig = File.createTempFile("z64_test_cfg_", ".json");
        tempConfig.deleteOnExit();

        File tempOut = File.createTempFile("z64_test_out_", ".json");
        tempOut.deleteOnExit();

        String configJson = "{\n"
                + "  \"sync\": true,\n"
                + "  \"max_steps\": 50000,\n"
                + "  \"stop_terminal\": \"Z64\",\n"
                + "  \"devices\": [\n"
                + "    {\n"
                + "      \"type\": \"Keyboard\",\n"
                + "      \"ivn\": 1,\n"
                + "      \"input\": \"Z64\",\n"
                + "      \"ports\": {\"INT_REQ\": \"0x20\", \"DATA\": \"0x21\"}\n"
                + "    },\n"
                + "    {\n"
                + "      \"type\": \"Terminal\",\n"
                + "      \"ivn\": -1,\n"
                + "      \"ports\": {\"STATUS\": \"0x30\", \"DATA\": \"0x31\"}\n"
                + "    }\n"
                + "  ]\n"
                + "}\n";

        try (FileWriter fw = new FileWriter(tempConfig, StandardCharsets.UTF_8)) {
            fw.write(configJson);
        }

        int code = CliRunner.run(new String[]{
                "CLI", "run",
                "-asm", asmFile.getAbsolutePath(),
                "-config", tempConfig.getAbsolutePath(),
                "-out", tempOut.getAbsolutePath()
        });

        assertEquals(0, code, "Simulation should complete successfully with exit code 0");

        // Parse and verify output JSON
        JSONParser parser = new JSONParser();
        JSONObject report;
        try (FileReader fr = new FileReader(tempOut, StandardCharsets.UTF_8)) {
            report = (JSONObject) parser.parse(fr);
        }

        assertNotNull(report);
        assertTrue(report.containsKey("steps"));
        long steps = ((Number) report.get("steps")).longValue();
        assertTrue(steps > 0);

        assertTrue(report.containsKey("devices"));
        JSONObject devicesObj = (JSONObject) report.get("devices");
        assertTrue(devicesObj.containsKey("Terminal"));
        JSONObject termObj = (JSONObject) devicesObj.get("Terminal");
        String termText = (String) termObj.get("text");
        assertNotNull(termText);
        assertTrue(termText.contains("Z64"), "Terminal output should contain typed text 'Z64', got: " + termText);
    }
}
