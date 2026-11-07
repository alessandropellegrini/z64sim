/**
 * SPDX-FileCopyrightText: 2015-2026 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.cli;

import it.uniroma2.pellegrini.z64sim.assembler.Assembler;
import it.uniroma2.pellegrini.z64sim.assembler.ParseException;
import it.uniroma2.pellegrini.z64sim.controller.SimulatorController;
import it.uniroma2.pellegrini.z64sim.controller.exceptions.SimulatorException;
import it.uniroma2.pellegrini.z64sim.devices.Keyboard;
import it.uniroma2.pellegrini.z64sim.devices.Terminal;
import it.uniroma2.pellegrini.z64sim.isa.registers.Register;
import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.DeviceMapping;
import it.uniroma2.pellegrini.z64sim.model.Devices;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;
import it.uniroma2.pellegrini.z64sim.model.Memory;
import it.uniroma2.pellegrini.z64sim.model.Program;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Headless CLI runner for the z64 CPU simulator.
 * <p>
 * Supports assembling source files, running simulations in batch/headless
 * mode with device configurations, injecting inputs, and producing
 * machine-readable JSON execution reports.
 * <p>
 * Exit codes:
 * <ul>
 *   <li>0: Success</li>
 *   <li>1: Assembly syntax error</li>
 *   <li>2: Runtime simulator exception</li>
 *   <li>3: Step limit exceeded (watchdog / timeout)</li>
 *   <li>4: CLI usage or invocation error</li>
 * </ul>
 */
public class CliRunner {

    private static final String DEFAULT_DEVICE_PKG = "it.uniroma2.pellegrini.z64sim.devices.";

    public static void main(String[] args) {
        int exitCode = run(args);
        System.exit(exitCode);
    }

    public static int run(String[] args) {
        if (args == null || args.length == 0) {
            printUsage();
            return 4;
        }

        // Shift off initial "CLI", "--cli", or "--headless" if present
        int startIdx = 0;
        if ("CLI".equalsIgnoreCase(args[0]) || "--cli".equalsIgnoreCase(args[0]) || "--headless".equalsIgnoreCase(args[0])) {
            startIdx = 1;
        }

        if (startIdx >= args.length || "-h".equals(args[startIdx]) || "--help".equals(args[startIdx])) {
            printUsage();
            return 0;
        }

        String command = args[startIdx];
        String[] cmdArgs = new String[args.length - startIdx - 1];
        System.arraycopy(args, startIdx + 1, cmdArgs, 0, cmdArgs.length);

        if ("assemble".equalsIgnoreCase(command)) {
            return handleAssemble(cmdArgs);
        } else if ("run".equalsIgnoreCase(command)) {
            return handleRun(cmdArgs);
        } else {
            System.err.println("Unknown CLI command: " + command);
            printUsage();
            return 4;
        }
    }

    private static void printUsage() {
        System.out.println("z64sim CLI — Headless z64 Simulator");
        System.out.println("Usage:");
        System.out.println("  java -jar z64sim.jar CLI assemble -asm <file.asm>");
        System.out.println("  java -jar z64sim.jar CLI run -asm <file.asm> [-config <config.json>] [-steps <N>] [-sync] [-out <report.json>]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  assemble    Check syntax and assemble source file");
        System.out.println("  run         Execute simulation headless with devices and output JSON state");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -asm <file>       Path to assembly source file (required)");
        System.out.println("  -config <file>    Path to JSON device and test configuration");
        System.out.println("  -steps <N>        Maximum instruction steps before timeout (default: 100000)");
        System.out.println("  -sync             Enable synchronous device callback mode (fast execution)");
        System.out.println("  -out <file>       Write JSON report to file instead of standard output");
        System.out.println("  -dump <addr:len>  Watch memory location (e.g. 0x800:8), repeatable");
        System.out.println("  -input <text>     Keyboard input string");
    }

