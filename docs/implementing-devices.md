# Implementing Custom Devices

z64sim supports pluggable I/O devices that interact with the CPU through
port-mapped I/O. This guide explains how to write a new device, declare its
hardware interface, and make it available in the simulator.

## Architecture Overview

A device in z64sim consists of three parts:

1. **A Java class** extending `Device` — contains the behavioural logic.
2. **A `DeviceDescriptor`** — declares the hardware interface (flip-flops,
   registers, protocol).
3. **Read/write handlers** — lambdas that react to CPU `in`/`out`
   instructions.

The simulator discovers device classes at runtime via reflection. Any
concrete subclass of `Device` in the package
`it.uniroma2.pellegrini.z64sim.devices` is automatically listed in the
Device Manager dialog.

## Quick Start

Here is a minimal device — a single writable register that the CPU can write to:

```java
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

public class MyDevice extends Device {
    private long value = 0;

    public MyDevice() {
        onRead("DATA", () -> value);
        onWrite("DATA", data -> value = data);
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("My Device")
                .ioPort("DATA",
                        IoPortDescriptor.REGISTER
                      | IoPortDescriptor.READABLE
                      | IoPortDescriptor.WRITABLE, 8)
                .build();
    }
}
```

## The Device Descriptor

The descriptor is built via `DeviceDescriptor.Builder` and declares
everything the simulator needs to know about the device's I/O interface.

### Builder Methods

| Method                             | Effect                                                                                                                                                          |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Builder(String name)`             | Sets the device display name.                                                                                                                                   |
| `.busyWaiting()`                   | Enables the busy-waiting protocol. Automatically adds a `STATUS` flip-flop (readable + writable).                                                               |
| `.interrupts()`                    | Enables interrupt-driven I/O. Automatically adds an `INT_REQ` flip-flop (writable). The IVN (Interrupt Vector Number) is hardwired at device registration time. |
| `.ioPort(name, flags, widthBytes)` | Adds a custom register element with the given width (1, 2, 4, or 8 bytes).                                                                                      |
| `.ioPort(name, flags)`             | Adds a custom flip-flop element (width defaults to 1 byte).                                                                                                     |
| `.build()`                         | Returns the immutable `DeviceDescriptor`.                                                                                                                       |

### I/O Port Flags

Flags are defined as constants on `IoPortDescriptor` and combined with
bitwise OR:

| Flag              | Value  | Meaning                                                              |
|-------------------|--------|----------------------------------------------------------------------|
| `FLIP_FLOP`       | `0x01` | Element is a 1-bit flip-flop.                                        |
| `REGISTER`        | `0x02` | Element is a multi-byte register.                                    |
| `READABLE`        | `0x04` | CPU can read from this element (`in` instruction).                   |
| `WRITABLE`        | `0x08` | CPU can write to this element (`out` instruction).                   |
| `CONNECTED_TO_CU` | `0x10` | Element output feeds the Control Unit (schematic rendering hint).    |
| `CONNECTED_TO_PU` | `0x20` | Element output feeds the Processing Unit (schematic rendering hint). |

`FLIP_FLOP` and `REGISTER` are mutually exclusive. Every element should have at least
one of `READABLE` or `WRITABLE`.

## Handler Registration

In the constructor, call `onRead(name, supplier)` and
`onWrite(name, consumer)` to attach behaviour to each I/O element:

```java
// When the CPU executes "in %rax, $PORT_ADDR", this lambda supplies the value.
onRead("DATA", () -> value);

