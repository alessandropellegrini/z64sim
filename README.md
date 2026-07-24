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

* ![](src/main/resources/images/assemble_icon.png): **Assemble** — assembles the code in the editor and loads it into
  the simulator. If the code is invalid, a message log will appear at the bottom of the window (below the CPU registers).
* ![](src/main/resources/images/step.png): **Step** — executes a single instruction. If the program counter does not
  point to a valid instruction, the simulator will stop. This button has no effect if no program is loaded or if
  execution has reached a `hlt` instruction.
* ![](src/main/resources/images/run.png): **Run** — starts continuous execution of the program. This is equivalent to
  repeatedly pressing the Step button until `hlt` is reached or an error occurs.
* ![](src/main/resources/images/stop24.png): **Stop** — halts a running program. This is useful when execution is
  in progress (e.g., the program is in an infinite loop) and you want to regain control of the simulator.

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

## Building from source

This project relies on [Maven](https://maven.apache.org/) for building. To build the project, simply run the following
command:

    mvn package

Please note that the project requires [JavaCC](https://javacc.github.io/javacc/) for generating the parser.