    private static int handleAssemble(String[] args) {
        String asmPath = getOption(args, "-asm");
        if (asmPath == null) {
            System.err.println("Error: -asm argument is required for assemble command.");
            return 4;
        }

        File asmFile = new File(asmPath);
        if (!asmFile.exists() || !asmFile.isFile()) {
            System.err.println("Error: Assembly file not found: " + asmPath);
            return 4;
        }

        try (FileInputStream fis = new FileInputStream(asmFile);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            Assembler assembler = new Assembler(isr);
            try {
                assembler.Program();
            } catch (ParseException ignored) {
            }

            List<String> errors = assembler.getSyntaxErrors();
            if (!errors.isEmpty()) {
                System.err.println("Assembly failed with " + errors.size() + " error(s):");
                for (String err : errors) {
                    System.err.println("  " + err);
                }
                return 1;
            }

            System.out.println("Assembly successful.");
            return 0;
        } catch (Exception e) {
            System.err.println("Error during assembly: " + e.getMessage());
            return 1;
        }
    }

    private static int handleRun(String[] args) {
        String asmPath = getOption(args, "-asm");
        if (asmPath == null) {
            System.err.println("Error: -asm argument is required for run command.");
            return 4;
        }

        File asmFile = new File(asmPath);
        if (!asmFile.exists() || !asmFile.isFile()) {
            System.err.println("Error: Assembly file not found: " + asmPath);
            return 4;
        }

        String configPath = getOption(args, "-config");
        String stepsStr = getOption(args, "-steps");
        String outPath = getOption(args, "-out");
        String cliInput = getOption(args, "-input");
        boolean syncMode = hasFlag(args, "-sync");
        List<String> cliDumps = getOptions(args, "-dump");

        long maxSteps = 100000;
        if (stepsStr != null) {
            try {
                maxSteps = Long.parseLong(stepsStr);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid -steps value: " + stepsStr);
                return 4;
            }
        }

        // Parse optional JSON config file
        JSONObject configObj = null;
        if (configPath != null) {
            File configFile = new File(configPath);
            if (!configFile.exists() || !configFile.isFile()) {
                System.err.println("Error: Config file not found: " + configPath);
                return 4;
            }
            try (FileInputStream fis = new FileInputStream(configFile);
                 InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                JSONParser parser = new JSONParser();
                configObj = (JSONObject) parser.parse(isr);
            } catch (Exception e) {
                System.err.println("Error parsing config JSON: " + e.getMessage());
                return 4;
            }
        }

        String stopTerminal = null;
        StopMemory stopMemory = null;

        long keyInterval = 100;

        if (configObj != null) {
            if (configObj.containsKey("sync") && Boolean.TRUE.equals(configObj.get("sync"))) {
                syncMode = true;
            }
            if (configObj.containsKey("max_steps") && stepsStr == null) {
                maxSteps = ((Number) configObj.get("max_steps")).longValue();
            }
            if (configObj.containsKey("key_interval")) {
                keyInterval = ((Number) configObj.get("key_interval")).longValue();
            }
            if (configObj.containsKey("stop_terminal")) {
                stopTerminal = (String) configObj.get("stop_terminal");
            }
            if (configObj.containsKey("stop_memory")) {
                JSONObject smObj = (JSONObject) configObj.get("stop_memory");
                long sAddr = parseAddress(String.valueOf(smObj.get("address")));
                int sSize = smObj.containsKey("size") ? ((Number) smObj.get("size")).intValue() : 1;
                long sVal = parseAddress(String.valueOf(smObj.get("value")));
                stopMemory = new StopMemory(sAddr, sSize, sVal);
            }
        }

        // Initialize Simulator
        SimulatorController.init();
        Devices devices = Devices.getInstance();
        devices.clearAllDevices();

        // Assemble student program
        Program program;
        try (FileInputStream fis = new FileInputStream(asmFile);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            Assembler assembler = new Assembler(isr);
            try {
                assembler.Program();
            } catch (ParseException ignored) {
            }

            List<String> errors = assembler.getSyntaxErrors();
            if (!errors.isEmpty()) {
                System.err.println("Assembly failed with " + errors.size() + " error(s):");
                for (String err : errors) {
                    System.err.println("  " + err);
                }
                return 1;
            }
            program = assembler.getProgram();
        } catch (Exception e) {
            System.err.println("Error reading/assembling file: " + e.getMessage());
            return 1;
        }

        SimulatorController.loadProgram(program);

        // Configure devices from JSON config
        Keyboard keyboardDevice = null;
        Terminal terminalDevice = null;
        DeviceMapping alarmMapping = null;
        Long alarmPort = null;
        String keyboardInput = cliInput;

        if (configObj != null && configObj.containsKey("devices")) {
            JSONArray devList = (JSONArray) configObj.get("devices");
            for (Object obj : devList) {
                if (!(obj instanceof JSONObject)) continue;
                JSONObject dObj = (JSONObject) obj;

                String typeName = (String) dObj.get("type");
                if (typeName == null) {
                    typeName = (String) dObj.get("class");
                }
                if (typeName == null) {
                    System.err.println("Error: device entry missing 'type' or 'class'.");
                    return 4;
                }

                int ivn = -1;
                if (dObj.containsKey("ivn")) {
                    ivn = ((Number) dObj.get("ivn")).intValue();
                }

                Device device = instantiateDevice(typeName);
                if (device == null) {
                    System.err.println("Error: Could not instantiate device: " + typeName);
                    return 4;
                }

                if (syncMode) {
                    device.setDmaMode(true);
                }

                if (device instanceof Keyboard) {
                    keyboardDevice = (Keyboard) device;
                    if (dObj.containsKey("input") && keyboardInput == null) {
                        keyboardInput = (String) dObj.get("input");
                    }
                } else if (device instanceof Terminal) {
                    terminalDevice = (Terminal) device;
                }

                DeviceMapping mapping = new DeviceMapping(device, ivn);
                DeviceDescriptor desc = device.getDescriptor();

                if (dObj.containsKey("ports")) {
                    JSONObject portsObj = (JSONObject) dObj.get("ports");
                    for (Object k : portsObj.keySet()) {
                        String elementName = (String) k;
                        long portAddr = parseAddress(String.valueOf(portsObj.get(elementName)));
                        IoPortDescriptor portDesc = desc.getIoPort(elementName);
                        if (portDesc == null) {
                            System.err.println("Warning: Element '" + elementName + "' not found on device " + typeName);
                            continue;
                        }
                        mapping.assignPort(portAddr, portDesc);
                        devices.registerPort(portAddr, mapping);

                        if ("Alarm".equalsIgnoreCase(device.getClass().getSimpleName()) && "ALARM".equalsIgnoreCase(elementName)) {
                            alarmMapping = mapping;
                            alarmPort = portAddr;
                        }
                    }
                }

                devices.registerDevice(ivn, mapping);
            }
        }

        // Prepare memory watch locations
        List<MemoryWatch> watchList = new ArrayList<>();
        for (String d : cliDumps) {
            watchList.add(parseMemoryWatch(d));
        }
        if (configObj != null && configObj.containsKey("watch_memory")) {
            JSONArray arr = (JSONArray) configObj.get("watch_memory");
            for (Object item : arr) {
                if (item instanceof String) {
                    watchList.add(parseMemoryWatch((String) item));
                }
            }
        }

        // Notify simulation start (headless guard in Devices.java prevents GUI creation)
        devices.notifySimulationStart();

        // Simulation execution loop
        SimulatorController sc = SimulatorController.getInstance();
        int keyIndex = 0;
        long steps = 0;
        long lastKeyStep = -keyInterval;

        try {
            while (steps < maxSteps) {
                // Feed keyboard input sequentially when INT_REQ is idle and enough steps have elapsed (or CPU is halted)
                if (keyboardDevice != null && keyboardInput != null && keyIndex < keyboardInput.length()) {
                    if (keyboardDevice.getIntReq() == 0 && (sc.halted || (steps - lastKeyStep >= keyInterval))) {
                        char c = keyboardInput.charAt(keyIndex++);
                        keyboardDevice.keyPressed((int) c);
                        lastKeyStep = steps;
                    }
                }

                boolean inputsExhausted = (keyboardDevice == null || keyboardInput == null || keyIndex >= keyboardInput.length());

                // Condition 1: Dead halt with interrupts disabled
                if (sc.halted && !SimulatorController.getCpuState().getIF()) {
                    break;
                }

                // Condition 2: Idle halt with all stimuli consumed and no pending interrupts
                if (sc.halted && inputsExhausted && !devices.isIRQPending()) {
                    break;
                }

                // Condition 3: Explicit test completion (terminal text or memory condition)
                if (stopTerminal != null && terminalDevice != null) {
                    String tText = extractTerminalText(terminalDevice);
                    if (tText.contains(stopTerminal)) {
                        break;
                    }
                }
                if (stopMemory != null) {
                    long currentVal = readMemoryValue(stopMemory.address, stopMemory.size);
                    if (currentVal == stopMemory.expectedValue) {
                        break;
                    }
                }

                sc.stepInstruction();
                steps++;
            }
        } catch (SimulatorException e) {
            devices.notifySimulationStop();
            System.err.println("Runtime error at RIP=0x" + Long.toHexString(SimulatorController.getCpuState().getRIP()) + ": " + e.getMessage());
            return 2;
        } catch (RuntimeException e) {
            devices.notifySimulationStop();
            System.err.println("Unexpected runtime exception: " + e.getMessage());
            return 2;
        }

        devices.notifySimulationStop();

        if (steps >= maxSteps) {
            System.err.println("Step limit exceeded: simulation did not finish within " + maxSteps + " steps.");
            return 3;
        }

        // Build execution state report
        JSONObject report = buildReport(steps, sc, watchList, terminalDevice, alarmMapping, alarmPort);

        if (outPath != null) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(outPath), StandardCharsets.UTF_8)) {
                writer.write(report.toJSONString());
            } catch (Exception e) {
                System.err.println("Error writing report to " + outPath + ": " + e.getMessage());
                return 4;
            }
        } else {
            System.out.println(report.toJSONString());
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject buildReport(
            long steps,
            SimulatorController sc,
            List<MemoryWatch> watchList,
            Terminal terminal,
            DeviceMapping alarmMapping,
            Long alarmPort
    ) {
        JSONObject report = new JSONObject();
        report.put("steps", steps);
        report.put("rip", String.format("0x%x", SimulatorController.getCpuState().getRIP()));
        report.put("rsp", String.format("0x%x", SimulatorController.getCpuState().getRSP()));

        // Registers
        Map<String, String> regs = new LinkedHashMap<>();
        regs.put("rax", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RAX)));
        regs.put("rbx", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RBX)));
        regs.put("rcx", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RCX)));
        regs.put("rdx", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RDX)));
        regs.put("rsi", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RSI)));
        regs.put("rdi", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RDI)));
        regs.put("rbp", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RBP)));
        regs.put("rsp", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.RSP)));
        regs.put("r8",  String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R8)));
        regs.put("r9",  String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R9)));
        regs.put("r10", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R10)));
        regs.put("r11", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R11)));
        regs.put("r12", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R12)));
        regs.put("r13", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R13)));
        regs.put("r14", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R14)));
        regs.put("r15", String.format("0x%x", SimulatorController.getCpuState().getRegisterValue(Register.R15)));
        report.put("registers", new JSONObject(regs));

        // Flags
        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put("CF", SimulatorController.getCpuState().getCF());
        flags.put("ZF", SimulatorController.getCpuState().getZF());
        flags.put("SF", SimulatorController.getCpuState().getSF());
        flags.put("OF", SimulatorController.getCpuState().getOF());
        flags.put("PF", SimulatorController.getCpuState().getPF());
        flags.put("IF", SimulatorController.getCpuState().getIF());
        report.put("flags", new JSONObject(flags));

        // Memory watch inspection
        JSONObject memObj = new JSONObject();
        for (MemoryWatch w : watchList) {
            long val = readMemoryValue(w.address, w.size);
            String hexVal;
            if (w.size == 1) {
                hexVal = String.format("0x%02x", val & 0xFFL);
            } else if (w.size == 2) {
                hexVal = String.format("0x%04x", val & 0xFFFFL);
            } else if (w.size == 4) {
                hexVal = String.format("0x%08x", val & 0xFFFFFFFFL);
            } else {
                hexVal = String.format("0x%016x", val);
            }
            memObj.put(String.format("0x%x", w.address), hexVal);
        }
        report.put("memory", memObj);

        // Device inspection
        JSONObject devObj = new JSONObject();
        if (terminal != null) {
            JSONObject termObj = new JSONObject();
            termObj.put("text", extractTerminalText(terminal));
            termObj.put("cursorRow", terminal.getCursorRow());
            termObj.put("cursorCol", terminal.getCursorCol());
            devObj.put("Terminal", termObj);
        }
        if (alarmMapping != null && alarmPort != null) {
            JSONObject alarmObj = new JSONObject();
            alarmObj.put("alarmOn", alarmMapping.read(alarmPort) != 0);
            devObj.put("Alarm", alarmObj);
        }
        report.put("devices", devObj);

        return report;
    }

    private static String extractTerminalText(Terminal terminal) {
        char[][] buf = terminal.getBuffer();
        StringBuilder sb = new StringBuilder();
        int maxRow = 0;
        for (int r = 0; r < Terminal.ROWS; r++) {
            boolean hasChar = false;
            for (int c = 0; c < Terminal.COLS; c++) {
                if (buf[r][c] != ' ' && buf[r][c] != 0) {
                    hasChar = true;
                    break;
                }
            }
            if (hasChar || r <= terminal.getCursorRow()) {
                maxRow = r;
            }
        }

        for (int r = 0; r <= maxRow; r++) {
            StringBuilder line = new StringBuilder();
            for (int c = 0; c < Terminal.COLS; c++) {
                char ch = buf[r][c];
                line.append((ch == 0) ? ' ' : ch);
            }
            // Strip trailing whitespace
            int end = line.length();
            while (end > 0 && line.charAt(end - 1) == ' ') {
                end--;
            }
            sb.append(line.substring(0, end));
            if (r < maxRow) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static long readMemoryValue(long address, int size) {
        long value = 0;
        for (int i = 0; i < size; i++) {
            byte b = Memory.getValueAt(address + i);
            value |= ((long) (b & 0xFF)) << (i * 8);
        }
        return value;
    }

    private static Device instantiateDevice(String typeName) {
        String fullClassName = typeName.contains(".") ? typeName : DEFAULT_DEVICE_PKG + typeName;
        try {
            Class<?> cls = Class.forName(fullClassName);
            if (!Device.class.isAssignableFrom(cls)) {
                return null;
            }
            return (Device) cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static long parseAddress(String str) {
        str = str.trim();
        if (str.startsWith("0x") || str.startsWith("0X")) {
            return Long.parseLong(str.substring(2), 16);
        }
        return Long.parseLong(str);
    }

    private static MemoryWatch parseMemoryWatch(String spec) {
        spec = spec.trim();
        String[] parts = spec.split(":");
        long addr = parseAddress(parts[0]);
        int size = 8;
        if (parts.length > 1) {
            size = Integer.parseInt(parts[1].trim());
        }
        return new MemoryWatch(addr, size);
    }

    private static class MemoryWatch {
        final long address;
        final int size;

        MemoryWatch(long address, int size) {
            this.address = address;
            this.size = size;
        }
    }

    private static String getOption(String[] args, String opt) {
        for (int i = 0; i < args.length - 1; i++) {
            if (opt.equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }

    private static List<String> getOptions(String[] args, String opt) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (opt.equals(args[i])) {
                list.add(args[i + 1]);
            }
        }
        return list;
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String a : args) {
            if (flag.equals(a)) return true;
        }
        return false;
    }

    private static class StopMemory {
        final long address;
        final int size;
        final long expectedValue;

        StopMemory(long address, int size, long expectedValue) {
            this.address = address;
            this.size = size;
            this.expectedValue = expectedValue;
        }
    }
}

