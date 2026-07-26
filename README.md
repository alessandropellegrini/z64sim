# z64sim: The z64 CPU Simulator

[![Build](https://github.com/alessandropellegrini/z64sim/actions/workflows/ci.yml/badge.svg)](https://github.com/alessandropellegrini/z64sim/actions/workflows/ci.yml)
[![REUSE Compliance](https://github.com/alessandropellegrini/z64sim/actions/workflows/reuse.yml/badge.svg)](https://github.com/alessandropellegrini/z64sim/actions/workflows/reuse.yml)
[![GitHub issues](https://img.shields.io/github/issues/alessandropellegrini/z64sim)](https://github.com/alessandropellegrini/z64sim/issues)
[![GitHub license](https://img.shields.io/github/license/alessandropellegrini/z64sim)](https://github.com/alessandropellegrini/z64sim)
[![Github all releases](https://img.shields.io/github/downloads/alessandropellegrini/z64sim/total.svg)](https://github.com/alessandropellegrini/z64sim/releases/)
---

This is a simulator for the z64 CPU, a fictional CPU designed for educational purposes.

## Getting and running the simulator

Official builds are available through [GitHub Releases](https://github.com/alessandropellegrini/z64sim/releases).
In order to run it, you need to have Java 8 or later installed. To run the simulator, simply execute the following
command:

    java -jar z64sim.jar

or double-click on the `z64sim.jar` file if your system supports it.

**Be careful**: The simulator is currently in a very early stage of development and is therefore experimental. If
you find a bug, please report it on
the [issue tracker](https://github.com/alessandropellegrini/z64sim/issues).
Please provide a detailed description of the bug and, if possible, a minimal example that reproduces it.

## Using the simulator

The simulator provides a minimalistic editor for writing z64 assembly code.
The toolbar contains the following buttons to interact with the simulator:

* ![](src/main/resources/images/assemble_icon24.png): **Assemble** — assembles the code in the editor and loads it into
  the simulator. If the code is invalid, a message log will appear at the bottom of the window (below the CPU registers).
* ![](src/main/resources/images/step24.png): **Step** — executes a single instruction. If the program counter does not
  point to a valid instruction, the simulator will stop. This button has no effect if no program is loaded or if
  execution has reached a `hlt` instruction.
* ![](src/main/resources/images/run24.png): **Run** — starts continuous execution of the program. This is equivalent to
  repeatedly pressing the Step button.
* ![](src/main/resources/images/stop24.png): **Stop** — halts a running program. This is useful when execution is
  in progress and you want to regain control of the simulator.
* ![](src/main/resources/images/iodevice24.png): Open the **Device Manager** — see below.
* ![](src/main/resources/images/mu24.png): Open the **µ-ops Animation** — see below.

### Keyboard shortcuts

| Shortcut      | Action              |
|---------------|---------------------|
| Ctrl+N        | New file            |
| Ctrl+O        | Open file           |
| Ctrl+S        | Save file           |
| Ctrl+B        | Assemble program    |
| F8            | Step instruction    |
| F5            | Run program         |
| Shift+F5      | Stop program        |

## Managing Devices

z64sim supports pluggable I/O devices that interact with the CPU through port-mapped I/O
instructions (`in` / `out`). Devices are managed through the **Device Manager** dialog.

A **DMAC** (Direct Memory Access Controller) is always present in the system. It occupies
IVN 0 and has its I/O ports hardwired at addresses `0x00`–`0x09`. The DMAC cannot be
removed, reordered, or reconfigured. User devices must not use port addresses in this
reserved range. The DMAC supports the `insb`/`insw`/`insl`/`outsb`/`outsw`/`outsl`
string I/O instructions, as well as manual programming via `out` instructions to its ports.

Device configuration is *persistent*: when the simulator is restarted, the previous device
configuration is reloaded.

### Adding a device

1. Open the Device Manager.
2. Select a device type from the drop-down list (sorted alphabetically) and click **Add**.
3. The device appears in the table. If the device supports interrupt-driven I/O, an IVN
   (Interrupt Vector Number) is automatically assigned. Non-interrupt devices show "—" in
   the IVN column.
4. Click the device row to configure its I/O port addresses in the port table below.
   Each port element (registers, flip-flops) must be assigned a unique I/O address.

### Editing IVN and port addresses

- Double-click the **IVN** cell to change the interrupt vector number (0–255). The
  simulator validates that no two interrupt-capable devices share the same IVN.
- Double-click a port **Address** cell to assign an I/O port address in hexadecimal
  (e.g., `0x10`).

### Reordering and removing devices

- **Drag and drop** rows in the device table to change the daisy-chain priority order
  (relevant for interrupt arbitration).
- Select a device row and click the **trash** button to remove a device from the system.
- Close the dialog with **OK** to apply changes, or **Cancel** to discard.

### Viewing the interface schematic

Select a device row and click the **Interface** button to open a hardware-level schematic
of the device's I/O interface. You can zoom in/out
(Ctrl+mouse wheel or +/− buttons), scroll, and export the diagram as PNG.

### IVT table in the main window

The **IVT** (Interrupt Vector Table) panel in the main window shows only
interrupt-capable devices at their assigned IVN slots.

## Custom Devices

You can implement your own devices and add them to the simulator. Compile your device
class against the z64sim JAR and place the resulting `.class` file under the following
directory structure, relative to where you launch the JAR:

    it/uniroma2/pellegrini/z64sim/devices/MyDevice.class

The simulator scans this path in the current working directory at startup. See the
[Implementing Custom Devices](docs/implementing-devices.md) guide for the full API
reference, protocol conventions, and deployment instructions.

## µ-ops Animation

The **µ-ops Animation** dialog provides an interactive, step-by-step visualisation of
the microoperations that the Control Unit executes for every z64 instruction.
It is opened by clicking the **µ** button in the toolbar, or can be triggered from
the Instruction Inspection dialog.

If the dialog is activated clicking the **µ** button in the toolbar, it is possible
to select an instruction to visualize from a list of representative ones.

In the bottom part of the panel, the application displays the current µ-op text,
the active control signals, a step counter, and playback controls with an adjustable
speed slider.

## Building from source

This project relies on [Maven](https://maven.apache.org/) for building. To build the project, simply run the following
command:

    mvn package

Please note that the project requires [JavaCC](https://javacc.github.io/javacc/) for generating the parser.