// When the CPU executes "out %rax, $PORT_ADDR", this lambda receives the value.
onWrite("DATA", data -> value = data);
```

Handler names must match the element names declared in the descriptor.
Elements without a matching handler return `0` on read and silently ignore
writes.

### INT_REQ Handlers

The `INT_REQ` flip-flop is automatically managed by the `Device` base
class. You do not need to register handlers for it. To raise an interrupt,
call `raiseInterrupt()`:

```java
raiseInterrupt();  // sets INT_REQ = 1 and asserts the IRQ line
```

When the driver clears `INT_REQ` by writing to its port, the base class
resets the flip-flop and calls `onIntReqCleared()`. Override this method if
your device should automatically restart when the interrupt is acknowledged:

```java
@Override
protected void onIntReqCleared() {
    // Restart the device for continuous operation
    scheduleAfterDelay(() -> {
        generateNewData();
        raiseInterrupt();
    }, delay);
}
```

## Protocols

### Busy-Waiting

Call `.busyWaiting()` on the builder. This adds a `STATUS` flip-flop that
the CPU polls in a loop. The convention is:

- **STATUS = 1**: device is ready (idle or operation complete).
- **STATUS = 0**: device is busy.

A typical implementation sets STATUS to 0 when the CPU starts an operation,
then uses `scheduleAfterDelay()` to set it back to 1 after a simulated
processing time:

```java
onRead("STATUS", () -> status);
onWrite("STATUS", data -> {
    status = 0;  // busy
    int delay = 200 + random.nextInt(800);
    scheduleAfterDelay(() -> {
        processData();
        status = 1;  // ready
    }, delay);
});
```

### Interrupt-Driven I/O

Call `.interrupts()` on the builder. This adds `INT_REQ`. The IVN is
hardwired at device registration time (not a CPU-accessible port). The
device signals completion by calling `raiseInterrupt()`, which notifies the
CPU via the interrupt mechanism.

The CPU's interrupt handler reads the data and clears `INT_REQ`. A typical
device implementation:

```java
onWrite("STATUS", data -> {
    scheduleAfterDelay(() -> {
        result = computeSomething();
        raiseInterrupt();
    }, delay);
});
```

### No Protocol

A device with neither `.busyWaiting()` nor `.interrupts()` simply responds
to port reads and writes with no protocol overhead. The Alarm device is an
example: the CPU writes 0 or 1 to turn the alarm off or on, and that's it.

## Simulating Processing Delays

Use `scheduleAfterDelay(Runnable action, int delayMs)` to simulate device
latency. The action fires on the Event Dispatch Thread after the specified
milliseconds:

```java
scheduleAfterDelay(() -> {
    dataIn = generateRandomValue();
    status = 1;
}, 500);
```

## Device GUI

Devices can display a graphical interface during simulation. Override the
lifecycle hooks:

```java
@Override
protected void onSimulationStart() {
    SwingUtilities.invokeLater(() -> {
        frame = new JFrame("My Device");
        // ... build and show GUI ...
        frame.setVisible(true);
    });
}

@Override
protected void onSimulationStop() {
    if (frame != null) {
        frame.dispose();
        frame = null;
    }
}
```

`onSimulationStart()` is called when the user presses Run or Step for the
first time. `onSimulationStop()` is called when execution halts or the user
stops the program.

## Interface Schematic

The simulator can render a hardware-level schematic of each device's I/O
interface. This schematic is automatically generated from the descriptor —
no extra code is needed. In the Device Manager dialog, select a device and
click the **Interface** button to view it.

The schematic shows:

- The CPU and its three buses (I/O Address Bus, I/O Data Bus, I/O Control Bus)
- The address decoder with selector outputs
- All flip-flops with their S/R logic gates
- All registers with their tri-state buffers and load logic
- The Control Unit hexagon (if applicable)
- Interrupt request logic and daisy-chain wiring (if interrupt-capable)

## Complete Example: Timer Device

The Timer device fires an interrupt after a programmable delay:

```java
package it.uniroma2.pellegrini.z64sim.devices;

import it.uniroma2.pellegrini.z64sim.model.Device;
import it.uniroma2.pellegrini.z64sim.model.DeviceDescriptor;
import it.uniroma2.pellegrini.z64sim.model.IoPortDescriptor;

public class Timer extends Device {
    private long delayMs = 0;

    public Timer() {
        // CPU writes the delay in milliseconds
        onWrite("DELAY", data -> delayMs = data & 0xFFFFFFFFL);

        // CPU writes to STATUS to start the timer
        onWrite("STATUS", data -> {
            int delay = (int) delayMs;
            if (delay > 0) {
                scheduleAfterDelay(this::raiseInterrupt, delay);
            }
        });
    }

    @Override
    public DeviceDescriptor getDescriptor() {
        return new DeviceDescriptor.Builder("Timer")
                .interrupts()
                .ioPort("STATUS",
                        IoPortDescriptor.FLIP_FLOP | IoPortDescriptor.WRITABLE)
                .ioPort("DELAY",
                        IoPortDescriptor.REGISTER | IoPortDescriptor.WRITABLE, 4)
                .build();
    }
}
```

An assembly program using this device:

```asm
.equ TIMER_INT_REQ, 0x30
.equ TIMER_STATUS,  0x31
.equ TIMER_DELAY,   0x32

.data
count: .long 0

