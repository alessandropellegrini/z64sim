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
In order to run it, you need to have Java 11 or newer installed. To run the simulator, simply run the following command:

    java -jar z64sim.jar

or double-click on the `z64sim.jar` file if your system supports it.

**Be careful**: The simulator is currently in a very early stage of development, and it is therefore experimental. If
you happen to find a bug, please report it on
the [issue tracker](https://github.com/alessandropellegrini/z64sim/issues).
Please provide a detailed description of the bug, and if possible, a minimal example that reproduces it.

## Using the simulator

The simulator provides a minimalistic editor for writing z64 assembly code.
In the toolbar, there are few buttons to interact with the simulator:

* ![](src/main/resources/images/assemble_icon.png): this is the assemble button. It assembles the code in the editor and
  loads it into the simulator. If the code is not valid, a message log will be shown in the bottom of the window (below
  the CPU registers).
* ![](src/main/resources/images/step.png): this button executes a single instruction. If the program counter is not
  pointing to a valid instruction, the simulator will stop. This button does nothing if a program is not loaded, or if
  execution reached a `hlt` instruction.
* ![](src/main/resources/images/run.png): this button starts the execution of the program. It corresponds to
  reiteratively pressing the `step` button until `hlt` is reached or an error occurs.

## Building from source

This project relies on [Maven](https://maven.apache.org/) for building. To build the project, simply run the following
command:

    mvn package

Please note that the project requires [JavaCC](https://javacc.github.io/javacc/) for generating the parser.