.text
_start:
    movl $1000, %eax
    outl %eax, $TIMER_DELAY    # set delay = 1000 ms
    outb %al, $TIMER_STATUS    # start timer
    sti                         # enable interrupts
    hlt                         # wait for interrupt

# IVN is set in the Device Manager (hardwired at registration)
.driver 1
    incl count                  # increment counter
    iret
```

## Deploying a Custom Device

The simulator automatically scans the current working directory for device
classes. Just compile and place the `.class` file in the correct package
subdirectory relative to where you run the JAR from:

1. Create your device source file (e.g., `MyDevice.java`) with the correct
   package declaration:
   ```java
   package it.uniroma2.pellegrini.z64sim.devices;
   ```
2. Compile against the z64sim JAR:
   ```
   javac -cp z64sim.jar MyDevice.java
   ```
   This produces `MyDevice.class` in the current directory.
3. Move it into the expected package directory structure:
   ```
   mkdir -p it/uniroma2/pellegrini/z64sim/devices
   mv MyDevice.class it/uniroma2/pellegrini/z64sim/devices/
   ```
4. Launch z64sim from the same directory:
   ```
   java -jar z64sim.jar
   ```
   Your device will appear in the Device Manager's drop-down list.

Your working directory should look like:
```
my-project/
├── z64sim.jar
└── it/uniroma2/pellegrini/z64sim/devices/
    └── MyDevice.class
```

## DMAC Interaction

The **DMAC** (Direct Memory Access Controller) is a permanent system device
that transfers data between memory and I/O devices autonomously. It is
always present, bound to IVN 0, with I/O ports hardwired at addresses
`0x00`–`0x07`.

### Reserved port range

User devices **must not** use port addresses `0x00`–`0x07`. The Device
Manager validates this and rejects any user device port that falls within
the reserved range.

### DMA mode

When the DMAC performs a burst transfer with your device, it enables
**DMA mode** on the device. This changes the behaviour of
`scheduleAfterDelay()`: instead of scheduling the callback on a Swing Timer,
the callback executes **immediately** (synchronously). This is how the DMAC
follows the busy-waiting protocol while completing the entire transfer in a
single simulation step.

In practice, your device does not need to do anything special to support
DMA. If your device uses `scheduleAfterDelay()` in its STATUS handler
(which is the standard busy-waiting pattern), it will work correctly with
the DMAC automatically.

### How the DMAC interacts with your device

For each word in a transfer:

1. **Input** (device→memory): the DMAC writes your device's STATUS port
   (starting the device), then reads the data port.
2. **Output** (memory→device): the DMAC writes data to your device's data port,
   then writes STATUS to start processing.

The DMAC resolves your device's STATUS port by name through the
`DeviceMapping`. This emulates the hardware WAIT blocking signal, which in
real hardware is a direct wire from the device's STATUS flip-flop to the
DMAC's CU.

## Devices with a GUI

Some devices need a visual representation — a terminal screen, an LED
indicator, a display panel. The `Device` base class provides two lifecycle
hooks for this:

- **`onSimulationStart()`** — called when the user starts running the
  program. Use this to create a Swing `JFrame` and display the device's GUI.
- **`onSimulationStop()`** — called when the program halts or the user stops
  execution. Use this to dispose the GUI window.

### Example: Terminal device

The `Terminal` device writes one ASCII character at a time to a 24×80 text
buffer and renders it in a Swing window with a green-on-dark monospace
display:

```java
@Override
protected void onSimulationStart() {
    SwingUtilities.invokeLater(() -> {
        frame = new JFrame("Terminal");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);

        panel = new TerminalPanel();
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });
}

@Override
protected void onSimulationStop() {
    SwingUtilities.invokeLater(() -> {
        if (frame != null) {
            frame.dispose();
            frame = null;
            panel = null;
        }
    });
}
```

**Key points:**

- Always wrap Swing operations in `SwingUtilities.invokeLater()`.
- Set `DO_NOTHING_ON_CLOSE` so the user cannot close the window
  independently of the simulation.
- In the write handler, trigger a repaint when the display needs updating:

```java
onWrite("STATUS", val -> {
    status = 0;
    scheduleAfterDelay(() -> {
        processChar((char) (data & 0x7F));
        status = 1;
        if (panel != null) {
            SwingUtilities.invokeLater(panel::repaint);
        }
    }, DELAY_MS);
});
```

See `Terminal.java` and `Alarm.java` for complete working examples.
